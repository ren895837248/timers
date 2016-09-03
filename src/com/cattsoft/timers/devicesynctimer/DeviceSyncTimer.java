package com.cattsoft.timers.devicesynctimer;

import org.apache.log4j.Logger;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.timers.BaseTimer;

public class DeviceSyncTimer extends BaseTimer {

	static{
		log = Logger.getLogger(DeviceSyncTimer.class);
	}
	
	
	@Override
	public void checkExist() throws AppException, SysException {

	}

	@Override
	public void initArguments(String[] args) throws AppException {
		threadNumber = Integer.parseInt(args[0]);
	}

	@Override
	public void initEJBContext() throws AppException, SysException {

	}
	
	
	public static void main(String[] args) {
		try {
			new DeviceSyncTimer().start(args);
		} catch (Exception e) {
			log.error("SoParseTimer 线程启动时出现错误，请检查启动脚本或程序！！！");
			e.printStackTrace();
		}
	}
	
	
}
