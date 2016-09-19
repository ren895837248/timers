package com.cattsoft.timers.devicesynctimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

import javax.ejb.Local;

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
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.sun.org.apache.bcel.internal.generic.BASTORE;

public class DeviceSyncThread extends BaseThread {

	public DeviceSyncThread(int modRemainder) {
		this.threadIndex = modRemainder;
		log = Logger.getLogger(DeviceSyncThread.class);
	}

	//当前从文件读取的字节数
	long skipBytes = 0;
	long lastProduceTime = 0;
	@SuppressWarnings("unchecked")
	@Override
	public void initBizData() throws AppException, SysException {
		/*
		 * 每10分钟处理一次
		 */
		if(new Date().getTime()<(lastProduceTime+10*60*1000)&&skipBytes==0){//加10分钟
			//如果本次执行时间为上次执行开始之后的10分钟内，并且上次扫描已经结束或者尚未开始扫描，则不知执行。
			return;
		}
		
		lastProduceTime = new Date().getTime();
		String localPath = "C:\\Users\\yangshan\\Desktop\\temp.txt";
		File tempSourcefile = new File(localPath);
		while(true){
			
			
			
			int count = 0;
			int modNum;
			int nodNumInt;
			
			Session session =null;
			ChannelSftp channelSftp = null;
			BufferedReader br = null;
			String ip = "192.168.1.90";
			String username = "root";
			String passWrod = "ys";
			StringBuffer path = new StringBuffer("/home/html/");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			path.append("DAY_ONUINST_").append(sdf.format(new Date())).append("0001.txt");
			
			if(skipBytes==0){
				cacheTempFile(ip,username,passWrod,path.toString(),localPath);
			}
			
			try {
				InputStream in = null;
				/**
				 * 如果缓存了本地文件，则读取本地文件。
				 */
				log.info("[thread " + this.threadIndex +"] 判断是否缓存文件到本地..");
				if(tempSourcefile.exists()){
					
					log.info("[thread " + this.threadIndex +"] 存在本地文件，读取本地文件..");
					in = new FileInputStream(tempSourcefile);
				}else{
					
					log.info("[thread " + this.threadIndex +"]不存在本地文件 开始连接远程服务器..ip:"+ip);
					JSch jsch = new JSch();
					session = jsch.getSession(username, ip,22);//stfp默认端口为22
					session.setConfig("StrictHostKeyChecking", "no");  
					session.setPassword(passWrod);
					session.connect();
					
					channelSftp =(ChannelSftp) session.openChannel("sftp");
					channelSftp.connect();
					in = channelSftp.get(path.toString());
				}
				
				
				
				br = new BufferedReader(
						new InputStreamReader(in,"GB2312"));
				String line = "";
				if(skipBytes>0){
					br.skip(skipBytes);
				}
				
				while(StringUtils.isNotBlank(line = br.readLine())){
					/*if(count == 0){
						System.out.println(line);
					}*/
					/**
					 * 每次只从文件里头读取限定的条数，防止内存溢出。
					 */
					if(count%baseTimer.getMaxCountPerCycle() == 0 &&count!=0){//每次读取baseTimer.getMaxCountPerCycle()条记录
						//System.out.println(line);
						count = 0;
						break;
					}
					String[] devicesInfo = line.split(",");
					String sn = devicesInfo[0];
					String model = devicesInfo[1];
					String type = devicesInfo[2];
					String resType = devicesInfo[3];
					String protocol = devicesInfo[4];
					String factory = devicesInfo[5];
					String barCode = devicesInfo[6];
					String devSts = devicesInfo[7];
					String deptId = devicesInfo[8];
					String deptName = devicesInfo[9];
					//String offerType = devicesInfo[10];
					String localNetId = devicesInfo[10];
					String createDate = devicesInfo[11];
					
					
					/*
					 *TODO   将数据封装为对象
					 *
					 */
					
					modNum = count% baseTimer.threadNumber;
					count++;
					nodNumInt = Integer.parseInt(new Long(modNum).toString());

					if (baseTimer.bizDataList[nodNumInt] == null) {
						log.debug("[threadNumber:" + threadIndex
								+ "初始化LinkedList");
						baseTimer.bizDataList[nodNumInt] = new LinkedList();
					}
					skipBytes += (line.length()+2);//每行会有\n\r

					if (baseTimer.containInLastRecord(nodNumInt,sn)) {
						log.debug("[thread " + this.threadIndex
								+ "] 查询出 MSG_ID= " + sn + " 与"
								+ nodNumInt + "号辅助线程最后处理的记录重复，不加入辅助线程的队列");
					} else {
						synchronized (baseTimer.bizDataList[nodNumInt]) {
							((LinkedList) baseTimer.bizDataList[nodNumInt])
									.addLast(null);
						}
					}
					
					
					synchronized (baseTimer.bizDataList[nodNumInt]) {
						((LinkedList) baseTimer.bizDataList[nodNumInt]).addLast(null);//TODO
					}
					queryDataRows = queryDataRows + 1;
					
				}
				
				/**
				 * 结束一次同步数据处理，初始化相应参数。
				 */
				log.info("[thread " + this.threadIndex
								+ "] 完成一次数据同步处理.初始化参数.进行休眠");
				if(line == null){skipBytes = 0;count=0;tempSourcefile.delete();break;}
				
				log.info("[thread " + this.threadIndex
						+ "] 查询INTER_MSG_CENTER Begin ... ");
				log.info("[thread " + this.threadIndex + "] 查询TO end ... ");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("[thread " + this.threadIndex + "] 查询数据错误："
						+ e.toString());
				log.error("[thread " + this.threadIndex + "] 初始化bizList时出现错误");
				throw new AppException("", "");
			} finally {
				if(br!=null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(channelSftp!=null){
					channelSftp.disconnect();
				}
				if(session!=null){
					session.disconnect();
				}
				
				
			}
			
			
			//是否进行下一次循环
			while(true && skipBytes>0){//说明正在扫描中
				boolean doNextLoopflag = true;
				for(int i=0;i<baseTimer.bizDataList.length;i++){
					if(baseTimer.bizDataList[i]!=null && baseTimer.bizDataList[i].size()>0){
						doNextLoopflag = false;
					}
				}
				
				if(!doNextLoopflag){//如果存在未处理完的则进行休眠
					try {
						Thread.sleep(baseTimer.getTimeFreeSleep());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					break;
				}
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
		log.debug("[thread " + this.threadIndex + "] 处理数据 begin........");
		LinkedList threadList = (LinkedList) baseTimer.bizDataList[threadIndex];
		int recordCount = 1;
		while (!threadList.isEmpty()) {

			baseTimer.addCurRecord(threadIndex, null);
			log.info("[thread " + this.threadIndex + "] 开始处理记录！");
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				conn = ConnectionFactory.createConnection();
				
				String sn = "";
				String devSts = "";
				ps = conn.prepareStatement("select * from TERMINAL_DEVICE where sn = ?");
				ps.setString(1,sn);
				rs = ps.executeQuery();
				if(rs.next()){
					String devStsCps = rs.getString("DEV_STS");
					/**
					 * 1）待同步的数据状态为空闲和重用，并且状态与库中状态不一致，则更新。
					 * 2）对于其他状态，以库中为准。
					 * 3）对于相同的状态，不做任何处理。
					 */
					if(",10,21,".indexOf(","+devSts+",")!=-1&&!devSts.equals(devStsCps)){
						log.info("[thread " + 
								this.threadIndex + "] 待处理记录设备状态：【"+devSts+"】,与库中设备状态：【"+devStsCps+"】,为空闲或者重用状态，且状态不一致，进行更新");
						updateDevice(conn);
						if("21".equals(devSts)){//重用状态，还需同步DEVICE_RECLAIM表
							//TODO
						}
					}
				}else{
					/**
					 * 不存在记录，直接插入
					 */
					insertDevice(conn);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
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
	
	
	
	
	
	private int insertDevice(Connection conn){
		int returnInt = 0;
		log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]  不存在设备记录，直接将记录插入...");
		StringBuffer insertSql = new StringBuffer("insert into TERMINAL_DEVICE ");
		
		insertSql.append(" ");
		PreparedStatement ps =null;
		try {
			ps = conn.prepareStatement(insertSql.toString());
			returnInt = ps.executeUpdate();
			log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]不存在设备记录，插入成功...");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(ps!=null){
				JdbcUtil.close(ps);
			}
		}
		
		return 0;
	}
	
	private int updateDevice(Connection conn){
		int returnInt = 0;
		log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]  不存在设备记录，直接将记录插入...");
		StringBuffer updateSql = new StringBuffer("update into TERMINAL_DEVICE set");
		
		updateSql.append(" WHERE 1=1 ");
		updateSql.append(" AND SN = ?");
		updateSql.append(" AND sharding_id = ?");
		PreparedStatement ps =null;
		try {
			ps = conn.prepareStatement(updateSql.toString());
			returnInt = ps.executeUpdate();
			log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]不存在设备记录，插入成功...");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(ps!=null){
				JdbcUtil.close(ps);
			}
		}
		
		
		return 0;
	}
	
	private int updateDeviceReclaim(Connection conn){
		int returnInt = 0;
		log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]  状态为21/重用,更新device_reclaim...");
		StringBuffer updateSql = new StringBuffer("update into device_reclaim set");
		updateSql.append(" RECLAIM_STS = ? ");
		updateSql.append(" WHERE 1=1 ");
		updateSql.append(" AND SN = ?");
		updateSql.append(" AND sharding_id = ?");
		PreparedStatement ps =null;
		try {
			ps = conn.prepareStatement(updateSql.toString());
			ps.setString(1,"21");
			ps.setString(2, sn);
			
			returnInt = ps.executeUpdate();
			log.info("[thread " + this.threadIndex + "] ,sn/mac:["+sn+"]不存在设备记录，插入成功...");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(ps!=null){
				JdbcUtil.close(ps);
			}
		}
		
		
		return 0;
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

	public static void main(String[] args) {
		BufferedReader br = null;
		long lastbyte = 0;
		long t = System.currentTimeMillis();
		while(true){
			try {
				
				long count = 0l;
				
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream("C:\\Users\\yangshan\\Desktop\\DAY_ONUINST201607210001.txt"),"GB2312"));
				
				String line = "";
				if(lastbyte>0){
					br.skip(lastbyte);
				}
				while((line = br.readLine())!=null){
					if(count == 0){
						System.out.println(line);
					}
					if(count%5000 == 0&&count != 0){
						System.out.println(line);
						count = 0;
						break;
					}
					lastbyte += (line.length()+2);
					count++;
				}
				if(line == null){
					lastbyte = 0;
					break;
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}
		
		
		System.out.println("用时："+(System.currentTimeMillis() - t));
		
	}
	
	
	private void cacheTempFile(String ip,String username,String pwd,String path,String loaclPath){
		log.info("[thread " + this.threadIndex +"] 开始连接远程服务器..ip:"+ip+"，缓存文件到本地");
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp channelSftp = null;
		BufferedReader br = null;
		PrintWriter out = null;
		
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			session = jsch.getSession(username, ip,22);//stfp默认端口为22
			session.setConfig("StrictHostKeyChecking", "no");  
			session.setPassword(pwd);
			session.connect();
			
			channelSftp =(ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			/*br = new BufferedReader(
					new InputStreamReader(channelSftp.get(path.toString()),"GB2312"));
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(loaclPath),"GB2312"));*/
			in = channelSftp.get(path.toString());
			
			byte[] b =new byte[1024];
			int len = 0;
			fos = new FileOutputStream(loaclPath);
			while((len = in.read(b))!= -1){
				fos.write(b,0, len);
			}
			
			
			/*String line = "";
			char[] enter = {'\r'};
			while((line = br.readLine())!=null){
				out.write(line);
				out.write(enter);
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(out!=null){
				out.close();
			}
			
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in !=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(channelSftp!=null){
				channelSftp.disconnect();
			}
			
			if(session!=null){
				session.disconnect();
			}
			
		}
		
		
	}
	
}
