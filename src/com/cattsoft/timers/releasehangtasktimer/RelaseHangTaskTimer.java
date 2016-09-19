package com.cattsoft.timers.releasehangtasktimer;

import org.apache.log4j.Logger;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.timers.BaseTimer;

public class RelaseHangTaskTimer extends BaseTimer {

	static{
		log = Logger.getLogger(RelaseHangTaskTimer.class);
	}
	
	
	@Override
	public void checkExist() throws AppException, SysException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initArguments(String[] args) throws AppException {
		threadNumber = Integer.parseInt(args[0]);
		localNetIds = args[1];
		try {
			if(args.length>2){
				preTimeLimit = Integer.parseInt(args[2]);
			}else{
				preTimeLimit = 60;
			}
		} catch (Exception e) {
			e.printStackTrace();
			preTimeLimit = 60;
		}
		
	}

	@Override
	public void initEJBContext() throws AppException, SysException {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) {
		try {
			new RelaseHangTaskTimer().start(args);
		} catch (Exception e) {
			log.error("SoParseTimer 线程启动时出现错误，请检查启动脚本或程序！！！");
			e.printStackTrace();
		}
	}
	

}
