package com.cattsoft.pub;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;

/**
 * �ر�Timer�������
 * 
 * @author XiaChangJi
 * 
 */
public class TimerManager {

	/**
	 * �ر�Timer�������з������ر�ĳһ��Timer�Ľ��̺����̣߳����������ݿ���Timer�Ľ������߳���Ϣ
	 * 
	 * @param args
	 * @throws SysException
	 * @throws AppException
	 */
	public static void main(String[] args) throws SysException, AppException {
		try {
			InitialTimer timer = new InitialTimer();
			ConnectionFactory.initConnectionFactory(timer);
			if (args == null) {
				throw new RuntimeException("����رսű�����,����Timer����Ϊ���� ");
			}
			String manageType = args[0];// kill,restart
			String timerName = args[1];
			if (manageType.equalsIgnoreCase("kill")) {
				ProcessThreadUtil.killTimer(timer, timerName);
			} else if (manageType.equalsIgnoreCase("restart")) {
				ProcessThreadUtil.killTimer(timer, timerName);
				ProcessThreadUtil.doOSCommand("./Start" + timerName + ".sh");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
