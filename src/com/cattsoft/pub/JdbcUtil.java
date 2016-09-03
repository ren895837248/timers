package com.cattsoft.pub;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cattsoft.pub.exception.SysException;

public class JdbcUtil {
	
	
	/**
	 * 关闭 ResultSet、Statement、Connection
	 * @param rs
	 * @param st
	 * @param con
	 * @throws SysException 
	 */
    public static void close(ResultSet rs, Statement ps, Connection con) throws  SysException
    {
        close(rs,ps);
        close(con);
    }

    /**
     * 关闭ResultSet、Statement
     * @param rs
     * @param st
     * @throws SysException 
     */
    public static void close(ResultSet rs, Statement ps) throws  SysException
    {
        close(rs);
        close(ps);
    }

    /**
     * 关闭Connection
     * @param con
     * @throws ConnectionException 
     */
    public static void close(Connection con) throws SysException
    {
        try
        {
            if(con != null)
                con.close();
        }
        catch(SQLException e) { 
        	throw new SysException("100000","关闭连接时出现异常！",e);
        }
    }

    /**
     * 关闭ResultSet
     * @param rs
     * @throws SysException 
     */
    public static void close(ResultSet rs) throws  SysException
    {
        try
        {
            if(rs != null)
                rs.close();
        }
        catch(SQLException e) { 
        	throw new SysException("100000","关闭连接时出现异常！",e);
        }
    }

    /**
     * 关闭Statement
     * @param st
     * @throws ConnectionException 
     * @throws SysException 
     */
    public static void close(Statement ps) throws  SysException
    {
        try
        {
            if(ps != null)
            	ps.close();
        }
        catch(SQLException e) { 
        	throw new SysException("100000","关闭statement时出现异常！",e);
        }
    }

}
