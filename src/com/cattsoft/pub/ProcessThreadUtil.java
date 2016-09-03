package com.cattsoft.pub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.cattsoft.bm.vo.ProcessStatusSVO;
import com.cattsoft.pub.dao.Sql;
import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.DataCacheException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.pub.util.JdbcUtil;
import com.cattsoft.pub.util.LogUtil;
import com.cattsoft.pub.util.StringUtil;
import com.cattsoft.sm.vo.SysAreaConfigSVO;
import com.cattsoft.sm.vo.SysConfigSVO;
import com.cattsoft.timers.BaseTimer;
import com.cattsoft.timers.IBaseDBCommand;

public class ProcessThreadUtil {
	
	public static Logger log = Logger.getLogger(ProcessThreadUtil.class);

	/**
	 * �������״̬��PROCESS_STATUS�еġ����̱�š��͡������߳������� ͬʱ��ʼ���߳�״̬��THREAD_STATUS
	 * ע���ڷ����з����쳣ʱ�������׳���ֱ��return���ɣ�����Ӱ��Timer��������
	 * 
	 * @param InitialTimer
	 *            initialTimer
	 * @param String
	 *            application ��ʶ
	 * @param String
	 *            processNo ���̱��
	 * @param Integer
	 *            threadNumber �����߳�����
	 * @return String ���ؽ���ID
	 * @throws SysException 
	 */
	public static String updateProcessStatus(BaseTimer baseTimer,long throughputCycle) throws AppException, SysException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String serverApplicationId = null;
		Sql sql = null;
		long throughputTotal = 0;
		String processNo;
		try {
			log.debug("updateProcessStatus begin!");
			// �ж���������Ƿ�Ϸ�
			processNo = getProcessId();// ϵͳ����ID��
			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();
			// 2.��ȡ��̨Ӧ�õ�ID
			sql = new Sql(
					"SELECT SERVER_APPLICATION_ID FROM SERVER_APPLICATION WHERE APPLICATION_CODE=:applicationCode AND STS = :sts");
			sql.setString("applicationCode", baseTimer.getTimerName());
			sql.setString("sts", "A");
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			while (rs != null && rs.next()){
				serverApplicationId = rs.getString("SERVER_APPLICATION_ID");
			}
			sql = new Sql("SELECT A.THROUGHPUT_TOTAL FROM PROCESS_STATUS A ");
			sql.append(" WHERE SERVER_APPLICATION_ID=:serverApplicationId AND PROCESS_NO = :processNo ");
			sql.append(" AND STS = 'A' and PROCESS_CODE = :processCode and ROWNUM=1");
			sql.setLong("serverApplicationId", serverApplicationId);
			sql.setString("processNo", processNo);
			sql.setString("processCode", baseTimer.getTimerProcessCode());
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			if (rs != null && rs.next()){				
				throughputTotal = rs.getLong("THROUGHPUT_TOTAL");
				log.debug("�鵽�������ǣ�" + throughputTotal);
				throughputTotal = throughputTotal + throughputCycle;
				log.debug("��������Ϊ��" + throughputTotal);
			}
			// 3.����,��ǰ�Ѿ�����Timer�������,����Timer�������
			sql = new Sql(
					"UPDATE PROCESS_STATUS SET THROUGHPUT_CYCLE = :THROUGHPUT_CYCLE,THROUGHPUT_TOTAL = :THROUGHPUT_TOTAL,STS_DATE = SYSDATE");
			sql.append(" WHERE SERVER_APPLICATION_ID=:serverApplicationId AND PROCESS_NO = :processNo ");
			sql.append(" AND STS =:sts and PROCESS_CODE = :processCode and ROWNUM=1");
			sql.setLong("THROUGHPUT_CYCLE", new Long(throughputCycle));
			sql.setLong("THROUGHPUT_TOTAL", new Long(throughputTotal));
			sql.setString("processNo", processNo);
			sql.setString("processCode", baseTimer.getTimerProcessCode());
			sql.setLong("serverApplicationId", serverApplicationId);
			sql.setString("sts", "A");			
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			int result = ps.executeUpdate();
			log.debug("updateProcessStatus end!");
			/*if (result == 0) {
				log.debug("���ݿ��н���������,��������ӽ��̼�¼��PROCESS_STATUS");
				throw new AppException("", "���ݿ��н���������,��������ӽ��̼�¼��PROCESS_STATUS");
			} else
				for (int i = 0; i < threadNumber; i++) {
					// ������MaxIdȡSEQʱ��ͨ��ConnectionFactory���»�ȡ���ӣ�����ConnectionFactory��Timer����ʱΪ�գ����Բ�ʹ��
					sql = new Sql("select " + SysConstants.THREAD_STATUS_ID_SEQ
							+ "_SEQ.nextval from dual");
					ps = conn.prepareStatement(sql.getSql());
					sql.fillParams(ps);
					sql.log(ProcessThreadUtil.class);
					rs = ps.executeQuery();
					Long SEQ = null;
					if (rs.next()) {
						SEQ = new Long(rs.getLong(1));
					}
					sql = new Sql(
							"INSERT INTO THREAD_STATUS(THREAD_STATUS_ID,SERVER_APPLICATION_ID,PROCESS_NO,PROCESS_CODE,THREAD_NO,THROUGHPUT_CYCLE,THROUGHPUT_TOTAL,STS,STS_DATE,REMARKS)");
					sql
							.append("VALUES(:threadStatusId,:serverApplicationId,:processNo,:processCode,:threadNo,:throughputCycle,:throughputTotal,:sts,SYSDATE,:remarks)");
					sql.setLong("threadStatusId", SEQ);
					sql.setLong("serverApplicationId", serverApplicationId);
					sql.setString("processNo", processNo);
					sql.setString("processCode", processCode);
					sql.setLong("threadNo", Integer.toString(i));
					sql.setLong("throughputCycle", new String("0"));
					sql.setLong("throughputTotal", new String("0"));
					sql.setString("sts", "A");
					sql.setString("remarks", remarks[i]);
					ps = conn.prepareStatement(sql.getSql());
					sql.fillParams(ps);
					sql.log(ProcessThreadUtil.class);
					ps.executeUpdate();
				}*/			
		} catch (Exception e) {
			LogUtil.logExceptionStackTrace(log, e);
			throw new AppException("", e.getMessage());
		} finally {
			JdbcUtil.close(ps);
			if (conn != null) {
				ConnectionFactory.closeConnection();
			}
			
		}
		return processNo;
	}

	public static void writeProcessInfo(BaseTimer baseTimer)
			throws AppException, SysException {
		Connection cn = null;
		PreparedStatement ps = null;

		try {
			/**
			 * д�����״̬��Ϣ
			 */
			cn = ConnectionFactory.createConnection();
			String psID = getSequenceNextVal(SysConstants.PROCESS_STATUS_ID_SEQ);
			
			Sql sql = new Sql("INSERT INTO PROCESS_STATUS");
			sql.append("(PROCESS_STATUS_ID,SERVER_APPLICATION_ID,PROCESS_NO,PROCESS_CODE,HOST_IP,THREAD_NUMBER,THROUGHPUT_CYCLE,THROUGHPUT_TOTAL,STS,STS_DATE,REMARKS)");
			sql.append(" SELECT :processStatusId, SA.SERVER_APPLICATION_ID, :processNo,:processCode, :hostIP, :threadNumber, :throughputCycle, :throughputTotal, 'A', SYSDATE, :remarks");			
			sql.append(" FROM SERVER_APPLICATION SA");
			sql.append(" WHERE SA.APPLICATION_CODE = :applicationCode");
			sql.append(" AND SA.STS = :sts");
			sql.append(" AND ROWNUM < 2");
			
			sql.setLong("processStatusId", new Long(psID));			
			sql.setString("processNo", baseTimer.getTimerProcessId());			
			sql.setString("processCode", baseTimer.getTimerProcessCode());
			sql.setString("hostIP", baseTimer.getTimerProcessHost());
			sql.setInteger("threadNumber", new Integer(baseTimer.threadNumber));
			sql.setString("applicationCode", baseTimer.getTimerName().substring(0,baseTimer.getTimerName().length()-5));
			sql.setInteger("throughputCycle", new Integer("0"));
			sql.setInteger("throughputTotal", new Integer("0"));
			sql.setString("sts", SysConstants.STS_IN_USE);
			sql.setString("remarks", "");
			sql.log(ProcessThreadUtil.class);
			ps = cn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			int resultNum = 0;
			try{
				resultNum = ps.executeUpdate();
			}catch(Exception sqlex){
				LogUtil.logExceptionStackTrace(log, sqlex);
				throw new SysException("1000081","���½�����Ϣ�쳣��",sqlex);
			}			
			
			if (1 == resultNum) {
				JdbcUtil.close(ps);
				/**
				 * ��¼�߳�״̬��Ϣ
				 */
				sql = new Sql("INSERT INTO THREAD_STATUS");
				sql.append("(THREAD_STATUS_ID,PROCESS_STATUS_ID,SERVER_APPLICATION_ID,PROCESS_NO, THREAD_NO,THROUGHPUT_CYCLE,THROUGHPUT_TOTAL,STS,STS_DATE,REMARKS)");
				sql.append(" SELECT THREAD_STATUS_ID_SEQ.NEXTVAL,:processStatusId,PS.SERVER_APPLICATION_ID,:processNo,T.THREAD_NO,0,0,'A',SYSDATE,null");
				sql.append(" FROM PROCESS_STATUS PS,(");

				for (int i = 0; i < baseTimer.threadNumber; i++) {
					if (i > 0) {
						sql.append(" UNION ALL");
					}
					sql.append(" SELECT " + i + " THREAD_NO FROM DUAL");					
				}
				sql.append(" )T");
				sql.append(" WHERE PS.PROCESS_STATUS_ID = :psID");
				sql.setString("psID", psID);
				sql.setString("processNo", baseTimer.getTimerProcessId());
				sql.setString("processStatusId", psID);
				ps = cn.prepareStatement(sql.getSql());
				sql.fillParams(ps);
				sql.log(ProcessThreadUtil.class);
				if ((baseTimer.threadNumber) != ps.executeUpdate()) {
					log.debug("�����߳�״̬��THREAD_STATUS����");
					log.error("�����߳�״̬��THREAD_STATUS����");
					throw new AppException("", "Timer["
							+ baseTimer.getTimerName()
							+ "]��������: ����TIMER�߳�״̬ʧ�ܡ�");
				}
			} else {
				log.debug("�������״̬��PROCESS_STATUS����");
				log.error("�������״̬��PROCESS_STATUS����");
				throw new AppException("", "Timer[" + baseTimer.getTimerName()
						+ "]���ô���: ȱ�ٺ�̨Ӧ�����ã��޷���������״̬��Ϣ��");
			}
			cn.commit();
		} catch (SQLException e) {
			ConnectionFactory.rollback();
			throw new SysException("100081","���½�����Ϣ�쳣��", e);
		} catch (SysException e) {
			ConnectionFactory.rollback();
			throw  e;
		} finally {
			JdbcUtil.close(ps);
			if (cn != null) {
				ConnectionFactory.closeConnection();
			}
		}
	}

	/**
	 * ��ȡ����ֵ
	 * 
	 * @param seqName
	 * @return
	 * @throws SysException
	 * @throws AppException
	 */
	public static String getSequenceNextVal(String seqName)
			throws SysException, AppException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		String seq = "0";
		Sql sql = new Sql();
		sql.append("SELECT " + seqName + "_SEQ.NEXTVAL FROM DUAL");
		try {
			conn = ConnectionFactory.getConnection();
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			if (rs.next()) {
				seq = rs.getString(1);
			}
		} catch (SQLException e) {
			throw new SysException("100000", "��������" + seqName + "�����ֵʧ��", e);
		} finally {
			JdbcUtil.close(rs,ps);
		}
		return seq;
	}

	/**
	 * �������״̬��PROCESS_STATUS���߳�״̬��THREAD_STATUS�еġ������ڴ��������������ۼƴ����������͡�����/״̬ʱ�䡱
	 * ע���ڷ����з����쳣ʱ�������׳���ֱ��return���ɣ�����Ӱ��Timer��������
	 * 
	 * @param String
	 *            application ��ʶ
	 * @param String
	 *            processNo ���̱��
	 * @param Integer
	 *            threadNo �̱߳��
	 * @param Long
	 *            throughpubCycle �����ڴ�������
	 */
	public static void realtimeFresh(BaseTimer baseTimer, int threadNo,long throughputCycle) {
		
		if (baseTimer.isRefreshHeatHeartFlag() == false)
			return;// �����в�ˢ������ֱ�ӷ���
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		long throughTotal = 0;
		log.debug("*******�����ڴ����¼��Ϊ��" + throughputCycle);
		try {
			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();
			sql = new Sql("SELECT ST.THROUGHPUT_TOTAL FROM THREAD_STATUS ST WHERE ST.PROCESS_NO = :processNo AND THREAD_NO = :threadNo");
			sql.setString("processNo", baseTimer.getTimerProcessId());
			sql.setInteger("threadNo", new Integer(threadNo));
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			if (rs !=null && rs.next()){
				throughTotal = rs.getLong("THROUGHPUT_TOTAL");
				throughTotal = throughTotal + throughputCycle;
			}
			sql = new Sql("UPDATE THREAD_STATUS SET THROUGHPUT_CYCLE = :throughputCycle,THROUGHPUT_TOTAL = :throughTotal,STS_DATE=SYSDATE");
			sql.append(" WHERE PROCESS_NO=:processNo AND THREAD_NO=:threadNo ");
			sql.setLong("throughputCycle", new Long(throughputCycle));
			sql.setLong("throughTotal", new Long(throughTotal));
			sql.setString("processNo", baseTimer.getTimerProcessId());
			sql.setInteger("threadNo", new Integer(threadNo));
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			ps.executeUpdate();
			log.debug("realtimeFresh end!");
		} catch (SysException e) {
			return;
		} catch (AppException e) {
			return;
		} catch (Exception e) {
			LogUtil.logExceptionStackTrace(log, e);
			return;
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				return;
			}
		}
	}

	public static boolean getSysConfigFlag(String configId, String localNetId,
			String areaId, String workAreaId, String exchId, String workTypeId)
			throws AppException, SysException {
		boolean flag = false;
		try {
			SysConfigSVO vo = getSysConfig(configId);// sysConfig
			SysAreaConfigSVO areaVO = new SysAreaConfigSVO();
			if (vo == null) {
				return flag;
			}
			if (SysConstants.SYS_CONFIG_TYPE_PROVINCE.equalsIgnoreCase(vo
					.getConfigType())) {
				if (SysConstants.TRUE.equals(vo.getCurValue()))
					flag = true;
			} else if (SysConstants.SYS_CONFIG_TYPE_LOCALNET
					.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(localNetId)) {
					throw new DataCacheException("1000031",
							"ϵͳ���������ǰ����������ã�������ʱȱ�ٱ�����������", null);
				}
				areaVO = getSysAreaConfig(configId, localNetId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_AREA.equalsIgnoreCase(vo
					.getConfigType())) {
				if (StringUtil.isBlank(areaId)) {
					throw new DataCacheException("1000031",
							"ϵͳ���������ǰ����������ã�������ʱȱ�ٷ�����������", null);
				}
				areaVO = getSysAreaConfig(configId, areaId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_WORKAREA
					.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(workAreaId)) {
					throw new DataCacheException("1000031",
							"ϵͳ���������ǰ��������ã�������ʱȱ�ٹ���������", null);
				}
				areaVO = getSysAreaConfig(configId, workAreaId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_EXCH.equalsIgnoreCase(vo
					.getConfigType())) {
				if (StringUtil.isBlank(exchId)) {
					throw new DataCacheException("1000031",
							"ϵͳ���������ǰ��������ã�������ʱȱ�پ��������", null);
				}
				areaVO = getSysAreaConfig(configId, exchId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_WORKTYPE
					.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(workTypeId)) {
					throw new DataCacheException("1000031",
							"ϵͳ���������ǰ������������ã�������ʱȱ�ٹ������Ͳ�����", null);
				}
				areaVO = getSysAreaConfig(configId, workTypeId.toString());
			} else {
				return flag;
			}

			if (areaVO != null) {
				if (SysConstants.TRUE.equals(areaVO.getCurValue()))
					flag = true;
			}

		} catch (Exception ex) {
			throw new SysException("", ex);
		}
		return flag;
	}

	public static SysConfigSVO getSysConfig(String configId)
			throws AppException, SysException {
		SysConfigSVO sysConfigSVO = null;
		log.debug("getSysConfig begin!configId[" + configId + "]");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		try {
			// �ж���������Ƿ�Ϸ�
			if (configId == null || "".equals(configId))
				throw new AppException("", "��ѯsysConfig����configIdΪ�գ�");

			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();

			// 2.��ȡ��̨Ӧ�õ�ID
			sql = new Sql("SELECT * FROM SYS_CONFIG WHERE CONFIG_ID=:configId");
			sql.setString("configId", configId);
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			while (rs.next()) {
				sysConfigSVO = new SysConfigSVO();
				sysConfigSVO.setConfigId(rs.getString("CONFIG_ID"));
				sysConfigSVO.setName(rs.getString("NAME"));
				sysConfigSVO.setSystemName(rs.getString("SYSTEM_NAME"));
				sysConfigSVO.setConfigType(rs.getString("CONFIG_TYPE"));
				sysConfigSVO.setCurValue(rs.getString("CUR_VALUE"));
				sysConfigSVO.setCreateDate(rs.getDate("CREATE_DATE"));

				log.debug("getSysConfig end!SysConfigId["
						+ sysConfigSVO.getConfigId() + "],curValue["
						+ sysConfigSVO.getCurValue() + "]");
			}
		} catch (SysException e) {
			return sysConfigSVO;
		} catch (AppException e) {
			return sysConfigSVO;
		} catch (Exception e) {
			LogUtil.logExceptionStackTrace(log, e);
			return sysConfigSVO;
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				return sysConfigSVO;
			}
		}
		return sysConfigSVO;
	}

	public static SysAreaConfigSVO getSysAreaConfig(String configId,
			String spAreaId) throws AppException, SysException {
		SysAreaConfigSVO sysAreaConfigSVO = null;
		log.debug("getSysAreaConfig begin!configId[" + configId + "]spAreaId["
				+ spAreaId + "]");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		try {
			// �ж���������Ƿ�Ϸ�
			if (configId == null || "".equals(configId))
				throw new AppException("", "��ѯsysAreaConfig����configIdΪ�գ�");
			if (spAreaId == null || "".equals(spAreaId))
				throw new AppException("", "��ѯsysAreaConfig����spAreaIdΪ�գ�");

			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();

			// 2.��ȡ��̨Ӧ�õ�ID
			sql = new Sql(
					"SELECT * FROM SYS_AREA_CONFIG WHERE CONFIG_ID=:configId");
			sql.setString("configId", configId);
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			if (rs.next()) {
				sysAreaConfigSVO = new SysAreaConfigSVO();
				sysAreaConfigSVO.setSysAreaConfigId(rs
						.getString("SYS_AREA_CONFIG_ID"));
				sysAreaConfigSVO.setConfigId(rs.getString("CONFIG_ID"));
				sysAreaConfigSVO.setSpAreaId(rs.getString("SP_AREA_ID"));
				sysAreaConfigSVO.setCurValue(rs.getString("CUR_VALUE"));

				log.debug("getSysAreaConfig end!configId["
						+ sysAreaConfigSVO.getConfigId() + "],spAreaId["
						+ sysAreaConfigSVO.getSpAreaId() + "],curValue["
						+ sysAreaConfigSVO.getCurValue() + "]");
			}
		} catch (SysException e) {
			return sysAreaConfigSVO;
		} catch (AppException e) {
			return sysAreaConfigSVO;
		} catch (Exception e) {
			LogUtil.logExceptionStackTrace(log, e);
			return sysAreaConfigSVO;
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				return sysAreaConfigSVO;
			}
		}
		return sysAreaConfigSVO;
	}

	/**
	 * ���Timer����״̬���ж�Timer�Ƿ��Ѿ�������������ر�
	 * 
	 * @param commonTimer
	 * @param arguments
	 * @throws AppException
	 * @throws SysException
	 */
	public static void checkRunStatus(BaseTimer commonTimer, Map arguments)
			throws AppException, SysException {
		log.debug("Begin to check " + commonTimer.getTimerName()
				+ " run status...");
		String localNetIds = (String) arguments.get("LOCAL_NET_ID");
		String workItem = (String) arguments.get("WORK_SYSTEM");
		if (localNetIds == null && workItem == null) {
			throw new AppException("��������", "Timer["
					+ commonTimer.getTimerName() + "]�����������ô���: ��Ч������������");
		}

		String servAppId = null;
		Connection cn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Sql sql = new Sql("SELECT SA.* FROM SERVER_APPLICATION SA");
			sql.append(" WHERE SA.NAME = :timerName");
			sql.setString("timerName", commonTimer.getTimerName());
			sql.append(" AND SA.STS = :sts");
			sql.setString("sts", SysConstants.STS_IN_USE);

			cn = ConnectionFactory.createConnection();
			ps = cn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);

			rs = ps.executeQuery();
			while (rs.next()) {
				if (servAppId == null) {
					servAppId = rs.getString("SERVER_APPLICATION_ID");
				} else {
					throw new AppException("", "Timer["
							+ commonTimer.getTimerName()
							+ "]���ô���: ���ڶ����Ч��Timer���á�");
				}
			}
			if (servAppId == null) {
				throw new AppException("", "Timer["
						+ commonTimer.getTimerName() + "]���ô���: ȱ�ٺ�̨Ӧ�����á�");
			}
			JdbcUtil.close(rs, ps);

			sql = new Sql("SELECT PS.* FROM PROCESS_STATUS PS");
			sql.append(" WHERE PS.SERVER_APPLICATION_ID = :servAppId");
			sql.setString("servAppId", servAppId);
			sql.append(" AND PS.STS = :sts");
			sql.setString("sts", SysConstants.STS_IN_USE);
			cn = ConnectionFactory.createConnection();
			ps = cn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);

			rs = ps.executeQuery();
			
			List processList = new ArrayList();
			while (rs.next()) {
				ProcessStatusSVO svo = new ProcessStatusSVO();
				svo.setProcessStatusId(rs.getString("PROCESS_STATUS_ID"));
				svo.setServerApplicationId(rs
						.getString("SERVER_APPLICATION_ID"));
				svo.setProcessNo(rs.getString("PROCESS_NO"));
				svo.setThreadNumber(rs.getString("THREAD_NUMBER"));
				svo.setThroughputCycle(rs.getString("THROUGHPUT_CYCLE"));
				svo.setThroughputTotal(rs.getString("THROUGHPUT_TOTAL"));
				svo.setSts(rs.getString("STS"));
				svo.setStsDate(rs.getTimestamp("STS_DATE"));
				// PROCESS_CODE,HOST_IP
				processList.add(svo);
			}

			for (int i = 0; i < processList.size(); i++) {
				ProcessStatusSVO svo = (ProcessStatusSVO) processList.get(i);
				String processCode = svo.getProcessCode();
				if (StringUtil.isBlank(processCode)) {
					throw new AppException("", "Timer["
							+ commonTimer.getTimerName() + "]���ô���: ������Ч�Ľ��̱��롣");
				} else {
					processCode = processCode.trim() + ",";
				}

				if (// �������ж�
				(// ������������Ϊ��
						localNetIds != null && (// ���б�����
						processCode.startsWith("0") ||
						// ��ǰ������
						processCode.startsWith(localNetIds.trim())))
						&& // ��Χϵͳ�ж�
						(// ��Χϵͳ������Ϊ��
						workItem != null && (// ������
						processCode.indexOf(","
								+ SPSConfig.UNLIMITED_WORK_SYSTEM + ",") > 0 ||
						// ��ǰ��Χϵͳ
						processCode.indexOf("," + workItem.trim() + ",") > 0))) {
					throw new AppException("", "Timer["
							+ commonTimer.getTimerName()
							+ "]У��ʧ��: �������½��̣�"
							+ ((localNetIds == null) ? "" : "[LOCAL_NET:"
									+ localNetIds + "]")
							+ ((workItem == null) ? "" : "[WORK_SYSTEM:"
									+ workItem + "]"));
				}
			}

		} catch (SQLException e) {
			ConnectionFactory.rollback();
			throw new SysException("10008","���Timer����״̬����SQLException��",e);
		} catch (SysException e) {
			ConnectionFactory.rollback();
			throw e;
		} finally {
			try {
				JdbcUtil.close(ps);
				ConnectionFactory.closeConnection();				
			} catch (SysException e) {
				LogUtil.logExceptionStackTrace(log,e);
				return;
			}
		}
	}

	/**
	 * 
	 * @param commonTimer
	 * @param localNetIds
	 * @param appName
	 * @param timerName
	 * @throws AppException
	 * @throws SysException 
	 */
	public static void validateTimer(BaseTimer commonTimer, String localNetIds,
			String appName) throws AppException, SysException {
		log.debug("validateTimer begin... ");
		boolean ifTimerInvalide = true;
		String processCode = null;
		String hostIP = getTimerProcessHostIp();
		String processNo = null;
		Sql sql = null;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			ResultSet rs = null;
			conn = ConnectionFactory.createConnection();
			sql = new Sql(
					" select * from PROCESS_STATUS ps , SERVER_APPLICATION sa where ps.SERVER_APPLICATION_ID=sa.SERVER_APPLICATION_ID and sa.NAME=:timerName and ps.HOST_IP is not null");
			sql.setString("timerName", commonTimer.getTimerName());
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			while (rs.next()) {
				processCode = rs.getString("PROCESS_CODE");
				processNo = rs.getString("PROCESS_NO");
				if (localNetIds.indexOf(",") != -1) {// localNetIdsΪ���������
					String[] netIdArray = localNetIds.split(",");
					for (int i = 0; i < netIdArray.length; i++) {
						if (processCode.indexOf(netIdArray[i]) != -1
								&& processCode.indexOf(appName) != -1) {
							ifTimerInvalide = false;
						}
					}
				} else {// localNetIdsΪһ��������
					if (processCode.startsWith("0")
							&& processCode.indexOf(appName) != -1) {
						ifTimerInvalide = false;
					}
					if (processCode.indexOf(localNetIds) != -1
							&& processCode.indexOf(appName) != -1) {
						ifTimerInvalide = false;
					}
				}
			}
			if (ifTimerInvalide == false) {
				log.info("Timer[" + commonTimer.getTimerName()
						+ "]������,����������IP:" + hostIP + ",���̺ţ�" + processNo
						+ ",����Timer����");
				throw new AppException("", "Timer["
						+ commonTimer.getTimerName() + "]������,����������IP:" + hostIP
						+ ",���̺ţ�" + processNo + ",����Timer����");
			}
		} catch (Exception e) {
			throw new SysException("", e);
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				return;
			}
		}
	}

	/**
	 * 
	 * @param commonTimer
	 * @param localNetIds
	 * @param getTimerName()
	 * @throws AppException
	 */
	public static void validateTimer(BaseTimer commonTimer, String localNetIds)
			throws AppException {
		return;		
		/*Sql sql = null;
		Connection conn = null;
		PreparedStatement ps = null;
		String HostIp = null;
		String processNo = null;
		try {
			ResultSet rs = null;
			conn = ConnectionFactory.createConnection();
			sql = new Sql(" SELECT * FROM PROCESS_STATUS PS,SERVER_APPLICATION SA WHERE PS.SERVER_APPLICATION_ID=SA.SERVER_APPLICATION_ID AND SA.NAME=:timerName AND PS.HOST_IP IS NOT NULL");
			sql.setString("timerName", commonTimer.getTimerName());

			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);			
			rs = ps.executeQuery();
			boolean ifValidate = true;
			while (rs.next()) {
				HostIp = rs.getString("HOST_IP");
				processNo = rs.getString("PROCESS_NO");
				String processCode = rs.getString("PROCESS_CODE");
				if (rs.getString("PROCESS_CODE").startsWith("0")){
									
					ifValidate = false;
				}					
				if (rs.getString("PROCESS_CODE").equals(commonTimer.getTimerProcessCode())){
					
					ifValidate = false;
				}					
				if (localNetIds.indexOf(",") > 0) {// localNetIdsΪ���������
					String[] netIdArray = localNetIds.split(",");
					// PROCESS_CODE��0��ͷ˵���������Ľ��̰������б�����
					for (int i = 0; i < netIdArray.length; i++) {
						if (processCode.indexOf(netIdArray[i]) > 0){
							ifValidate = false;
												
							break;
						}
							
					}
				} else {// localNetIdsΪһ��������
					if (processCode.indexOf(localNetIds) > 0)
					{
											
						ifValidate = false;
					}						
				}
			}
			if (ifValidate == false) {
				log.debug(commonTimer.getTimerName() + "  ["
						+ ProcessThreadUtil.getProcessId() + "] �ڷ�������"
						+ ProcessThreadUtil.getTimerProcessHostIp()
						+ " ������.....");
				throw new AppException("", "Timer["
						+ commonTimer.getTimerName() + "]������,����������IP:" + HostIp
						+ ",���̺ţ�" + processNo + ",����Timer����");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException("", e.getMessage());
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				e.printStackTrace();
				return;
			}
		}*/
	}

	/**
	 * ������Timer��ͬʱ��ͨ��Timer�����������ϵͳ����ID��
	 * 
	 * @author WuGang
	 * @param className
	 * @return
	 * @throws IOException
	 */
	public static String getProcessId() throws Exception {
		String pid = System.getProperty(ProcessThreadUtil.class.getName());
		if (pid == null) {
			String cmd[];
			File tempFile = null;
			try {
				if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)//��Windows����ϵͳ
					cmd = new String[] { "/bin/sh", "-c", "echo $$ $PPID" };
				else {
					tempFile = File.createTempFile("getpids", "exe");
					
					pump(ProcessThreadUtil.class.getResourceAsStream("getpids.exe"),
							new FileOutputStream(tempFile), true, true); // NOI18N
					cmd = new String[] { tempFile.getAbsolutePath() };
				}
				if (cmd != null) {
					Process p = Runtime.getRuntime().exec(cmd);
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					pump(p.getInputStream(), bout, false, true);
					if (tempFile != null)
						tempFile.delete();
					StringTokenizer stok = new StringTokenizer(bout.toString());
					stok.nextToken();
					pid = stok.nextToken();
					if (pid != null)
						System.setProperty(ProcessThreadUtil.class.getClass().getName(), pid);
				}
			} catch (IOException e) {
				throw new SysException("",e);
			}
		}
		return pid;
	}

	public static String getTimerProcessHostIp() throws AppException, SysException {
		String hostIp = null;
		try {
			hostIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new SysException("", e);
		}
		return hostIp;
	}

	/**
	 * ɱ���̷�������ϵͳ��ָ������ɱ��
	 * 
	 * @param pids
	 * @throws Exception
	 */
	public static void killOSProcess(String pids) throws Exception {
		String killCmd = null;
		if (pids == null || pids.length() == 0) {
			return;
		}
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
			// UNIX&LINUX: kill -9 PID
			killCmd = "kill -9 " + pids;
		} else {
			// windows:ntsd -c q -p PID
			killCmd = "ntsd -c q -p " + pids;
		}
		try {
			Runtime.getRuntime().exec(killCmd);
		} catch (IOException e) {
			throw new SysException("", e);
		}
	}

	/**
	 * ɱ���̷�������ϵͳ��ָ������ɱ��
	 * 
	 * @param pids
	 * @throws Exception
	 */
	public static void doOSCommand(String commandString) throws Exception {
		try {
			Runtime.getRuntime().exec(commandString);
		} catch (IOException e) {
			log.debug("ִ������[" + commandString + "]�쳣��" + e.getMessage());
			throw new SysException("", e);
		}
	}

	/**
	 * �̹߳ر���ڷ���,�ر�ĳһ��Timer�����н��̼������߳� ����:applicationCodeΪҪ�رյ�Timer����
	 * 
	 * @param applicationCode
	 * @throws SysException
	 * @throws AppException
	 */
	public static void killTimer(IBaseDBCommand commonTimer,
			String applicationCode) throws Exception {
		Connection conn = ConnectionFactory.createConnection();
		conn.setAutoCommit(false);
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		List processNos = null;
		// ��ѯ���̺ţ�PROCESS_NO
		sql = new Sql(
				"SELECT PS.PROCESS_NO FROM PROCESS_STATUS PS, SERVER_APPLICATION SA ");
		sql.append(" WHERE SA.SERVER_APPLICATION_ID = PS.SERVER_APPLICATION_ID ");
		if (applicationCode.indexOf(",") > 0) {
			String[] applicationCodes = applicationCode.split(",");
			sql.append(" AND SA.APPLICATION_CODE in ( ");
			for (int i = 0; i < applicationCodes.length; i++) {
				if (i > 0) {
					sql.append(",");
				}
				sql.append(":applicationCode" + i);
				sql.setString("applicationCode" + i, applicationCodes[i]);
			}
			sql.append(") ");
		} else {
			sql.append(" AND SA.APPLICATION_CODE = :applicationCode ");
			sql.setString("applicationCode", applicationCode);
		}
		sql.append(" AND PS.PROCESS_NO IS NOT NULL ");

		try {
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(TimerManager.class);
			sql.log(ProcessThreadUtil.class);
			rs = ps.executeQuery();
			processNos = new ArrayList();
			while (rs.next()) {
				processNos.add(rs.getString("PROCESS_NO"));
			}
			// ɾ���̼߳�¼
			for (int i = 0; i < processNos.size(); i++) {
				sql = new Sql(
						"DELETE FROM THREAD_STATUS TS WHERE TS.PROCESS_NO = :processNo ");
				sql.setString("processNo", (String) processNos.get(i));
				ps = conn.prepareStatement(sql.getSql());
				sql.fillParams(ps);
				sql.log(TimerManager.class);
				ps.execute();
			}
			// ���½��������Ϣ
			for (int i = 0; i < processNos.size(); i++) {
				sql = new Sql("DELETE FROM PROCESS_STATUS PS WHERE PS.PROCESS_NO = :processNo ");				
				sql.setString("processNo", (String) processNos.get(i));				
				ps = conn.prepareStatement(sql.getSql());
				sql.log(TimerManager.class);
				sql.fillParams(ps);
				ps.executeUpdate();
			}
			conn.commit();
			StringBuffer processStr = new StringBuffer();
			// ϵͳ��ɱ������,�������������ϵͳ
			for (int i = 0; i < processNos.size(); i++) {
				processStr.append((String) processNos.get(i) + " ");
			}
			killOSProcess(processStr.toString());
			log.debug(applicationCode + " �������н��̱�ɱ��");
		} catch (SQLException e) {
			ConnectionFactory.rollback();
			throw new SysException("", e);
		} catch (IOException e) {
			log.error("ɱ���̳���:" + e);
			throw new SysException("", e);
		} finally {
			ConnectionFactory.closeConnection();
		}
	}

	/**
	 * Timer��ʼ��ʱ��ȡ�̵߳��������ʱ��
	 * 
	 * @param timer
	 * @return
	 * @throws AppException
	 * @throws SysException 
	 */
	public static int getTimerDeadInterval(BaseTimer timer) throws AppException, SysException {
		int deadInterval = 0;
		Sql sql = null;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			ResultSet rs = null;
			conn = ConnectionFactory.createConnection();
			
			sql = new Sql(" SELECT DEAD_INTERVAL FROM SERVER_APPLICATION SA WHERE SA.APPLICATION_CODE=:timerName");
			
			sql.setString("timerName", timer.getTimerName().substring(0,timer.getTimerName().length()-5));
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			sql.log(ProcessThreadUtil.class);
			
			log.debug("��ѯsqlΪ��" + sql.getSql());
			log.debug("Timer����Ϊ��" + timer.getTimerName());
			
			rs = ps.executeQuery();
			if (rs.next()) {
				deadInterval = rs.getInt("DEAD_INTERVAL");
			} else {
				log.debug(timer.getTimerName() + " ��ʼ��û�ж�Ӧ���������ʱ��....");
				log.debug("��ѯSERVER_APPLICATION�������ʱ��Ϊ��....");
			}
		} catch (Exception e) {
			log.debug(timer.getTimerName() + " ��ʼ����ȡ��Ӧ���������ʱ���쳣��"
					+ e.getMessage());
			throw new AppException("", e.getMessage());
		} finally {
			JdbcUtil.close(ps);
			if (conn != null) {
				ConnectionFactory.closeConnection();
			}
			
		}
		return deadInterval;
	}

	/**
	 * ������ת��
	 * 
	 * @param in
	 * @param out
	 * @param closeIn
	 * @param closeOut
	 * @throws IOException
	 */
	private static void pump(InputStream in, OutputStream out, boolean closeIn,
			boolean closeOut) throws IOException {
		
		byte[] bytes = new byte[1024];
		int read;
		try {
			if (null==in){
				log.error("[������ת��pump,�õ��������Ϊ��]");
				return;
			}
			while ((read = in.read(bytes)) != -1)
				out.write(bytes, 0, read);
		} finally {
			if (closeIn)
				in.close();
			if (closeOut)
				out.close();
		}
	}

	public static boolean getConfigFlag() throws AppException, SysException {
		boolean sysConfigFlag = false;
		
		sysConfigFlag = getSysConfigFlag(
					SysConstants.SYS_CONFIG_TIMER_REALTIMEFRESH, null, null,
					null, null, null);
			log.debug("sysConfigFlag=" + sysConfigFlag);
		
		return sysConfigFlag;
	}

}
