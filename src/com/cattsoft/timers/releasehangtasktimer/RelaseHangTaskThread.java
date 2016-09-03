package com.cattsoft.timers.releasehangtasktimer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.cattsoft.im.vo.InterMsgCenterSVO;
import com.cattsoft.pub.ConnectionFactory;
import com.cattsoft.pub.SPSConfig;
import com.cattsoft.pub.dao.Sql;
import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.pub.util.JdbcUtil;
import com.cattsoft.pub.util.StringUtil;
import com.cattsoft.socket.vo.TaskOrderMVO;
import com.cattsoft.timers.BaseThread;

public class RelaseHangTaskThread extends BaseThread {

	public RelaseHangTaskThread(int modRemainder) {
		this.threadIndex = modRemainder;
		log = Logger.getLogger(RelaseHangTaskThread.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initBizData() throws AppException, SysException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		TaskOrderMVO vo = null;
		long modNum;
		int nodNumInt;

		try {
			log.info("[thread " + this.threadIndex
					+ "] 查询INTER_MSG_CENTER Begin ... ");

			/**
			 * 扫数据库代码
			 */
			if ("solr".equals(this.baseTimer.scanType)) {
				/**
				 * 扫solr代码
				 */

				String solrType = this.baseTimer.getSolrType();
				SolrDocumentList resultList = null;
				SolrQuery solrQuery = new SolrQuery();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String startTime = this.dataToSolrDate(sdf.format(new Date(
						new Date().getTime() + 60 * 60 * 1000)));
				solrQuery.setQuery("EXT_WO_NBR:["
						+ new Long(baseTimer.THE_LAST_MAX_ID)
						+ " TO *]  AND RUN_STS:H AND BOOK_TIME:[* TO "
						+ startTime + "]");
				// solrQuery.setQuery("*:*");
				solrQuery.setSort("EXT_WO_NBR", ORDER.asc);

				solrQuery.setStart(0);
				solrQuery.setRows(new Integer(baseTimer.getMaxCountPerCycle()));
				HttpSolrClient client = null;
				QueryResponse response = null;
				CloudSolrClient cloudSolrServer = null;
				try {
					if ("solr".equals(solrType)) {
						client = new HttpSolrClient(this.baseTimer.getSolrUrl()
								+ "TO");
						response = client.query(solrQuery, METHOD.POST);
					} else {
						cloudSolrServer = new CloudSolrClient(
								this.baseTimer.getZkHost());
						cloudSolrServer.setDefaultCollection("to");
						cloudSolrServer.setZkClientTimeout(5000);
						cloudSolrServer.setZkConnectTimeout(5000);
						cloudSolrServer.connect();
						cloudSolrServer.query(solrQuery, METHOD.POST);
					}
				} catch (Exception e) {
					log.debug("查询solr出差。");
				} finally {
					if (client != null) {
						client.close();
					}
					if (cloudSolrServer != null) {
						cloudSolrServer.close();
					}
				}

				resultList = response.getResults();

				for (int i = 0; i < resultList.size(); i++) {
					SolrDocument taskOrder = resultList.get(i);
					taskOrder.removeFields("_version_");

					String extWoNbr = (String) taskOrder.get("EXT_WO_NBR");
					// 取摸然后入集合
					modNum = (Long.parseLong(extWoNbr))
							% (baseTimer.threadNumber);

					nodNumInt = Integer.parseInt(new Long(modNum).toString());

					if (baseTimer.bizDataList[nodNumInt] == null) {
						log.debug("[threadNumber:" + threadIndex
								+ "初始化LinkedList");
						baseTimer.bizDataList[nodNumInt] = new LinkedList();
					}
					baseTimer.THE_LAST_MAX_ID = Long.parseLong(extWoNbr);

					// modify by caiqian 判断是否重复取出已经正在执行的数据,如果重复则不添加到队列中
					if (baseTimer.containInLastRecord(nodNumInt, extWoNbr)) {
						log.debug("[thread " + this.threadIndex
								+ "] 查询出 MSG_ID= " + extWoNbr + " 与"
								+ nodNumInt + "号辅助线程最后处理的记录重复，不加入辅助线程的队列");
					} else {
						synchronized (baseTimer.bizDataList[nodNumInt]) {
							((LinkedList) baseTimer.bizDataList[nodNumInt])
									.addLast(taskOrder);
						}
					}
					queryDataRows = queryDataRows + 1;
				}
			} else {
				StringBuffer sql = new StringBuffer(
						"\nselect tas.to_nbr ,tas.spec_serv_id,tas.to_staff_id,tas.sharding_id,tas.ext_wo_nbr from task_order tas,so,so_book sb \n");

				sql.append("where tas.EXT_WO_NBR = so.EXT_SO_NBR \n");
				sql.append("and so.SO_NBR = sb.SO_NBR \n");
				sql.append("and sb.BOOK_TIME<DATE_ADD(SYSDATE(),INTERVAL 1 HOUR) \n");
				sql.append("and tas.RUN_STS='H' \n");
				
						sql.append("and tas.EXT_WO_NBR>? \n");
				sql.append("and tas.SHARDING_ID = ? \n");
				sql.append("ORDER BY tas.EXT_WO_NBR \n");
				sql.append("LIMIT ?");


				// 为了便于事务控制每个线程得到自己的数据库连接
				conn = ConnectionFactory.createConnection();
				log.info("[thread " + this.threadIndex + "] connect db success");
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, baseTimer.THE_LAST_MAX_ID + "");
				ps.setString(2, baseTimer.localNetIds);
				ps.setInt(3, baseTimer.getMaxCountPerCycle());
				log.debug("sql:["+sql.toString()+"]");
				rs = ps.executeQuery();

				while (rs.next()) {
					vo = new TaskOrderMVO();
					vo.setToNbr(rs.getString("to_nbr"));
					vo.setSpecServId(rs.getString("spec_serv_id"));
					vo.setToStaffId(rs.getString("to_staff_id"));
					vo.setShardingId(rs.getString("sharding_id"));
					vo.setExtWoNbr(rs.getString("ext_wo_nbr"));
					// 取摸然后入集合
					modNum = (Long.parseLong(vo.getExtWoNbr()))
							% (baseTimer.threadNumber);

					nodNumInt = Integer.parseInt(new Long(modNum).toString());

					if (baseTimer.bizDataList[nodNumInt] == null) {
						log.debug("[threadNumber:" + threadIndex
								+ "初始化LinkedList");
						baseTimer.bizDataList[nodNumInt] = new LinkedList();
					}
					baseTimer.THE_LAST_MAX_ID = Long
							.parseLong(vo.getExtWoNbr());

					if (baseTimer.containInLastRecord(nodNumInt,
							vo.getExtWoNbr())) {
						log.debug("[thread " + this.threadIndex
								+ "] 查询出 MSG_ID= " + vo.getExtWoNbr() + " 与"
								+ nodNumInt + "号辅助线程最后处理的记录重复，不加入辅助线程的队列");
					} else {
						synchronized (baseTimer.bizDataList[nodNumInt]) {
							((LinkedList) baseTimer.bizDataList[nodNumInt])
									.addLast(vo);
						}
					}
					queryDataRows = queryDataRows + 1;
				}
			}

			log.info("[thread " + this.threadIndex + "] 查询TO end ... ");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("[thread " + this.threadIndex + "] 查询task_order表错误："
					+ e.toString());
			log.error("[thread " + this.threadIndex + "] 初始化bizList时出现错误");
			throw new AppException("", "");
		} finally {
			JdbcUtil.close(ps);
			if (conn != null) {
				ConnectionFactory.closeConnection();
				log.info("[thread " + this.threadIndex
						+ "] close db connection");
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void processBizData() throws AppException, SysException {
		if (baseTimer.bizDataList[threadIndex] == null) {
			return;
		}
		if (baseTimer.bizDataList[threadIndex].size() < baseTimer
				.getMinCountPerCycle()) {
			return;
		}
		String solrType = this.baseTimer.getSolrType();
		log.debug("[thread " + this.threadIndex + "] 处理数据 begin........");
		LinkedList threadList = (LinkedList) baseTimer.bizDataList[threadIndex];
		int recordCount = 1;
		SolrDocument vo = null;
		TaskOrderMVO taskOrder = null;
		while (!threadList.isEmpty()) {
			String toNbr = null;
			String extWoNbr = null;
			String specServId = null;
			String toStaffId = null;
			String shardingId = null;

			// 扫solr
			if ("solr".equals(this.baseTimer.scanType)) {
				vo = (SolrDocument) threadList.getFirst();
				toNbr = (String) vo.get("TO_NBR");
				extWoNbr = (String) vo.get("EXT_WO_NBR");
				specServId = (String) vo.get("SPEC_SERV_ID");
				toStaffId = (String) vo.get("TO_STAFF_ID");
				shardingId = (String) vo.get("SHARDING_ID");
			} else {
				taskOrder = (TaskOrderMVO) threadList.getFirst();
				toNbr = taskOrder.getToNbr();
				extWoNbr = taskOrder.getExtWoNbr();
				specServId = taskOrder.getSpecServId();
				toStaffId = taskOrder.getToStaffId();
				shardingId = taskOrder.getShardingId();
			}

			baseTimer.addCurRecord(threadIndex, toNbr);
			log.info("[thread " + this.threadIndex + "] 开始处理 task_order "
					+ toNbr + ", co_nbr=" + extWoNbr + "记录！");
			Connection conn = null;
			PreparedStatement ps = null;
			HttpSolrClient client = null;
			CloudSolrClient cloudSolrServer = null;
			try {

				String runSts = "";
				if (!StringUtil.isBlank(toStaffId)
						&& "016D23038401F540815FF71B4051F81E,FE33B5031312408ECA4F2A2AA3CFAEA9,FE33B5031312408ECA4F2A2AA3CFAEA8,SC2003,SC2008,SP3095"
								.indexOf(specServId) < 0) {
					runSts = "P";
				} else {
					runSts = "D";
				}

				/**
				 * 先更新mysql，再更新solr
				 */
				StringBuffer sql = new StringBuffer();
				sql.append("update task_order set run_sts =? where to_nbr= ? and sharding_id=?");
				conn = ConnectionFactory.createConnection();
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, runSts);
				ps.setString(2, toNbr);
				ps.setString(3, shardingId);
				ps.executeUpdate();

				/**
				 * 分为扫solr和扫mysql
				 */
				// 扫solr
				if ("solr".equals(this.baseTimer.scanType)) {
					// 扫solr
					vo.put("RUN_STS", runSts);
					if ("solr".equals(solrType)) {
						client = new HttpSolrClient(this.baseTimer.getSolrUrl()
								+ "TO");
						client.add(this.buildSolrInputDoc(vo));
						client.commit();
					} else {
						cloudSolrServer = new CloudSolrClient(
								this.baseTimer.getZkHost());
						cloudSolrServer.setDefaultCollection("to");
						cloudSolrServer.setZkClientTimeout(5000);
						cloudSolrServer.setZkConnectTimeout(5000);
						cloudSolrServer.connect();
						cloudSolrServer.add(this.buildSolrInputDoc(vo));
						cloudSolrServer.commit();
					}
				} else {
					SolrQuery query = new SolrQuery();
					query.setQuery("TO_NBR:" + toNbr);
					QueryResponse response = null;
					if ("solr".equals(solrType)) {
						client = new HttpSolrClient(this.baseTimer.getSolrUrl()
								+ "TO");
						response = client.query(query);
						SolrDocumentList list = response.getResults();
						if (list != null && list.size() > 0) {
							SolrDocument solrDocument = list.get(0);
							solrDocument.removeFields("_version_");
							solrDocument.setField("RUN_STS", runSts);
							client.add(this.buildSolrInputDoc(solrDocument));
							client.commit();
						}

					} else {
						cloudSolrServer = new CloudSolrClient(
								this.baseTimer.getZkHost());
						cloudSolrServer.setDefaultCollection("to");
						cloudSolrServer.setZkClientTimeout(5000);
						cloudSolrServer.setZkConnectTimeout(5000);
						cloudSolrServer.connect();

						response = cloudSolrServer.query(query);
						SolrDocumentList list = response.getResults();
						if (list != null && list.size() > 0) {
							SolrDocument solrDocument = list.get(0);
							solrDocument.removeFields("_version_");
							solrDocument.setField("RUN_STS", runSts);
							cloudSolrServer.add(this
									.buildSolrInputDoc(solrDocument));
							cloudSolrServer.commit();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (client != null) {
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (cloudSolrServer != null) {
					try {
						cloudSolrServer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
					log.info("[thread " + this.threadIndex
							+ "] close db connection");
				}
				// 对于成功，则主线程不会再次扫描到该数据，如果失败则此时lastRecord已经为null，因此也能将失败的数据加入到队列中进行重新执行
				baseTimer.addCurRecord(threadIndex, null);
				synchronized (baseTimer.bizDataList[threadIndex]) {
					threadList.removeFirst();
				}
			}

			recordCount++;
		}
		log.debug("[thread " + this.threadIndex + "] 处理数据 end........");

	}

	public String dataToSolrDate(String dateVal) {
		String res = " ";
		String va[] = dateVal.split(" ");
		if (va.length == 1) {
			res = va[0] + "T" + "00:00:00Z";
		} else {
			res = va[0] + "T" + va[1] + "Z";
		}
		return res;
	}

	public SolrInputDocument buildSolrInputDoc(SolrDocument colMap) {
		SolrInputDocument doc = new SolrInputDocument();
		for (Entry<String, Object> entry : colMap.entrySet()) {
			if (entry.getValue() != null
					&& !StringUtil.isBlank((entry.getValue()).toString())
					&& !"null".equals((entry.getValue()).toString())) {
				doc.addField(entry.getKey(), entry.getValue());
			}
		}
		return doc;
	}

	public HashMap changeSolrDoc2HashMap(SolrDocument solrDoc) {
		HashMap<String, String> returnMap = new HashMap<String, String>();

		Set<Map.Entry<String, Object>> solrDocEn = solrDoc.entrySet();
		for (Map.Entry<String, Object> doc : solrDocEn) {
			returnMap.put(doc.getKey(), (String) doc.getValue());
		}

		return returnMap;
	}

	public HashMap<String, String> changeSolrDoc2HashMap(Map map) {
		HashMap<String, String> returnMap = new HashMap<String, String>();

		Set<Map.Entry<String, Object>> solrDocEn = map.entrySet();
		for (Map.Entry<String, Object> doc : solrDocEn) {
			returnMap.put(doc.getKey(), (String) doc.getValue());
		}

		return returnMap;
	}

}
