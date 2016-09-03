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
 * �߳���ĸ����߳�,��������߳���������ȹ���
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
	 * ����ˢ�·���:����жϵ�ǰ
	 */
	public void refreshHeartBeat() {
		Hashtable map = baseTimer.getThreadHeartHeatMap();
		Set mapKeys = map.keySet();
		Object [] keyArray=mapKeys.toArray();
		for(int i=0;i<keyArray.length;i++){
			System.out.println("processNo="+baseTimer.getTimerProcessId()+"thread id="+keyArray[i]+",�ϴ�ˢ��ʱ�䣺"+map.get(keyArray[i]));
		}
		//synchronized(this){
			for (int i = 0; i < keyArray.length; i++) {
				Calendar lastRefreshTime=(Calendar)map.get(keyArray[i]);
				lastRefreshTime.add(Calendar.MILLISECOND,baseTimer.getDeadInterval());// �ϴ�ˢ��ʱ���������DEAD_INTERVAL
				if(new Date().after(lastRefreshTime.getTime())){
					// ����:���߳�����
				}else{
					//ProcessThreadUtil.realtimeFresh(baseTimer, ((Integer)keyArray[i]).intValue());
					// ���µ�ǰ�߳�����ʱ�䵽���ݿ�
				}
				
			}
		//}
	}
	public void initBizData() throws AppException, SysException {
		
	}
	public void processBizData() throws AppException, SysException {
		
	}
}
