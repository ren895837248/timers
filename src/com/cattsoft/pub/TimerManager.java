package com.cattsoft.pub;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;

/**
 * 关闭Timer所起进程
 * 
 * @author XiaChangJi
 * 
 */
public class TimerManager {

	/**
	 * 关闭Timer的主运行方法，关闭某一个Timer的进程和子线程，并更新数据库中Timer的进程与线程信息
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
				throw new RuntimeException("请检查关闭脚本参数,请以Timer类名为参数 ");
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
