/**
 * 
 */
package com.cattsoft.timers;

import javax.naming.Context;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;

/**
 * �ӿ���:�ṩ�˽ӿڷ���,BaseTimer��InitialTimerʵ�ִ˽ӿ�
 * @author WuGang
 *
 */
public interface IBaseDBCommand {
	public String getConnType();
	public String getJndiName();
	public String getJdbcDriver();
	public String getJdbcUrl();
	public String getDbUserName();
	public String getDbPassword();
	public int getMaxWait();
	public int getMaxActive();
	public int getMaxIdle();
	public Context initialContext() throws AppException,SysException;
}
