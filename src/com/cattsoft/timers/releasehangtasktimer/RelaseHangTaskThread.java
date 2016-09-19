package com.cattsoft.timers.releasehangtasktimer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
								+ "to");
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
						"\nselect tas.area_id,tas.to_nbr,tas.wo_nbr ,tas.spec_serv_id,tas.to_staff_id,tas.sharding_id,tas.ext_wo_nbr from task_order tas,so_book sb \n");

				sql.append("where tas.EXT_WO_NBR = sb.EXT_SO_NBR \n");
				//sql.append("and so.SO_NBR = sb.SO_NBR \n");
				sql.append("and sb.STS = 'A' \n");
				sql.append("and sb.BOOK_TIME<DATE_ADD(SYSDATE(),INTERVAL ? MINUTE) \n");
				sql.append("and tas.RUN_STS='H' \n");
				
						sql.append("and tas.EXT_WO_NBR>? \n");
				sql.append("and tas.SHARDING_ID = ? \n");
				sql.append("ORDER BY tas.EXT_WO_NBR \n");
				sql.append("LIMIT ?");

				
				// 为了便于事务控制每个线程得到自己的数据库连接
				conn = ConnectionFactory.createConnection();
				log.info("[thread " + this.threadIndex + "] connect db success");
				ps = conn.prepareStatement(sql.toString());
				ps.setInt(1,baseTimer.preTimeLimit);
				ps.setString(2, baseTimer.THE_LAST_MAX_ID + "");
				ps.setString(3, baseTimer.localNetIds);
				ps.setInt(4, baseTimer.getMaxCountPerCycle());
				log.debug("sql:["+sql.toString()+"]");
				rs = ps.executeQuery();

				while (rs.next()) {
					vo = new TaskOrderMVO();
					vo.setToNbr(rs.getString("to_nbr"));
					vo.setSpecServId(rs.getString("spec_serv_id"));
					vo.setToStaffId(rs.getString("to_staff_id"));
					vo.setShardingId(rs.getString("sharding_id"));
					vo.setExtWoNbr(rs.getString("ext_wo_nbr"));
					vo.setWoNbr(rs.getString("wo_nbr"));
					vo.setAreaId(rs.getString("area_id"));
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
			String woNbr = null;
			String areaId = null;
			// 扫solr
			if ("solr".equals(this.baseTimer.scanType)) {
				vo = (SolrDocument) threadList.getFirst();
				toNbr = (String) vo.get("TO_NBR");
				extWoNbr = (String) vo.get("EXT_WO_NBR");
				specServId = (String) vo.get("SPEC_SERV_ID");
				toStaffId = (String) vo.get("TO_STAFF_ID");
				shardingId = (String) vo.get("SHARDING_ID");
				woNbr = (String) vo.get("WO_NBR");
				areaId = (String)vo.get("AREA_ID");
			} else {
				taskOrder = (TaskOrderMVO) threadList.getFirst();
				toNbr = taskOrder.getToNbr();
				extWoNbr = taskOrder.getExtWoNbr();
				specServId = taskOrder.getSpecServId();
				toStaffId = taskOrder.getToStaffId();
				shardingId = taskOrder.getShardingId();
				woNbr = taskOrder.getWoNbr();
				areaId = taskOrder.getAreaId();
			}

			if(StringUtils.isNotBlank(woNbr)){
				woNbr = woNbr.split(",")[0];
			}
			
			baseTimer.addCurRecord(threadIndex, toNbr);
			log.info("[thread " + this.threadIndex + "] 开始处理 task_order "
					+ toNbr + ", co_nbr=" + extWoNbr + "记录！");
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			Connection iomConn = null;
			CallableStatement iomProc = null;
			
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

				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "判断任务单原runsts："+runSts);
				/**
				 * 先更新mysql，再更新solr
				 */
				
				StringBuffer sql = new StringBuffer();
				sql.append("\nupdate task_order set \nrun_sts =? ,\nrun_sts_date = SYSDATE() \nwhere \nto_nbr= ? \nand sharding_id=?");
				conn = ConnectionFactory.createConnection();
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "更新run_sts状态，执行sql：["+sql.toString()+"]");
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, runSts);
				ps.setString(2, toNbr);
				ps.setString(3, shardingId);
				ps.executeUpdate();
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "更新run_sts状态成功");
				
				sql = new StringBuffer();
				sql.append("\n select ext_wo_nbr from wo \nwhere wo.wo_nbr = ? \nand sharding_id=?");
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "查询iom工单号，执行sql：["+sql.toString()+"]");
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, woNbr);
				ps.setString(2, shardingId);
				rs = ps.executeQuery();
				while(rs.next()){
					woNbr = rs.getString("ext_wo_nbr");
				}
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "查询iom工单号,查询出工单号为：["+woNbr+"]");
				
				/**
				 * 分为扫solr和扫mysql
				 */
				// 扫solr
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "开始执行更新solr操作..");
				if ("solr".equals(this.baseTimer.scanType)) {
					// 扫solr
					vo.put("RUN_STS", runSts);
					if ("solr".equals(solrType)) {
						client = new HttpSolrClient(this.baseTimer.getSolrUrl()
								+ "to");
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
					String solrQuery = "TO_NBR:"+toNbr;
					query.setQuery(solrQuery);
					log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，查询相关记录："+solrQuery);
					QueryResponse response = null;
					log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，判断solr连接模式："+solrType);
					if ("solr".equals(solrType)) {
						log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，建立solr连接："+this.baseTimer.getSolrUrl()+ "to");
						client = new HttpSolrClient(this.baseTimer.getSolrUrl()
								+ "to");
						log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，执行查询");
						response = client.query(query);
						SolrDocumentList list = response.getResults();
						log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，查询结果返回记录数：["+list.size()+"]");
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
						log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，查询结果返回记录数：["+list.size()+"]");
						if (list != null && list.size() > 0) {
							SolrDocument solrDocument = list.get(0);
							solrDocument.removeFields("_version_");
							solrDocument.setField("RUN_STS", runSts);
							log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr，将该条记录RUN_STS==>"+runSts);
							cloudSolrServer.add(this
									.buildSolrInputDoc(solrDocument));
							cloudSolrServer.commit();
							log.info("[thread " + this.threadIndex + "] 处理 task_order ,更新solr完成，提交确认");
						}
					}
				}
				log.info("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "执行更新solr操作成功");
				
				log.info("[thread " + this.threadIndex + "] 处理 task_order ，解挂成功，记录tohandle");
				
				
				sql = new StringBuffer();
				sql.append("INSERT INTO to_handle (`TO_HANDLE_ID`, `AREA_ID`, `TO_NBR`, `HANDLE_TYPE_ID`, `HANDLE_DATE`, `STAFF_ID`,  `SHARDING_ID`, `ARCHIVE_BASE_DATE`, `REMARKS`)"); 
				sql.append(" VALUES (?, ?, ?, '100010', SYSDATE(), ?, ?, SYSDATE(), '任务单解挂成功');");
				log.info("插入to_handle,sql:"+sql.toString());
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, UUID.randomUUID().toString().replaceAll("-",""));
				ps.setString(2, areaId);
				ps.setString(3, toNbr);
				ps.setString(4, toStaffId);
				ps.setString(5, shardingId);
				ps.executeUpdate();
				log.info("[thread " + this.threadIndex + "] 处理 task_order ，解挂成功，记录tohandle成功..");
				
				conn.commit();
				
				/**
				 * 解挂成功，通过调用短信接口，通知供单人。
				 */
				if("P".equals(runSts)){
					log.info("[thread " + this.threadIndex + "] 处理 task_order "
							+ toNbr + "解挂成功，解挂之后调用iom接口，发送短信。");
					
					
					
					iomConn = this.baseTimer.getIomDs().getConnection();
					iomConn.setAutoCommit(false);
					iomProc = iomConn.prepareCall("{ call P_INSERT_SMS_NOTICE_INFO(?,?,?,?,?,?,?,?) }");
					iomProc.setString(1, shardingId);
					iomProc.setString(2, areaId);
					iomProc.setString(3, toStaffId);
					iomProc.setString(4, "");
					iomProc.setString(5, "7029");
					iomProc.setString(6, woNbr);
					iomProc.registerOutParameter(7, oracle.jdbc.OracleTypes.VARCHAR);
					iomProc.registerOutParameter(8, oracle.jdbc.OracleTypes.VARCHAR);
					log.debug("调用iom存储，发送短信，调用存储参数依次为："+shardingId+","+areaId+","+toStaffId+","+""+","+"7029"+","+woNbr);
					iomProc.execute();
					iomConn.commit();
					
					log.info("[thread " + this.threadIndex + "] 处理 task_order "
							+ toNbr + "解挂成功，解挂之后调用iom接口，发送成功。");
				}else{
					log.info("[thread " + this.threadIndex + "] 处理 task_order "
							+ toNbr + "解挂成功，任务单运行状态为D,不能发送短信通知施工人。");
				}
				
			} catch (Exception e) {
				log.debug("[thread " + this.threadIndex + "] 处理 task_order "
						+ toNbr + "发生异常，异常信息："+e.getMessage());
				e.printStackTrace();
				
				try {
					log.info("[thread " + this.threadIndex + "] 处理 task_order "
							+ toNbr + "发生异常，回滚数据库操作..");
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				if(iomConn!=null){
					try {
						iomConn.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
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
				
				if(iomProc!=null){
					try {
						iomProc.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if(iomConn!=null){
					try {
						iomConn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
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
