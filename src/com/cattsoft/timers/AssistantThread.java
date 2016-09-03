/**
 * 
 */
package com.cattsoft.timers;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;

/**
 * 线程组的辅助线程,用来完成线程组的心跳等功能
 * @author WuGang
 * 
 */
public class AssistantThread implements Runnable {
	
	BaseTimer baseTimer;
	public void setBaseTimer(BaseTimer baseTimer) {
		this.baseTimer = baseTimer;
	}
	public void run() {
		while (true) {
			while (true) {
				try {
					Thread.sleep(baseTimer.getThreadRefreshSleepTime());
					refreshHeartBeat();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 心跳刷新方法:如果判断当前
	 */
	public void refreshHeartBeat() {
		Hashtable map = baseTimer.getThreadHeartHeatMap();
		Set mapKeys = map.keySet();
		Object [] keyArray=mapKeys.toArray();
		for(int i=0;i<keyArray.length;i++){
			System.out.println("processNo="+baseTimer.getTimerProcessId()+"thread id="+keyArray[i]+",上次刷新时间："+map.get(keyArray[i]));
		}
		//synchronized(this){
			for (int i = 0; i < keyArray.length; i++) {
				Calendar lastRefreshTime=(Calendar)map.get(keyArray[i]);
				lastRefreshTime.add(Calendar.MILLISECOND,baseTimer.getDeadInterval());// 上次刷新时间向后推延DEAD_INTERVAL
				if(new Date().after(lastRefreshTime.getTime())){
					// 报警:此线程已死
				}else{
					//ProcessThreadUtil.realtimeFresh(baseTimer, ((Integer)keyArray[i]).intValue());
					// 更新当前线程心跳时间到数据库
				}
				
			}
		//}
	}
	public void initBizData() throws AppException, SysException {
		
	}
	public void processBizData() throws AppException, SysException {
		
	}
}
