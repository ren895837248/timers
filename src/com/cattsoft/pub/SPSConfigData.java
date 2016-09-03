package com.cattsoft.pub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.cattsoft.pub.dao.Sql;
import com.cattsoft.pub.exception.AppException;
import com.cattsoft.pub.exception.DataCacheException;
import com.cattsoft.pub.exception.SysException;
import com.cattsoft.pub.util.JdbcUtil;
import com.cattsoft.pub.util.StringUtil;
import com.cattsoft.sm.vo.SysAreaConfigSVO;
import com.cattsoft.sm.vo.SysConfigSVO;

public class SPSConfigData {
	private static Logger log = Logger.getLogger(ProcessThreadUtil.class);
	public static boolean getSysConfigFlag(String configId, String localNetId, String areaId,
			String workAreaId, String exchId, String workTypeId) throws AppException, SysException {
		boolean flag = false;
		try {
			SysConfigSVO vo = getSysConfig(configId);// sysConfig
			SysAreaConfigSVO areaVO = new SysAreaConfigSVO();
			if (vo == null) {
				return flag;
			}
			if (SysConstants.SYS_CONFIG_TYPE_PROVINCE.equalsIgnoreCase(vo.getConfigType())) {
				if (SysConstants.TRUE.equals(vo.getCurValue()))
					flag = true;
			} else if (SysConstants.SYS_CONFIG_TYPE_LOCALNET.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(localNetId)) {
					throw new DataCacheException("1000031", "ϵͳ���������ǰ����������ã�������ʱȱ�ٱ�����������", null);
				}
				areaVO = getSysAreaConfig(configId, localNetId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_AREA.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(areaId)) {
					throw new DataCacheException("1000031", "ϵͳ���������ǰ����������ã�������ʱȱ�ٷ�����������", null);
				}
				areaVO = getSysAreaConfig(configId, areaId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_WORKAREA.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(workAreaId)) {
					throw new DataCacheException("1000031", "ϵͳ���������ǰ��������ã�������ʱȱ�ٹ���������", null);
				}
				areaVO = getSysAreaConfig(configId, workAreaId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_EXCH.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(exchId)) {
					throw new DataCacheException("1000031", "ϵͳ���������ǰ��������ã�������ʱȱ�پ��������", null);
				}
				areaVO = getSysAreaConfig(configId, exchId.toString());
			} else if (SysConstants.SYS_CONFIG_TYPE_WORKTYPE.equalsIgnoreCase(vo.getConfigType())) {
				if (StringUtil.isBlank(workTypeId)) {
					throw new DataCacheException("1000031", "ϵͳ���������ǰ������������ã�������ʱȱ�ٹ������Ͳ�����", null);
				}
				areaVO = getSysAreaConfig(configId, workTypeId.toString());
			} else {
				return flag;
			}

			if (areaVO != null) {
				if (SysConstants.TRUE.equals(areaVO.getCurValue()))
					flag = true;
			}

		} catch (Exception ex) {
			throw new SysException("", ex);
		}
		return flag;
	}

	public static SysConfigSVO getSysConfig(String configId) throws AppException, SysException {
		SysConfigSVO sysConfigSVO = null;
		log.debug("getSysConfig begin!configId[" + configId + "]");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		try {
			// �ж���������Ƿ�Ϸ�
			if (configId == null || "".equals(configId))
				throw new AppException("", "��ѯsysConfig����configIdΪ�գ�");

			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();

			// 2.��ȡ��̨Ӧ�õ�ID
			sql = new Sql("SELECT * FROM SYS_CONFIG WHERE CONFIG_ID=:configId");
			sql.setString("configId", configId);
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			rs = ps.executeQuery();
			if (rs.next()) {
				sysConfigSVO = new SysConfigSVO();
				sysConfigSVO.setConfigId(rs.getString("CONFIG_ID"));
				sysConfigSVO.setName(rs.getString("NAME"));
				sysConfigSVO.setSystemName(rs.getString("SYSTEM_NAME"));
				sysConfigSVO.setConfigType(rs.getString("CONFIG_TYPE"));
				sysConfigSVO.setCurValue(rs.getString("CUR_VALUE"));
				sysConfigSVO.setCreateDate(rs.getDate("CREATE_DATE"));
			
				log.debug("getSysConfig end!SysConfigId[" + sysConfigSVO.getConfigId() + "],curValue["
						+ sysConfigSVO.getCurValue() + "]");
			}
		} catch (SysException e) {
			e.printStackTrace();
			return sysConfigSVO;
		} catch (AppException e) {
			e.printStackTrace();
			return sysConfigSVO;
		} catch (Exception e) {
			e.printStackTrace();
			return sysConfigSVO;
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				e.printStackTrace();
				return sysConfigSVO;
			}
		}
		return sysConfigSVO;
	}

	public static SysAreaConfigSVO getSysAreaConfig(String configId, String spAreaId)
			throws AppException, SysException {
		SysAreaConfigSVO sysAreaConfigSVO = null;
		log.debug("getSysAreaConfig begin!configId[" + configId + "]spAreaId[" + spAreaId + "]");
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Sql sql = null;
		try {
			// �ж���������Ƿ�Ϸ�
			if (configId == null || "".equals(configId))
				throw new AppException("", "��ѯsysAreaConfig����configIdΪ�գ�");
			if (spAreaId == null || "".equals(spAreaId))
				throw new AppException("", "��ѯsysAreaConfig����spAreaIdΪ�գ�");

			// 1.������ݿ�����
			conn = ConnectionFactory.createConnection();

			// 2.��ȡ��̨Ӧ�õ�ID
			sql = new Sql("SELECT * FROM SYS_AREA_CONFIG WHERE CONFIG_ID=:configId");
			sql.setString("configId", configId);
			ps = conn.prepareStatement(sql.getSql());
			sql.fillParams(ps);
			rs = ps.executeQuery();
			while (rs.next()) {
				sysAreaConfigSVO = new SysAreaConfigSVO();
				sysAreaConfigSVO.setSysAreaConfigId(rs.getString("SYS_AREA_CONFIG_ID"));
				sysAreaConfigSVO.setConfigId(rs.getString("CONFIG_ID"));
				sysAreaConfigSVO.setSpAreaId(rs.getString("SP_AREA_ID"));
				sysAreaConfigSVO.setCurValue(rs.getString("CUR_VALUE"));
			}
			log.debug("getSysAreaConfig end!configId[" + sysAreaConfigSVO.getConfigId()
					+ "],spAreaId[" + sysAreaConfigSVO.getSpAreaId() + "],curValue["
					+ sysAreaConfigSVO.getCurValue() + "]");
		} catch (SysException e) {
			e.printStackTrace();
			return sysAreaConfigSVO;
		} catch (AppException e) {
			e.printStackTrace();
			return sysAreaConfigSVO;
		} catch (Exception e) {
			e.printStackTrace();
			return sysAreaConfigSVO;
		} finally {
			try {
				JdbcUtil.close(ps);
				if (conn != null) {
					ConnectionFactory.closeConnection();
				}
			} catch (SysException e) {
				e.printStackTrace();
				return sysAreaConfigSVO;
			}
		}
		return sysAreaConfigSVO;
	}
}
