package com.cattsoft.timers.devicestroagesynctimer;

import org.apache.log4j.Logger;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.timers.BaseTimer;

public class DeviceStroageSyncTimer extends BaseTimer {

	static{
		log = Logger.getLogger(DeviceStroageSyncTimer.class);
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
			new DeviceStroageSyncTimer().start(args);
		} catch (Exception e) {
			log.error("SoParseTimer 线程启动时出现错误，请检查启动脚本或程序！！！");
			e.printStackTrace();
		}
	}
	
	
}
