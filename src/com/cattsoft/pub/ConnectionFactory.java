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
 * Title: 服务开通系统<br>
 * Description: 通过配置的连接池、数据源获取连接<br>
 * Date: 2007-8-17 <br>
 * Copyright cattsoft
 */
public class ConnectionFactory {
	private static Logger log = Logger.getLogger(ConnectionFactory.class);

	/**
	 * JDBC配置
	 */
	private static IBaseDBCommand baseTimerCtx = null;

	private static ThreadLocal connections = new ThreadLocal();

	private DataSource ds = null;

	private static ConnectionFactory instance;

	private ConnectionFactory() throws SysException {

		if(SPSConfig.ConnectionType_JNDI_POOL.equals(baseTimerCtx.getConnType())){
			ds = getJNDIDataSource();
			log.debug("使用JNDI连接池");
		}else{
			ds = getDBCPDataSource();
			log.debug("使用本地连接池");
		}
		/*"ds/hbsps"*/
		if (null == ds) {
			//log.error("系统获取连接的数据源失败！");
			throw new SysException("1001","系统获取连接的数据源失败！",new Exception());
		}
		try {
			Connection conn = ds.getConnection();
			//log.debug("connection "+conn);
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new SysException("1001","创建连接失败！",e);
		}
	}
	public ConnectionFactory(String func) {
		super();
	}

	/**
	 * 获得DBCP连接池的数据源
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
	 * 获得JNDI连接池的数据源
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
			throw new SysException("1001","获取JNDI数据失败！",e);
		}
		return ds;
	}

	/**
	 * 初始化连接配置和数据源
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
	 * 创建连接
	 * 
	 * @return
	 */
	public static Connection createConnection() throws SysException{
		if (null == instance) {
			 throw new RuntimeException("系统未初始化ConnectionFactory！");
			 //initConnectionFactory();
		}
		if (connections.get() == null) {
			//log.debug("获取连接中 ...");
			Connection conn = null;
			try {
				conn = instance.ds.getConnection();
				//log.debug("成功创建连接:" + conn);
				connections.set(conn);
			} catch (SQLException e) {
				throw new SysException("","",e);
				//log.debug("系统创建连接失败！");
			}
			//instance.ds.getConnection();
			//log.debug("成功创建连接:" + conn);
			connections.set(conn);
		}
		log.debug("成功获取连接:" + connections.get());
		return (Connection) connections.get();
	}

	/**
	 * 获得当前线程对应的连接
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
	 * 回滚当前连接的事务
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public static void rollback() throws  SysException {
		Connection conn = (Connection) connections.get();
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new SysException("100001", "系统事务提交错误!", e);
		}
	}
	
	/**
	 * 提交当前连接的事务
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public static void commit() throws SysException {
		Connection conn = (Connection) connections.get();
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new SysException("100001", "系统事务提交错误!", e);
		}
	}

	/**
	 * 释放当前连接
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
