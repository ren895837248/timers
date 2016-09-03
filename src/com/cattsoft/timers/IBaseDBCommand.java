/**
 * 
 */
package com.cattsoft.timers;

import javax.naming.Context;

import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.SysException;

/**
 * 接口类:提供了接口方法,BaseTimer和InitialTimer实现此接口
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
