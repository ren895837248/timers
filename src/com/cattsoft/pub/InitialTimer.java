package com.cattsoft.pub;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.timers.IBaseDBCommand;

public class InitialTimer implements IBaseDBCommand {
	/**
	 * JDBC����
	 */
	private String jdbcDriver = "";
	private String jdbcUrl = "";
	private String dbUserName = "";
	private String dbPassword = "";
	/**
	 * Ӧ�÷����������Ļ�������
	 */
	private String initialContextFactory = "";
	private String providerUrl = "";
	private String securityPrincipal = "";
	private String securityCredentials = "";
	/**
	 * ���ݿ����ӳز�������
	 */
	public int maxActive = 1;
	public int maxIdle = 1;
	public int maxWait = 1;
	public String connType = "";//��������
	public String jndiName = "";
	public InitialTimer(String timerName) {
		initialContextFactory = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_INIT_FACTORY).trim();
		providerUrl = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_PROVIDER_URL).trim();
		jdbcDriver = SPSConfig.getInstance().getProp(SPSConfig.DB_DRIVER)
				.trim();
		jdbcUrl = SPSConfig.getInstance().getProp(SPSConfig.DB_URL).trim();
		dbUserName = SPSConfig.getInstance().getProp(SPSConfig.DB_USER).trim();
		dbPassword = SPSConfig.getInstance().getProp(SPSConfig.DB_PASSWORD)
				.trim();
		// ÿ��timer���������� timer��+������
		connType = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.CONNECTION_TYPE).trim();
		jndiName = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.JNDI_NAME).trim();
		maxActive = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_ACTIVE).trim());
		maxIdle = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_IDLE).trim());
		maxWait = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_WAIT).trim());
	}
	
	/**
	 * 
	 * ��Timer����ʱ�õ��Ĺ��췽��:TimerManager�ô˹��췽����������ݿ�����
	 */
	public InitialTimer(){
		initialContextFactory = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_INIT_FACTORY).trim();
		providerUrl = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_PROVIDER_URL).trim();
		jdbcDriver = SPSConfig.getInstance().getProp(SPSConfig.DB_DRIVER)
				.trim();
		jdbcUrl = SPSConfig.getInstance().getProp(SPSConfig.DB_URL).trim();
		dbUserName = SPSConfig.getInstance().getProp(SPSConfig.DB_USER).trim();
		dbPassword = SPSConfig.getInstance().getProp(SPSConfig.DB_PASSWORD)
				.trim();
		connType=SPSConfig.ConnectionType_LOCAL_POOL;
	}
	public Context initialContext() throws AppException, SysException {
		Context initialContext = null;
		try {
			// log.debug("��ʼ�������Ļ��� Begin ... ");
			Hashtable properties = new Hashtable();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,
					initialContextFactory);
			properties.put(Context.PROVIDER_URL, providerUrl);
			if (securityPrincipal != null
					&& !securityPrincipal.equals(securityPrincipal)) {
				properties.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
				properties.put(Context.SECURITY_CREDENTIALS,
						securityCredentials == null ? securityCredentials
								: securityCredentials);
			}
			initialContext = new InitialContext(properties);
			// log.debug("��ʼ�������Ļ��� End ... ");
		} catch (NamingException e) {
			throw new SysException("", e);
			// log.error("��ʼ�������Ļ�������" + e.toString());
		}
		return initialContext;
	}

	/*public Connection getDBConnection() {
		Connection conn = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, dbUserName, dbPassword);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}*/

	public String getJNDIName() {
		return jndiName;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public int getMaxWait() {
		return maxWait;
	}
	public String getDbUserName() {
		return dbUserName;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public String getDbPassword() {
		return dbPassword;
	}
	public String getConnType() {
		return connType;
	}
	public String getJndiName() {
		return jndiName;
	}
}
