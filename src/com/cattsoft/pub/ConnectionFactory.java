package com.cattsoft.pub;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.timers.IBaseDBCommand;

/**
 * 
 * Title: ����ͨϵͳ<br>
 * Description: ͨ�����õ����ӳء�����Դ��ȡ����<br>
 * Date: 2007-8-17 <br>
 * Copyright cattsoft
 */
public class ConnectionFactory {
	private static Logger log = Logger.getLogger(ConnectionFactory.class);

	/**
	 * JDBC����
	 */
	private static IBaseDBCommand baseTimerCtx = null;

	private static ThreadLocal connections = new ThreadLocal();

	private DataSource ds = null;

	private static ConnectionFactory instance;

	private ConnectionFactory() throws SysException {

		if(SPSConfig.ConnectionType_JNDI_POOL.equals(baseTimerCtx.getConnType())){
			ds = getJNDIDataSource();
			log.debug("ʹ��JNDI���ӳ�");
		}else{
			ds = getDBCPDataSource();
			log.debug("ʹ�ñ������ӳ�");
		}
		/*"ds/hbsps"*/
		if (null == ds) {
			//log.error("ϵͳ��ȡ���ӵ�����Դʧ�ܣ�");
			throw new SysException("1001","ϵͳ��ȡ���ӵ�����Դʧ�ܣ�",new Exception());
		}
		try {
			Connection conn = ds.getConnection();
			//log.debug("connection "+conn);
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new SysException("1001","��������ʧ�ܣ�",e);
		}
	}
	public ConnectionFactory(String func) {
		super();
	}

	/**
	 * ���DBCP���ӳص�����Դ
	 * 
	 * @return
	 */
	private DataSource getDBCPDataSource() {

		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(baseTimerCtx.getJdbcDriver());
		ds.setUsername(baseTimerCtx.getDbUserName());
		ds.setPassword(baseTimerCtx.getDbPassword());
		ds.setUrl(baseTimerCtx.getJdbcUrl());
		ds.setMaxActive(baseTimerCtx.getMaxActive());
		ds.setMaxIdle(baseTimerCtx.getMaxIdle());
		ds.setMaxWait(baseTimerCtx.getMaxWait());
		return ds;

	}
	
	/**
	 * ���JNDI���ӳص�����Դ
	 * 
	 * @return
	 * @throws SysException 
	 */
	private DataSource getJNDIDataSource() throws SysException {

		Context initialContext = null;
		DataSource ds = null;
		try {
			initialContext = baseTimerCtx.initialContext();
			ds = (DataSource)initialContext.lookup(baseTimerCtx.getJndiName());
		} catch (Exception e) {
			throw new SysException("1001","��ȡJNDI����ʧ�ܣ�",e);
		}
		return ds;
	}

	/**
	 * ��ʼ���������ú�����Դ
	 * @param commonTimer
	 * @throws SysException
	 */
	synchronized public static void initConnectionFactory(IBaseDBCommand commonTimer) throws SysException {
		
		baseTimerCtx = commonTimer;
		if (null == instance) {
			instance = new ConnectionFactory();
		}
	}

	/**
	 * ��������
	 * 
	 * @return
	 */
	public static Connection createConnection() throws SysException{
		if (null == instance) {
			 throw new RuntimeException("ϵͳδ��ʼ��ConnectionFactory��");
			 //initConnectionFactory();
		}
		if (connections.get() == null) {
			//log.debug("��ȡ������ ...");
			Connection conn = null;
			try {
				conn = instance.ds.getConnection();
				//log.debug("�ɹ���������:" + conn);
				connections.set(conn);
			} catch (SQLException e) {
				throw new SysException("","",e);
				//log.debug("ϵͳ��������ʧ�ܣ�");
			}
			//instance.ds.getConnection();
			//log.debug("�ɹ���������:" + conn);
			connections.set(conn);
		}
		log.debug("�ɹ���ȡ����:" + connections.get());
		return (Connection) connections.get();
	}

	/**
	 * ��õ�ǰ�̶߳�Ӧ������
	 * 
	 * @return
	 */
	public static Connection getConnection() throws SysException{
		Connection conn = (Connection) connections.get();
		if (null == conn) {
			throw new SysException();
		}
		return conn;
	}
	
	/**
	 * �ع���ǰ���ӵ�����
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public static void rollback() throws  SysException {
		Connection conn = (Connection) connections.get();
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new SysException("100001", "ϵͳ�����ύ����!", e);
		}
	}
	
	/**
	 * �ύ��ǰ���ӵ�����
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public static void commit() throws SysException {
		Connection conn = (Connection) connections.get();
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new SysException("100001", "ϵͳ�����ύ����!", e);
		}
	}

	/**
	 * �ͷŵ�ǰ����
	 * 
	 */
	public static void closeConnection() {
		Connection conn = (Connection) connections.get();
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connections.set(null);
		}
	}

}
