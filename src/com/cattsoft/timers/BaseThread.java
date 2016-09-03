/**
 * 
 */
package com.cattsoft.timers;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.cattsoft.pub.ProcessThreadUtil;
import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.pub.util.LogUtil;

/**
 * 所有业务线程基类
 * 
 * @author WuGang
 */
public abstract class BaseThread implements Runnable{
	public static Logger log;
	protected List bizList;

	public int threadIndex;

	public Date heartHeatDate;// 上次心跳刷新的日期时间

	public BaseTimer baseTimer;

	private long sleepTime;

	private boolean runFlag = true;

	public int queryDataRows = 0;// 最后一次扫描数据库得到的数据量

	// 主线程标识，标明是否是主线程，主线程负责扫描，其他线程负责处理数据
	public boolean isMainThread = false;

	/**
	 * BaseTimer 的工厂方法,客户端获得Timer的入口
	 * 
	 * @param timerName:Timer的类名
	 * @return
	 */
	public static BaseThread getThread(BaseTimer baseTimer, String threadName,
			int mod) {
		BaseThread thread = null;
		try {
			Class threadClass = BaseTimer.class.getClassLoader().loadClass(
					threadName);
			Integer[] modArg = { new Integer(mod) };
			Constructor[] constructors = threadClass.getConstructors();
			thread = (BaseThread) constructors[0].newInstance(modArg);
			return thread;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("BaseThread 工厂方法获得 线程:" + threadName
					+ " 异常:" + e.getMessage());
		}
	}

	/**
	 * 所有线程通用的run方法,针对不同线程提供了不同的策略
	 */
	public void run() {

		setup();
		while (runFlag) {
			try {
				init();
				while (true) {
					try {
						boolean iScanFlag = false;
						boolean isBusiThread = false;
						log.debug("*****总线程数量：" + baseTimer.threadNumber + ", 当前线程编号：" + this.threadIndex +" 循环中 , 主辅标志：" + isMainThread());
						if(baseTimer.bizDataList != null){
							log.debug("*****BaseTimer的队列数组长度：" + baseTimer.bizDataList.length);
							if(this.threadIndex < baseTimer.bizDataList.length && baseTimer.bizDataList[this.threadIndex] != null){
								log.debug("*******线程编号：" + this.threadIndex + " 的队列长度为：" + baseTimer.bizDataList[this.threadIndex].size());
							}
						}
						// 如果是主线程
						if (isMainThread() && this.threadIndex == baseTimer.threadNumber) {
							// 还原THE_LAST_MAX_ID
							restoreMAXId();									
							if (baseTimer.THE_LAST_MAX_ID == 0) {
								log
										.debug("[thread "
												+ this.threadIndex
												+ "]"
												+ baseTimer.getTimerName()
												+ " , THE_LAST_MAX_ID 归零了,清空所有线程的队列 ]");
								this.clearContext(baseTimer.bizDataList);
								iScanFlag = true;
							} else {
								for (int i = 0; i < baseTimer.bizDataList.length; i++) {
									if (null == baseTimer.bizDataList[i]
											|| baseTimer.bizDataList[i].size() == 0) {
										log.debug("**********线程编号：" + this.threadIndex + " 循环中，线程编号为：" + i +" 的线程队列为空，扫描标志置为true");
										iScanFlag = true;
										break;
									}
								}
								if (iScanFlag) {
									log.debug("**********线程编号：" + this.threadIndex + " 循环中，扫描标志为：" + iScanFlag);
									for (int i = 0; i < baseTimer.bizDataList.length; i++) {
										if (null != baseTimer.bizDataList[i]
												&& baseTimer.bizDataList[i]
														.size() > baseTimer
														.getMaxCountPerCycle() * 2) {
											isBusiThread = true;
											break;
										}
									}
									if (isBusiThread) {										
										this
												.clearContext(baseTimer.bizDataList);
										baseTimer.THE_LAST_MAX_ID = 0;
										log
												.debug("[thread "
														+ this.threadIndex
														+ "]"
														+ this.getClass()
																.getName()
														+ ".run()方法 ,判断存在堵塞的线程, THE_LAST_MAX_ID 归零,并清空所有线程的队列 ]");
									}
								}
							}
							// 如果某一个辅助线程处理完了它的数据，则主线程扫描数据库
							if (iScanFlag) {
								//modify by caiqian 20101115 在主线程取数据前，将当前正在处理的数据数组复制到最后处理的数据数组中
								baseTimer.copyCurRecordToLastRecord();
								initBizData();
								if(baseTimer.isRefreshHeatHeartFlag()){
									log.debug("*******主线程编号：" + this.threadIndex + " 更新进程状态到数据库");
									//ProcessThreadUtil.updateProcessStatus(baseTimer,this.queryDataRows);
								}								
							}
							if (this.queryDataRows < baseTimer
									.getMaxCountPerCycle()) {
								sleepTime = baseTimer.getTimeFreeSleep();
							} else {
								sleepTime = baseTimer.getTimeBusySleep();
							}							
						} else {
							log.debug("**********线程编号：" + this.threadIndex + " 的线程初始化Context并处理队列中的数据");
							initContext();
							int execCount = 0;
							if(baseTimer.bizDataList != null 
									&& baseTimer.bizDataList[this.threadIndex] != null){
								execCount = baseTimer.bizDataList[this.threadIndex].size();
							}
							processBizData();
							//辅助线程采用较短时间休眠
							sleepTime = baseTimer.getSubTimerSleep();
							if(baseTimer.isRefreshHeatHeartFlag()){
								log.debug("**********线程编号：" + this.threadIndex + " 的线程realtimeFresh心跳到数据库");								
								//ProcessThreadUtil.realtimeFresh(baseTimer,this.threadIndex,execCount);
							}
						}
					} catch (AppException e) {
						log.info(baseTimer.getTimerName() + ":" + threadIndex
								+ ":" + e.getMessage());
						LogUtil.logExceptionStackTrace(log,e);
					} finally {
						log.debug("**********线程编号：" + this.threadIndex + " 内层循环中，刷新线程心跳并开始休眠");
						refreshThread();
						sleepThread();
					}
				}
			} catch (SysException e) {
				LogUtil.logExceptionStackTrace(log,e);
				log.info(baseTimer.getTimerName() + ":" + threadIndex + ":"
						+ e.getMessage());
			} catch (Exception e) {
				LogUtil.logExceptionStackTrace(log,e);
				log.info(baseTimer.getTimerName() + ":" + threadIndex + ":"
						+ e.getMessage());
			} finally {
				log.debug("**********线程编号：" + this.threadIndex + " 外层循环中，开始休眠");
				sleepThread();
			}
		}
		//destroy();
	}

	// 将THE_LAST_MAX_ID还原的方法
	public void restoreMAXId() throws AppException, SysException {
		
		log.debug("*****线程编号:" + this.threadIndex + "  判断THE_LAST_MAX_ID是否归零");
		Calendar lastRestoreTime = (Calendar) baseTimer.lastRestoreTime.clone();
		lastRestoreTime.add(Calendar.SECOND, baseTimer.lastRestoreLimit);
		
		log.debug("当前时间  " + new Date());
		log.debug("归零时间  " + lastRestoreTime.getTime());
		
		log.debug(baseTimer.getTimerName() + "是否需要归0，ManHourPoolTimer，在处理完所有数据时才归0");
		
		if ((!"ManHourPoolTimer".equals(baseTimer.getTimerName()))&&new Date().after(lastRestoreTime.getTime())) {
			log.debug(baseTimer.getTimerName() + baseTimer.lastRestoreLimit
					+ "秒后，THE_LAST_MAX_ID归零！");
			baseTimer.THE_LAST_MAX_ID = 0;
			baseTimer.lastRestoreTime = Calendar.getInstance();
		}

	}

	/**
	 * 线程while外层循环初始方法,可将线程池初始化方法放入其中
	 * 
	 * @throws AppException
	 */
	private void init() throws AppException {
		try {
			log.debug("线程编号为：" + this.threadIndex + " 的线程初始化,线程主辅标志为：" + isMainThread());
			baseTimer.initConnectionFactory();
			refreshThread();
		} catch (SysException e) {
			throw new AppException("", "初始化数据库出现异常");
		}
	}

	/**
	 * 线程获取业务数据方法,将取来的数据存入bizList中,以待processBizData来做处理
	 * 
	 * @return
	 * @throws SQLException
	 */
	public abstract void initBizData() throws AppException, SysException;

	/**
	 * 线程处理业务数据方法,将从initBizData方法中获得到的bizList做相关的业务处理工作
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public abstract void processBizData() throws AppException, SysException;

	/**
	 * 线程时间校验规则,根据业务规则判断当前线程是否运行
	 * 
	 * @return
	 * @throws AppException
	 */
	/*public boolean checkRunTime() throws SysException {
		log.debug("线程编号为：" + this.threadIndex + " 的线程进入校验线程时间方法");
		boolean isRun = true;
		int startHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		// 开始时间==结束时间： 说明无业务时间规则,直接进入内层循环
		if (baseTimer.getTimerStartTime() == baseTimer.getTimerEndTime()) {
			isRun = true;
		}
		// 当前时间不在运行时间范围内：则不运行内层循环,并设置sleepTime为长休眠时间,这种情况是在一天的时间段内，如6：00-18：00
		else if (baseTimer.getTimerStartTime() < baseTimer.getTimerEndTime()) {
			if (startHour < baseTimer.getTimerStartTime()
					|| startHour > baseTimer.getTimerEndTime()) {
				sleepTime = baseTimer.getTimeLongSleep();
				isRun = false;
			}
		}
		// 不在一天的时间段内，比如晚上20：00到第二天早上6：00
		else {
			if (startHour < baseTimer.getTimerStartTime()
					&& startHour > baseTimer.getTimerEndTime()) {
				sleepTime = baseTimer.getTimeLongSleep();
				isRun = false;
			}
		}
		log.debug("线程编号为：" + this.threadIndex + " 的线程结束校验线程时间方法，返回值为：" + isRun);
		return isRun;
	}*/

	/**
	 * 刷新线程心跳:刷新方式：基于内存
	 * 
	 */
	private void refreshThread() {
		//synchronized (baseTimer.getThreadHeartHeatMap()) {
			log.debug("[ 线程编号: " + threadIndex + " 在内存中刷新线程心跳");
			baseTimer.putThreadHeartHeatMap(new Integer(threadIndex));
		//}
	}

	/**
	 * 运行线程休眠的统一方法：根据不同的sleepTime进行休眠
	 */
	private void sleepThread() {
		if(sleepTime > 1000){
			log.info(baseTimer.getTimerName() + " [线程编号:" + threadIndex + "] 休眠:"
					+ sleepTime / 1000 + "秒");
		}else{
			log.info(baseTimer.getTimerName() + " [线程编号:" + threadIndex + "] 休眠:"
					+ sleepTime + "毫秒");
		}		
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			LogUtil.logExceptionStackTrace(log,e);
		}
	}

	/**
	 * 初始context,子类需要实现具体的初始化方法
	 * 
	 * @throws Exception
	 */
	private void initContext() throws AppException,SysException {
		baseTimer.initEJBContext();
	}

	private void setup() {
	}

	public void setBaseTimer(BaseTimer baseTimer) {
		this.baseTimer = baseTimer;
	}

	public boolean isMainThread() {
		return isMainThread;
	}

	public void setMainThread(boolean isMainThread) {
		this.isMainThread = isMainThread;
	}

	/**
	 * 心跳刷新方法:主线程调用，更新所有线程心跳到数据库
	 */
	/*public void mainThreadRefreshHeartBeat() {

		Hashtable map = baseTimer.getThreadHeartHeatMap();
		Set mapKeys = map.keySet();
		Object[] keyArray = mapKeys.toArray();
		
		if(!isMainThread()){
			log.debug("****线程编号为：" + threadIndex + " 的线程非主线程，mainThreadRefreshHeartBeat方法直接返回,当前线程主线程标志为：" + isMainThread());
			log.debug("****线程编号为：" + threadIndex + " 的线程队列长度为：" + (baseTimer.bizDataList[threadIndex] == null? 0 : baseTimer.bizDataList[threadIndex].size()));
			return;
		}
		//synchronized (baseTimer.getThreadHeartHeatMap()) {
			for (int i = 0; i < keyArray.length; i++) {
				Calendar lastRefreshTime = (Calendar)((Calendar) (map.get(keyArray[i]))).clone();
				lastRefreshTime.add(Calendar.SECOND, baseTimer
						.getDeadInterval());// 上次刷新时间向后推延DEAD_INTERVAL
				if (new Date().after(lastRefreshTime.getTime())) {
					log.error(baseTimer.getTimerName() + "[报警：线程编号 为 " + i
							+ " 的线程已死]");
					// baseTimer.reStartTimer(baseTimer.getTimerName());
				} else {
					log.debug(baseTimer.getTimerName() + "[主线程 " + threadIndex
							+ "更新线程" + keyArray[i] + " 心跳时间到数据库]");
					ProcessThreadUtil.realtimeFresh(baseTimer,
							((Integer) keyArray[i]).intValue());
				}				
			}
		//}
	}*/

	private void clearContext(List list[]) {
		if (list == null)
			return;
		for (int i = 0; i < list.length; i++) {
			if (null != list[i] && (list[i].size() > 0)) {
				synchronized (list[i]) {
					list[i].clear();
				}
			}
		}
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

}
