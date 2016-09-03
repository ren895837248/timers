package com.cattsoft.pub;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.cattsoft.timers.BaseTimer;

public class SPSConfig {
	public static String APP_ROOT_PATH = null;

	public static final String APPLICATION_CONFIG = "SPSConfig";
	
	public static final String LOCK_FLAG="N";

	/**
	 * 数据库配置
	 */

	public static final String DB_DRIVER = "DB_DRIVER";

	public static final String DB_URL = "DB_URL";

	public static final String DB_USER = "DB_USER";

	public static final String DB_PASSWORD = "DB_PASSWORD";
	
	
	//连接类型
	public static final String NAMING_INIT_FACTORY = "INITIAL_CONTEXT_FACTORY";
	//JNDI名称
	public static final String NAMING_PROVIDER_URL = "PROVIDER_URL";
	

	/**
	 * 上下文环境配置
	 */

	public static final String CONNECTION_TYPE = "ConnectionType";

	public static final String JNDI_NAME = "JNDIName";

	/**
	 * 是否永久执行标志
	 */

	public static final String RUN_STILL = "RUN_STILL";
	
	
	// 实时标志
	public static String REAL_TIME_YES = "Y";
	public static String REAL_TIME_NO = "N";

	// WO 业务状态标志
	public static String BIZ_CALL_FAILED = "I";
	
	// 连接类型
	public static final String ConnectionType_LOCAL_POOL = "localPool";
	public static final String ConnectionType_JNDI_POOL = "JNDIPool";
	 
	//连接线程池参数
	public static final String MAX_ACTIVE = "MaxActive";
	public static final String MAX_IDLE = "MaxIdle";
	public static final String MAX_WAIT = "MaxWait";
	
	//Timer各个线程运行时间段
	public static final String TIMER_START_TIME = "StartTime";
	public static final String TIMER_END_TIME = "EndTime";
	public static final String LONG_SLEEP_TIME="LongSleepTime";
	
	//线程刷新模式有两种：DB 和 RAM
	public static final String REFRESH_THREAD_MODE="REFRESH_THREAD_MODE";
	public static final String REFRESH_THREAD_MODE_DB="DB";
	public static final String REFRESH_THREAD_MODE_RAM="RAM";
	/**
	 * 各个timer的连接池参数配置
	 */
	public static final String AlarmTimerMaxActive = "AlarmTimerMaxActive";

	public static final String AlarmTimerMaxIdle = "AlarmTimerMaxIdle";

	public static final String AlarmTimerMaxWait = "AlarmTimerMaxWait";

	public static final String StateMachineTimerMaxActive = "StateMachineTimerMaxActive";

	public static final String StateMachineTimerMaxWait = "StateMachineTimerMaxWait";

	public static final String StateMachineTimerMaxIdle = "StateMachineTimerMaxIdle";

	public static final String AutoStepTimerMaxActive = "AutoStepTimerMaxActive";

	public static final String AutoStepTimerMaxIdle = "AutoStepTimerMaxIdle";

	public static final String AutoStepTimerMaxWait = "AutoStepTimerMaxWait";

	public static final String SoParseTimerMaxActive = "SoParseTimerMaxActive";// SoParseTimer

	public static final String SoParseTimerMaxIdle = "SoParseTimerMaxIdle";// SoParseTimer

	public static final String SoParseTimerMaxWait = "SoParseTimerMaxWait";// SoParseTimer

	public static final String SoMatchTimerMaxActive = "SoMatchTimerMaxActive";// SoMatchTimer

	public static final String SoMatchTimerMaxIdle = "SoMatchTimerMaxIdle";// SoMatchTimer

	public static final String SoMatchTimerMaxWait = "SoMatchTimerMaxWait";// SoMatchTimer

	public static final String SoExpReturnTimerMaxActive = "SoExpReturnTimerMaxActive";// SoExpReturnTimer

	public static final String SoExpReturnTimerMaxIdle = "SoExpReturnTimerMaxIdle";// SoExpReturnTimer

	public static final String SoExpReturnTimerMaxWait = "SoExpReturnTimerMaxWait";// SoExpReturnTimer

	public static final String ReportTimerMaxActive = "ReportTimerMaxActive";// ReportTimer

	public static final String ReportTimerMaxIdle = "ReportTimerMaxIdle";// ReportTimer

	public static final String ReportTimerMaxWait = "ReportTimerMaxWait";// ReportTimer

	public static final String BSRequestTimerMaxActive = "BSRequestTimerMaxActive";

	public static final String BSRequestTimerMaxIdle = "BSRequestTimerMaxIdle";

	public static final String BSRequestTimerMaxWait = "BSRequestTimerMaxWait";
	
	public static final String BSResponseTimerMaxActive = "BSResponseTimerMaxActive";

	public static final String BSResponseTimerMaxIdle = "BSResponseTimerMaxIdle";

	public static final String BSResponseTimerMaxWait = "BSResponseTimerMaxWait";

    public static final String CurToArcToHisTimerMaxActive = "CurToArcToHisTimerMaxActive";// SoMatchTimer

	public static final String CurToArcToHisTimerMaxIdle = "CurToArcToHisTimerMaxIdle";// SoMatchTimer

	public static final String CurToArcToHisTimerMaxWait = "CurToArcToHisTimerMaxWait";// SoMatchTimer

	/**
	 * 各个timer的JNDI参数配置
	 */

	public static final String JNDI_AUTOSTEPTIMER = "JNDI_AUTOSTEPTIMER";

	public static final String JNDI_STATEMACHINETIMER = "JNDI_STATEMACHINETIMER";

	public static final String JNDI_BSRESPONSETIMER = "JNDI_BSRESPONSETIMER";//ADD BY RONG HENG EN 20110901
	
	public static final String JNDI_SOPARSETIMER = "JNDI_SOPARSETIMER";// soParseTimer-JNDI

	public static final String JNDI_SOMATCHTIMER = "JNDI_SOMATCHTIMER";// soMatchTimer-JNDI

	public static final String JNDI_SOEXPRETURNTIMER = "JNDI_SOEXPRETURNTIMER";// soExpReturnTimer-JNDI

	public static final String JNDI_SWITCHTIMER = "JNDI_SWITCHTIMER";

	public static final String JNDI_ALARMTIMER = "JNDI_ALARMTIMER";

	/**
	 * 各个timer每个周期扫描最大记录数
	 */

	public static final String AutoStepTimerPerCycleMaxCount = "AutoStepTimerPerCycleMaxCount";

	public static final String SwitchTimerPerCycleMaxCount = "SwitchTimerPerCycleMaxCount";

	public static final String StateMachineTimerPerCycleMaxCount = "StateMachineTimerPerCycleMaxCount";

	public static final String SoParseTimerPerCycleMaxCount = "SoParseTimerPerCycleMaxCount";// SoParseTimer每周期最大处理记录数

	public static final String SoMatchTimerPerCycleMaxCount = "SoMatchTimerPerCycleMaxCount";// SoMatchTimer每周期最大处理记录数

	public static final String SoExpReturnTimerPerCycleMaxCount = "SoExpReturnTimerPerCycleMaxCount";// SoExpReturnTimer每周期最大处理记录数

	public static final String BSRequestTimerPerCycleMaxCount = "BSRequestTimerPerCycleMaxCount";
	
	public static final String BSResponseTimerPerCycleMaxCount = "BSResponseTimerPerCycleMaxCount";

    public static final String CurToArcToHisTimerPerCycleMaxCount = "CurToArcToHisTimerPerCycleMaxCount";


	/**
	 * 各个timer每次扫描记录数如果小于等于该最小值，则定义为线程空闲时
	 */
	public static final String AutoStepTimerPerCycleMinCount = "AutoStepTimerPerCycleMinCount";

	public static final String StateMachineTimerPerCycleMinCount = "StateMachineTimerPerCycleMinCount";

	public static final String SoParseTimerPerCycleMinCount = "SoParseTimerPerCycleMinCount";

	public static final String SoMatchTimerPerCycleMinCount = "SoMatchTimerPerCycleMinCount";

	public static final String SoExpReturnTimerPerCycleMinCount = "SoExpReturnTimerPerCycleMinCount";

	public static final String BSRequestTimerPerCycleMinCount = "BSRequestTimerPerCycleMinCount";
	
	public static final String BSResponseTimerPerCycleMinCount = "BSResponseTimerPerCycleMinCount";

    public static final String CurToArcToHisTimerPerCycleMinCount = "CurToArcToHisTimerPerCycleMinCount";


	/**
	 * 休眠时间(各个timer有自己的休眠时间)
	 */
	public static final String StateMachineTimerBusySleepTime = "StateMachineTimerBusySleepTime";// 忙碌时

	public static final String StateMachineTimerFreeSleepTime = "StateMachineTimerFreeSleepTime";// 空闲时

	public static final String AutoStepTimerBusySleepTime = "AutoStepTimerBusySleepTime";// 忙碌时

	public static final String AutoStepTimerFreeSleepTime = "AutoStepTimerFreeSleepTime";// 空闲时

	public static final String SoParseTimerBusySleepTime = "SoParseTimerBusySleepTime";// SoParseTimer无剩余记录时间隔时间

	public static final String SoParseTimerFreeSleepTime = "SoParseTimerFreeSleepTime";// SoParseTimer有剩余记录时间隔时间
	
	public static final String SoParseTimersubTimerSleepTime = "SoParseTimersubTimerSleepTime";// SoParseTimer辅助线程休眠时间

	public static final String SoMatchTimerBusySleepTime = "SoMatchTimerBusySleepTime";// SoMatchTimer无剩余记录时间隔时间

	public static final String SoMatchTimerFreeSleepTime = "SoMatchTimerFreeSleepTime";// SoMatchTimer有剩余记录时间隔时间
	
	public static final String SoMatchTimersubTimerSleepTime = "SoMatchTimersubTimerSleepTime";// SoMatchTimer辅助线程休眠时间

	public static final String SoExpReturnTimerBusySleepTime = "SoExpReturnTimerBusySleepTime";// SoExpReturnTimer无剩余记录时间隔时间

	public static final String SoExpReturnTimerFreeSleepTime = "SoExpReturnTimerFreeSleepTime";// SoExpReturnTimer有剩余记录时间隔时间

	public static final String ReportTimerSleepTime = "ReportTimerSleepTime";// ReportTimer数据生成检查间隔时间

	public static final String BSRequestTimerBusySleepTime = "BSRequestTimerBusySleepTime";

	public static final String BSRequestTimerFreeSleepTime = "BSRequestTimerFreeSleepTime";

	public static final String BSResponseTimerBusySleepTime = "BSResponseTimerBusySleepTime";

	public static final String BSResponseTimerFreeSleepTime = "BSResponseTimerFreeSleepTime";

	public static final String AlarmTimerSleepTime = "AlarmTimerSleepTime";
	
	public static final String AlarmTimerFreeSleepTime = "AlarmTimerFreeSleepTime";
	
	public static final String AlarmTimerLongSleepTime = "AlarmTimerLongSleepTime";

	public static final String CurToArcToHisTimerBusySleepTime = "CurToArcToHisTimerBusySleepTime";// 忙碌时

	public static final String CurToArcToHisTimerFreeSleepTime = "CurToArcToHisTimerFreeSleepTime";// 空闲时

    public static final String CurToArcToHisTimerLongSleepTime = "CurToArcToHisTimerLongSleepTime";// 空闲时

	/**
	 * 实时标志（适用于工作流引擎）
	 */

	public static final String REALTIME_FLAG = "REALTIME_FLAG";

	/**
	 * 各个timer最大重复处理次数
	 */

	public static final String AutoStepTimerRepeatDoCount = "AutoStepTimerRepeatDoCount";

	public static final String SoParseTimerRepeatDoCount = "SoParseTimerRepeatDoCount";// SoParseTimer最大重复处理次数

	public static final String SoMatchTimerRepeatDoCount = "SoMatchTimerRepeatDoCount";// SoMatchTimer最大重复处理次数

	public static final String SoExpReturnTimerRepeatDoCount = "SoExpReturnTimerRepeatDoCount";// SoExpReturnTimer最大重复处理次数

	public static final String StateMachineTimerRepeatDoCount = "StateMachineTimerRepeatDoCount";

	public static final String ReportTimerInfos = "ReportTimerInfos";// ReportTimer线程开始工作时间和存储过程名称数组字符串

	public static final String BSRequestTimerRepeatDoCount = "BSRequestTimerRepeatDoCount";
	
	public static final String BSResponseTimerRepeatDoCount = "BSResponseTimerRepeatDoCount";

    public static final String CurToArcToHisTimerRepeatDoCount = "CurToArcToHisTimerRepeatDoCount";
    
    public static final String MsgTimerRepeatDoCount = "MsgTimerRepeatDoCount";//MsgTimer最大重复处理次数

    /**
	 * 其他配置
	 */

    public static final String soCurSql = "soCurSql";

    public static final String soHisSql = "soHisSql";
    
    public static final String soToArcProcName = "SoCurToArcProcName";
    
    public static final String soArcToHisProcName = "SoArcToHisProcName";
    
    public static final String soToArcProcSasName = "SoCurToArcProcSasName";
    
    public static final String soArcToHisProcSasName = "SoArcToHisProcSasName";

    public static final String CurToArcToHisTimerStartTime="CurToArcToHisTimerStartTime";

    public static final String curToArcToHisTimerEndTime="curToArcToHisTimerEndTime";
    
    public static final String SwitchTimerStartTime="SwitchTimerStartTime";
    
    public static final String SwitchTimerEndTime="SwitchTimerEndTime";
    
    public static final String SwitchTimerLongSleepTime="SwitchTimerLongSleepTime";
    
    public static final String VoiceTimerRepeatSendCount = "VoiceTimerRepeatSendCount";
    
    public static final String WoEventsTimerDelayHours= "WoEventsTimerDelayHours";//WoEventsTimer二次预约转出时间(小时)
    
    // mod by baijm 2013-05-30
	public static final String WoEventsTimerCount = "WoEventsTimerCount";
	
	public static final String WoEventsTimerInterval = "WoEventsTimerInterval";
	
	public static final String WoEventsTimerAlarmSql = "WoEventsTimerAlarmSql";//WoEventsTimer中是否查询告警工单
	
	public static final String WoEventsTimerPreAlarmSql = "WoEventsTimerPreAlarmSql";//WoEventsTimer中是否查询预警工单
	// mod end

	/**
	 * BSResponseTimer
	 */
	public static final String BSResponseTimerUpdateCRM = "BSResponseTimerUpdateCRM";//是否更新CRM停复状态
	
	public static final String BSResponseTimerRecordARC = "BSResponseTimerRecordARC";//是否进行停复记录归档
	/**
	 * CurToArcToHisTimer专用参数
	 * 
	 */
	 //定单竣工时间与系统当前时间差值大于此值，归历史Timer不对其处理
    public static final String SoArcToHisTimerRangeBeginDays = "SoArcToHisTimerRangeBeginDays";

    //定单竣工时间与系统当前时间差值小于此值，归历史Timer不对其处理
    public static final String SoArcToHisTimerRangeEndDays = "SoArcToHisTimerRangeEndDays";
    
    //定单竣工时间与系统当前时间差值大于此值，归档Timer不对其处理
    public static final String SoCurToArcTimerRangeBeginDays = "SoCurToArcTimerRangeBeginDays";

    //定单竣工时间与系统当前时间差值小于此值，归档Timer不对其处理
    public static final String SoCurToArcTimerRangeEndDays = "SoCurToArcTimerRangeEndDays";

	// ------------------------

	public static final String TfTimerPerCycleMaxCount = "TfTimerPerCycleMaxCount";

	public static final String TfTimerCycle = "TfTimerCycle";
	
	//Timer扫描最小ID归零时限 add by rong
	public static final String LAST_RESTORE_LIMIT = "LastRestoreLimit";
	
	//UbiClient发送登录消息的用户名密码串
	public static final String UBI_LGIN_INFO = "UbiLginInfo";
	
	public static final String UBI_FILTER_CONFIG = "UbiClientFilterConfigId";
	
	public static final String UBI_LENGTH_LENGTH = "UbiLengthLength";
	
	public static final String PressPrecent = "Precent";

	private static SPSConfig _appConfig = new SPSConfig();

	private HashMap _configs = null;

	private SPSConfig() {
		_configs = new HashMap();

		// try {
		// String classPath =
		// Class.forName("com.cattsoft.pub.SPSConfig").getResource(
		// "SPSConfig.class").getPath();
		// System.out.println(classPath);
		APP_ROOT_PATH = "";
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
	}

	public static SPSConfig getInstance() {
		return (_appConfig);
	}

	public String getProp(String prop) {
		String propValue = (String) _configs.get(prop);		
		if (propValue == null) {
			try{
			String propFile = APP_ROOT_PATH + APPLICATION_CONFIG;
			ResourceBundle resourceBundle = ResourceBundle.getBundle(propFile);
			propValue = resourceBundle.getString(prop);
			}catch(MissingResourceException e){
				BaseTimer.log.debug(e.getMessage());
			}
			if (propValue != null) {
				if (prop.equals(DB_PASSWORD)) {
					propValue = SPSEncrypt.JieM(propValue);
				}
				setProp(prop, propValue);
			}
		}
		return (propValue);
	}

	private void setProp(String prop, String propValue) {
		_configs.put(prop, propValue);
	}

	public static String getParameter(String ParameterName) {
		SPSConfig spsConfig = SPSConfig.getInstance();
		return (spsConfig.getProp(ParameterName));
		
	}
	
	// Unlimited_work_system 用于区分外围系统的timer进程
	public static String UNLIMITED_WORK_SYSTEM = "unLimit";
}
