package com.cattsoft.timers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.cattsoft.pub.ConnectionFactory;
import com.cattsoft.pub.ProcessThreadUtil;
import com.cattsoft.pub.SPSConfig;
import com.cattsoft.pub.SPSEncrypt;
import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.pub.util.LogUtil;

/**
 * Timer的基类,将公共的成员变量与方法抽象到此类中
 * 
 * @author WuGang
 * 
 */
public abstract class BaseTimer implements IBaseDBCommand {

	public static Logger log;

	public BaseTimer() {
		timerName = this.getClass().getName().substring(
				this.getClass().getPackage().getName().length() + 1,
				this.getClass().getName().length());
		//log.debug("application_code为：" + timerName.substring(0,timerName.length()-5));
	}

	/**
	 * Timer基本信息
	 */
	private String timerName;
	
	private DataSource inasDs = null;
	private DataSource iomDs = null;

	public DataSource getIomDs() {
		return iomDs;
	}

	// 进程编码，由启动参数确定
	public String timerProcessCode;

	// Timer宿主机
	private String timerProcessHost;

	// 进程编号
	private String timerProcessId;

	public String timerWorkItem;
	
	protected String threadClassName;

	public int threadNumber;// 线程数

	//private HashMap threadHeartHeatMap = new HashMap();
	private Hashtable threadHeartHeatMap = new Hashtable();

	private int deadInterval;// 线程死亡间隔

	private boolean refreshHeatHeartFlag = true;

	/**
	 * 系统配置信息: 数据库联接、EJB上下文地址
	 */
	private String connType = "";

	private String jndiName = "";

	private String jdbcDriver = null;

	private String jdbcUrl = null;

	private String dbUserName = null;

	private String dbPassword = null;
	
	private String iNasJdbcDriver = null;
	
	private String iNasDbUserName = null;
	
	private String iNasDbPassword = null;
	
	private String iNasJdbcUrl = null;
	
	private int iNasMaxActive = 1;
	
	private int iNasMaxIdle = 1;

	private int iNasMaxWait = 1;

	// 数据库连接池参数配置
	private int maxActive = 1;

	private int maxIdle = 1;

	private int maxWait = 1;

	private String ejbJndiName;

	public Context initialContext = null;

	private String initialContextFactory = null;

	private String providerUrl = null;

	private String securityPrincipal = null;

	private String securityCredentials = null;
	
	private String solrType = null;
	private String zkHost = null;
	private String solrUrl = null;
	
	private String iomJdbcDriver = null;

	private String iomJdbcUrl = null;

	private String iomDbUserName = null;

	private String iomDbPassword = null;

	// 线程刷新类型
	//private String refreshThreadMode = SPSConfig.REFRESH_THREAD_MODE_DB;// 默认是DB

	private int threadRefreshSleepTime;

	/**
	 * Timer线程配置：线程数量、
	 */
	// 每周期最小处理记录数，扫描记录数小于最小值，定义为线程空闲
	private int minCountPerCycle;

	// 每周期最大处理记录数
	private int maxCountPerCycle;

	// 有剩余记录休息时间
	private int timeBusySleep;

	// 无剩余记录休息时间
	private int timeFreeSleep;

	// 辅助线程休眠时间
	private int subTimerSleep;

	// 非工作时间休息时间
	private int timeLongSleep;
	
	// 失败重发次数
	private int repeatDoCount;

	/**
	 * Timer启动参数
	 */
	// 本地网，用于分进程
	public String localNetIds;

	// 分区标识，用于分进程
	public String subArea;

	// 每次程序运行时初始化为满足条件的最小id的值
	public long THE_LAST_MAX_ID = 0;

	// 最小ID上次归零时间,记录在内存中，便于比较
	public Calendar lastRestoreTime = Calendar.getInstance();

	// 归零时限，定义多长时间之后，THE_LAST_MAX_ID归零,默认300秒
	public int lastRestoreLimit = 300;
	
//	public static int WoEventsTimerCount =3;
//	
//	public static int WoEventsTimerInterval =10; 

	// 存各个线程的记录
	public List[] bizDataList = null;	
	
	//modify by caiqian 20101115每个辅助线程记录最后处理的记录，
	//主线程取数据前，先将curRecordList的值放到lastRecordList中，
	//取数据后，判断取出的数据是否和lastRecordList中记录的最后执行的值一致
	//如果一致，则说明改记录被重复取出，则不放到辅助线程的执行队列
	//不一致，则放到辅助线程的执行队列
	public String[] lastRecordList = null;
	//modify by caiqian 20101115记录每个辅助线程当前正在处理的记录，每次处理时更新此记录
	public String[] curRecordList = null;

	// public int bizDataListElementSize = 200;
 
	
	public String scanType;
	
	public int preTimeLimit;

	/**
	 * 初始化Timer配置信息
	 * 
	 * @param name
	 *            Timer名称，用来区分不同TIMER的参数
	 * @throws Exception
	 */
	protected void init() throws Exception {
		if (timerName == null || timerName.trim().length() == 0) {
			throw new AppException("", "初始化TIMER异常, TIMER名称错误");
		}

		// 读取配置：INITIAL_CONTEXT_FACTORY
		/*initialContextFactory = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_INIT_FACTORY).trim();
		// 读取配置：PROVIDER_URL
		providerUrl = SPSConfig.getInstance().getProp(
				SPSConfig.NAMING_PROVIDER_URL).trim();*/
		// 读取配置：DB_DRIVER
		jdbcDriver = SPSConfig.getInstance().getProp(SPSConfig.DB_DRIVER)
				.trim();
		// 读取配置：DB_URL
		jdbcUrl = SPSConfig.getInstance().getProp(SPSConfig.DB_URL).trim();
		// 读取配置：DB_USER
		dbUserName = SPSConfig.getInstance().getProp(SPSConfig.DB_USER).trim();
		// 读取配置：DB_PASSWORD
		dbPassword = SPSConfig.getInstance().getProp(SPSConfig.DB_PASSWORD)
				.trim();
		if("RelaseHangTaskTimer".equals(timerName) ){
			iomJdbcDriver = SPSConfig.getInstance().getProp("IOM_DB_DRIVER").trim();
			iomJdbcUrl = SPSConfig.getInstance().getProp("IOM_DB_URL").trim();
			iomDbUserName = SPSConfig.getInstance().getProp("IOM_DB_USER").trim();
			iomDbPassword = SPSConfig.getInstance().getProp("IOM_DB_PASSWORD").trim();
		}
		
		
		// 读取配置：ConnectionType
		connType = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.CONNECTION_TYPE).trim();
		// 读取配置：JNDIName
	/*	jndiName = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.JNDI_NAME).trim();*/
		// 读取配置：连接线程池参数--MaxActive
		maxActive = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_ACTIVE).trim());
		// 读取配置：连接线程池参数--MaxIdle
		maxIdle = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_IDLE).trim());
		// 读取配置：连接线程池参数--MaxWait
		maxWait = Integer.parseInt(SPSConfig.getInstance().getProp(
				timerName + SPSConfig.MAX_WAIT).trim());

		// 读取配置：Timer对应的PerCycleMinCount
		if (SPSConfig.getInstance().getProp(timerName + "PerCycleMinCount") != null) {
			minCountPerCycle = Integer.parseInt(SPSConfig.getInstance()
					.getProp(timerName + "PerCycleMinCount"));
		}
		// 读取配置：Timer对应的PerCycleMaxCount
		if (SPSConfig.getParameter(timerName + "PerCycleMaxCount") != null) {
			maxCountPerCycle = Integer.parseInt(SPSConfig
					.getParameter(timerName + "PerCycleMaxCount"));
		}
		// 读取配置：Timer对应的BusySleepTime
		if (SPSConfig.getParameter(timerName + "BusySleepTime") != null) {
			timeBusySleep = Integer.parseInt(SPSConfig.getParameter(timerName
					+ "BusySleepTime"));
		}
		// 读取配置：Timer对应的FreeSleepTime
		if (SPSConfig.getParameter(timerName + "FreeSleepTime") != null) {
			timeFreeSleep = Integer.parseInt(SPSConfig.getParameter(timerName
					+ "FreeSleepTime"));
		}
		// 读取配置：Timer对应的subTimerSleepTimer
		if (SPSConfig.getParameter(timerName + "subTimerSleepTime") != null) {
			subTimerSleep = Integer.parseInt(SPSConfig.getParameter(timerName
					+ "subTimerSleepTime"));
		}
		// 读取配置：Timer对应的RepeatDoCount
		if (SPSConfig.getParameter(timerName + "RepeatDoCount") != null) {
			repeatDoCount = Integer.parseInt(SPSConfig.getParameter(timerName
					+ "RepeatDoCount"));
		}
		
		if("BillDataBringTimer".equals(timerName) || "BillDataTakeTimer".equals(timerName) || "BillDataTakeBJTimer".equals(timerName)){
			iNasJdbcDriver = SPSConfig.getInstance().getProp(
					"INAS_" + SPSConfig.DB_DRIVER).trim();
			iNasDbUserName = SPSConfig.getInstance().getProp(
					"INAS_" + SPSConfig.DB_USER).trim();
			iNasDbPassword = SPSConfig.getInstance().getProp(
					"INAS_" + SPSConfig.DB_PASSWORD).trim();
			iNasJdbcUrl = SPSConfig.getInstance().getProp(
					"INAS_" + SPSConfig.DB_URL).trim();
			iNasMaxActive = Integer.parseInt(SPSConfig.getInstance().getProp(
					timerName + "Inas" + SPSConfig.MAX_ACTIVE).trim());
			iNasMaxIdle = Integer.parseInt(SPSConfig.getInstance().getProp(
					timerName + "Inas" + SPSConfig.MAX_IDLE).trim());
			iNasMaxWait = Integer.parseInt(SPSConfig.getInstance().getProp(
					timerName + "Inas" + SPSConfig.MAX_WAIT).trim());
		}
		
		
		// 读取配置：Timer对应的StartTime
		/*String temp = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.TIMER_START_TIME);
		if (temp != null) {
			timerStartTime = Integer.parseInt(temp.trim());
		}*/
		// 读取配置：Timer对应的EndTime
		/*temp = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.TIMER_END_TIME);
		if (temp != null) {
			timerEndTime = Integer.parseInt(temp.trim());
		}*/
		// 读取配置：Timer对应的LongSleepTime
		String temp = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.LONG_SLEEP_TIME);
		if (temp != null) {
			timeLongSleep = Integer.parseInt(temp.trim());
		} else {
			timeLongSleep = timeBusySleep;
		}
		// 读取配置：Timer对应的ejbJndiName
		temp = SPSConfig.getInstance().getProp(
				"JNDI_" + timerName.toUpperCase());
		if (temp != null) {
			ejbJndiName = temp;
		}

		// 读取THE_LAST_MAX_ID回零时限
		temp = SPSConfig.getInstance().getProp(
				timerName + SPSConfig.LAST_RESTORE_LIMIT).trim();
		if (temp != null) {
			lastRestoreLimit = Integer.parseInt(temp);
		}
		 
		// 读取配置：REFRESH_THREAD_MODE
		/*temp = SPSConfig.getParameter(SPSConfig.REFRESH_THREAD_MODE);
		if (temp != null) {
			refreshThreadMode = temp;			
		}*/

		// 设置启动Timer的主机
		timerProcessHost = ProcessThreadUtil.getTimerProcessHostIp();
		timerProcessId = ProcessThreadUtil.getProcessId();

		// 初始化结果集
		bizDataList = new LinkedList[threadNumber];

		//modify by caiqian 20101115
		lastRecordList = new String[threadNumber];
		curRecordList = new String[threadNumber];
		
		threadClassName = this.getClass().getPackage().getName() + "."
				+ timerName.replaceAll("Timer", "Thread");
		String tempStr = "";
		this.solrType = (tempStr = SPSConfig.getInstance().getProp("SOLR_TYPE"))!=null?tempStr.trim():null;
		this.solrUrl = (tempStr = SPSConfig.getInstance().getProp("SOLR_URL"))!=null?tempStr.trim():null;
		this.zkHost = (tempStr = SPSConfig.getInstance().getProp("Zk_HOST"))!=null?tempStr.trim():null;
		this.scanType = (tempStr = SPSConfig.getInstance().getProp(timerName+"ScanType"))!=null?tempStr.trim():null;
	}

	/**
	 * 从数据库中初始化Timer数据，需在数据库连接池初始化之后调用
	 * 
	 * @throws AppException
	 * @throws SysException 
	 */
	protected void initDBData() throws AppException, SysException {
		// 初始化线程死亡间隔和Timer实时回填信息标志
		deadInterval = ProcessThreadUtil.getTimerDeadInterval(this);
		refreshHeatHeartFlag = ProcessThreadUtil.getConfigFlag();
		log.debug("获取Timer实时回填信息标志完成" + refreshHeatHeartFlag);
	}

	/**
	 * 参数赋值
	 * 
	 * @param args
	 * @throws AppException
	 */
	public abstract void initArguments(String[] args) throws AppException;

	/**
	 * 校验Timer对应进程是否启动： <br>
	 * 1. 读取后台应用配置（SERVER_APPLICATION）；用APPLICATION_CODE查询，有且仅有一条有效记录，否则校验失败<br>
	 * 2. 读取对应的进程状态（PROCESS_STATUS）；<br>
	 * 通过SERVER_APPLICATION_ID关联查询，如果存在有效记录，判断进程编码，如果已启动进程参数范围大于当前进程，则校验失败；
	 * 
	 * 注： SERVER_APPLICATION配置是Timer正常启动的前提；<br>
	 * 正常关闭Timer时，必须删除进程状态（PROCESS_STATUS）记录和线程状态（THREAD_STATUS）记录，否则Timer不能再次启动；
	 * 
	 * @throws AppException
	 */
	public abstract void checkExist() throws AppException, SysException;

	/**
	 * 重启timer
	 * 
	 * @throws AppException
	 */
	public void reStartTimer(String timerName) {
		try {
			ProcessThreadUtil.killTimer(this, timerName);
			ProcessThreadUtil.doOSCommand("./Start" + timerName + ".sh");
		} catch (Exception e) {
			LogUtil.logExceptionStackTrace(log,e);
		}
	}

	/**
	 * Timer启动入口方法,包括参数校验,写表信息,初始化ejbContext
	 * 
	 * @throws AppException
	 * @throws SysException 
	 */
	public void start(String[] args) throws AppException, SysException {
		log.info(timerName + " 启动中...");
		try {
			// 1.初始化Timer的启动参数
			initArguments(args);
			// 2.初始化Timer成员变量
			init();
			// 3.初始化连接工厂
			initConnectionFactory();
			// 4.初始化数据库数据
			//initDBData();
			// 5.校验Timer是否已启动
			//checkExist();
			// 6.写进程与线程信息到数据库
			// log.debug("BaseTimer完成了checkExist()方法！");
			// log.debug("......................输出Timer进程编号timerProcessId="+timerProcessId);
			/*if (this.timerProcessId != null) {
				ProcessThreadUtil.writeProcessInfo(this);
			}*/

			// 7.启动线程
			for (int i = 0; i <= threadNumber; i++) {
				BaseThread thread = BaseThread.getThread(this, threadClassName, i);
				thread.setBaseTimer(this);
				if (threadNumber == i) {
					// 最后一个线程为主线程，负责扫描数据库，其他线程负责处理数据
					thread.setMainThread(true);
				} else {
					thread.setMainThread(false);
				}
				new Thread(thread).start();
			}
			// 8.启动辅助线程,需要判断
			/*
			 * if (refreshThreadMode.equals(SPSConfig.REFRESH_THREAD_MODE_RAM)) {
			 * AssistantThread assistantThread = new AssistantThread();
			 * assistantThread.setBaseTimer(this); new
			 * Thread(assistantThread).start(); }
			 */
		} catch (SysException e) {
			throw new SysException("10008","启动timer发生异常!",e);
		} catch (Exception e) {
			throw new SysException("10001","Timer异常终止！", e);
		}
		log.info(timerName + " 启动结束");
	}

	public String getSolrType() {
		return solrType;
	}

	public void setSolrType(String solrType) {
		this.solrType = solrType;
	}

	public String getZkHost() {
		return zkHost;
	}

	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}

	public String getSolrUrl() {
		return solrUrl;
	}

	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	/**
	 * 初始化TIMER上下文,主要是EJB所需要的
	 * 
	 * @return context
	 * @throws AppException
	 */
	public Context initialContext() throws AppException ,SysException{
		if (initialContext == null) {
			try {
				log.debug("初始化上下文环境 Begin ... ");
				Hashtable properties = new Hashtable();
				log.debug("initialContextFactory为：" + initialContextFactory);
				properties.put(Context.INITIAL_CONTEXT_FACTORY,
						initialContextFactory);
				log.debug("providerUrl为：" + providerUrl);
				properties.put(Context.PROVIDER_URL, providerUrl);
				if (securityPrincipal != null
						&& !securityPrincipal.equals(securityPrincipal)) {
					properties.put(Context.SECURITY_PRINCIPAL,
							securityPrincipal);
					properties.put(Context.SECURITY_CREDENTIALS,
							securityCredentials == null ? securityCredentials
									: securityCredentials);
				}
				initialContext = new InitialContext(properties);
				log.debug("初始化上下文环境 End ... ");
			} catch (NamingException e) {
				log.error("初始化上下文环境错误：" + e.toString());
				throw new SysException("10008", "初始化EJB Context 异常",e);
			}
		}
		return initialContext;
	}

	/**
	 * 初始化线程池,每个Timer实例相互独立
	 * 
	 * @throws Exception
	 */
	public void initConnectionFactory() throws SysException {
		try {
			log.info("初始化连接池 Begin ...");
			ConnectionFactory.initConnectionFactory(this);
			log.info("初始化连接池 End");
		} catch (SysException e) {
			throw new SysException("", "初始化连接失败...", e);
		}
		if("BillDataBringTimer".equals(timerName) || "BillDataTakeTimer".equals(timerName)|| "BillDataTakeBJTimer".equals(timerName)){
			this.initINASDataSource();
		}
		if("RelaseHangTaskTimer".equals(timerName)){
			this.initIOMDataSource();
		}
	}
	
	/**
	 * 初始化INAS数据源
	 * @throws SysException
	 */
	public void initINASDataSource() throws SysException{
		
		log.info("初始化INAS 数据源 Begin ...");
		
		BasicDataSource iNasBasicDataSource = new BasicDataSource();
		iNasBasicDataSource.setDriverClassName(this.getINasJdbcDriver());
		iNasBasicDataSource.setUsername(this.getINasDbUserName());
		iNasBasicDataSource.setPassword(SPSEncrypt.JieM(this.getINasDbPassword()));
		iNasBasicDataSource.setUrl(this.getINasJdbcUrl());
		iNasBasicDataSource.setMaxActive(this.getINasMaxActive());
		iNasBasicDataSource.setMaxIdle(this.getINasMaxIdle());
		iNasBasicDataSource.setMaxWait(this.getINasMaxWait());
		
		inasDs = iNasBasicDataSource;
		
		if (null == inasDs) {
			throw new SysException("1001","系统获取INAS连接的数据源失败！",new Exception());
		}
		try {
			Connection conn = inasDs.getConnection();
			log.debug("inas connection "+conn);
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new SysException("1001","创建inas连接失败！",e);
		}
		
		log.info("初始化INAS 数据源 End ...");
		
	}
	
	
	/**
	 * 初始化INAS数据源
	 * @throws SysException
	 */
	public void initIOMDataSource() throws SysException{
		
		log.info("初始化IOM 数据源 Begin ...");
		
		BasicDataSource iomBasicDataSource = new BasicDataSource();
		iomBasicDataSource.setDriverClassName(this.iomJdbcDriver);
		iomBasicDataSource.setUsername(this.iomDbUserName);
		iomBasicDataSource.setPassword(SPSEncrypt.JieM(this.iomDbPassword));
		iomBasicDataSource.setUrl(this.iomJdbcUrl);
		iomBasicDataSource.setMaxActive(this.getMaxActive());
		iomBasicDataSource.setMaxIdle(this.getMaxIdle());
		iomBasicDataSource.setMaxWait(this.getMaxWait());
		
		iomDs = iomBasicDataSource;
		
		if (null == iomDs) {
			throw new SysException("1001","系统获取IOM连接的数据源失败！",new Exception());
		}
		try {
			Connection conn = iomDs.getConnection();
			log.debug("iom connection "+conn);
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			throw new SysException("1001","创建iom连接失败！",e);
		}
		
		log.info("初始化IOM 数据源 End ...");
		
	}
	

	/**
	 * 初始化EJB上下文,抽象方法由子类来实现
	 * 
	 * @throws Exception
	 */
	public abstract void initEJBContext() throws AppException,SysException;

	/**
	 * 每处理一条记录前，先将该记录的唯一id记录下来，作为当前正在处理的数据队列
	 * add by caiqian 20101115
	 */
	public void addCurRecord(int threadIndex,String obj){
		curRecordList[threadIndex] = obj;
	}

	/**
	 * 判断最后执行记录是否等于该值
	 * add by caiqian 20101115
	 */
	public boolean containInLastRecord(int threadIndex,String obj){
		if(lastRecordList[threadIndex] == null||"".equals(lastRecordList[threadIndex])){
			return false;
		}
		return lastRecordList[threadIndex].equals(obj);
	}

	/**
	 * 当主线程
	 * add by caiqian 20101115
	 */
	public void copyCurRecordToLastRecord(){
		for(int i=0;i<curRecordList.length;i++){
			lastRecordList[i] = curRecordList[i];
		}
	}

	public String getConnType() {
		return connType;
	}

	public String getJndiName() {
		return jndiName;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
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

	public String getInitialContextFactory() {
		return initialContextFactory;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public String getSecurityPrincipal() {
		return securityPrincipal;
	}

	public String getSecurityCredentials() {
		return securityCredentials;
	}

	public String getTimerProcessId() {
		return timerProcessId;
	}

	public String getTimerName() {
		return timerName;
	}

	public int getTimeLongSleep() {
		return timeLongSleep;
	}

	public int getTimeBusySleep() {
		return timeBusySleep;
	}

	public int getTimeFreeSleep() {
		return timeFreeSleep;
	}

	public String getTimerProcessCode() {
		return timerProcessCode;
	}

	public String getTimerWorkItem() {
		return timerWorkItem;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public int getDeadInterval() {
		return deadInterval;
	}

	public String getEjbJndiName() {
		return ejbJndiName;
	}

	public Context getInitialContext() {
		return initialContext;
	}	

	public int getThreadRefreshSleepTime() {
		return threadRefreshSleepTime;
	}

	public int getMinCountPerCycle() {
		return minCountPerCycle;
	}

	public int getMaxCountPerCycle() {
		return maxCountPerCycle;
	}

	public int getRepeatDoCount() {
		return repeatDoCount;
	}

	public String getLocalNetIds() {
		return localNetIds;
	}

	public String getSubArea() {
		return subArea;
	}

	public Hashtable getThreadHeartHeatMap() {
		return threadHeartHeatMap;
	}
	
	public void putThreadHeartHeatMap(Integer threadIndex) {
		threadHeartHeatMap.put(threadIndex,Calendar.getInstance());
	}

	public boolean isRefreshHeatHeartFlag() {
		return refreshHeatHeartFlag;
	}

	public void setRefreshHeatHeartFlag(boolean refreshHeatHeartFlag) {
		this.refreshHeatHeartFlag = refreshHeatHeartFlag;
	}

	public String getTimerProcessHost() {
		return timerProcessHost;
	}

	public int getSubTimerSleep() {
		return subTimerSleep;
	}

	public void setSubTimerSleep(int subTimerSleep) {
		this.subTimerSleep = subTimerSleep;
	}

	public void setTimerProcessHost(String timerProcessHost) {
		this.timerProcessHost = timerProcessHost;
	}

	public void setTimerProcessId(String timerProcessId) {
		this.timerProcessId = timerProcessId;
	}

	public void setTimerName(String timerName) {
		this.timerName = timerName;
	}
	
	public String getINasDbPassword() {
		return iNasDbPassword;
	}

	public String getINasDbUserName() {
		return iNasDbUserName;
	}

	public String getINasJdbcDriver() {
		return iNasJdbcDriver;
	}

	public String getINasJdbcUrl() {
		return iNasJdbcUrl;
	}

	public int getINasMaxActive() {
		return iNasMaxActive;
	}

	public int getINasMaxIdle() {
		return iNasMaxIdle;
	}

	public int getINasMaxWait() {
		return iNasMaxWait;
	}	

	public DataSource getInasDs() {
		return inasDs;
	}
 
}
