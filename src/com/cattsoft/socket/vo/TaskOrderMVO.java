package com.cattsoft.socket.vo;

import org.apache.commons.lang.StringUtils;

public class TaskOrderMVO extends TaskOrder {

	private String culmName;
	private String closeFlag;
	private String pageCode;
	private String staffId;
	private String orgTypeId;
	private String imagValue;
	private String imagCustLevel;
	private String fromSystemName;
	private String actTypeName;
	private String toTypeName;
	private String bookedFlagName;
	private String busiStsName;
	private String runStsName;
	private String haltName;
	private String bookTime;
	private String areaName;
	private String notifyType;
	private String notifyNbr;
	private String notifyRemarks;
	private String detailStaffId;
	private String detailStaffName;
	private String curStaffId;
	private String curStaffName;
	private String specServName;
	private String orgName; 
	private String printFlag;
	private String overTimeFlag;
	private String bookFlagName;
	private String failReasonName;
	private String mosFlagName;
	private String coNbr;
	
	
	//WO_TYPE PROD_ID SERV_OFFER_ID SO_CAT IOM_WO_NBR APPL_DATE BOOK_TIME BUSINESS_ID NGN_TYPE
	private String woType;
	private String prodId;
	private String servOfferId;
	private String soCat;
	private String iomWoNbr;
	private String applDate;
	private String businessId;
	private String ngnType;
	private String coStaffId;
	private String tookDate; 
	private String vipType;
	private String vipTypeName;
	
	private String soExtsId;
	private String mFlag;
	private String dealFlag;
	

	
	public String getDealFlag() {
		return dealFlag;
	}

	public void setDealFlag(String dealFlag) {
		this.dealFlag = dealFlag;
	}

	public String getmFlag() {
		return mFlag;
	}

	public void setmFlag(String mFlag) {
		this.mFlag = mFlag;
	}

	public String getSoExtsId() {
		return soExtsId;
	}

	public void setSoExtsId(String soExtsId) {
		this.soExtsId = soExtsId;
	}

	public String getVipTypeName() {
		
		return vipTypeName;
	}

	public void setVipTypeName(String vipTypeName) {
		this.vipTypeName = vipTypeName;
	}

	public String getVipType() {
		return vipType;
	}

	public void setVipType(String vipType) {
		this.vipType = vipType;
//		if(StringUtils.isBlank(vipTypeName)){
//			CacheManager cacheManager = CacheManager.getInstance();
//			vipTypeName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "SO_CUST.VIP_TYPE."
//							+ vipType);
//		}
	}

	public String getCoStaffId() {
		return coStaffId;
	}

	public void setCoStaffId(String coStaffId) {
		this.coStaffId = coStaffId;
	}

	public String getWoType() {
		return woType;
	}

	public void setWoType(String woType) {
		this.woType = woType;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public String getServOfferId() {
		return servOfferId;
	}

	public void setServOfferId(String servOfferId) {
		this.servOfferId = servOfferId;
	}

	public String getSoCat() {
		return soCat;
	}

	public void setSoCat(String soCat) {
		this.soCat = soCat;
	}

	public String getIomWoNbr() {
		return iomWoNbr;
	}

	public void setIomWoNbr(String iomWoNbr) {
		this.iomWoNbr = iomWoNbr;
	}

	public String getApplDate() {
		return applDate;
	}

	public void setApplDate(String applDate) {
		this.applDate = applDate;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getNgnType() {
		return ngnType;
	}

	public void setNgnType(String ngnType) {
		this.ngnType = ngnType;
	}

	public String getCoNbr() {
		return coNbr;
	}

	public void setCoNbr(String coNbr) {
		this.coNbr = coNbr;
	}

	public String getBookFlagName() {
		return bookFlagName;
	}
	
	@Override
	public void setBookFlag(String bookFlag) {
		super.setBookFlag(bookFlag);
		
	}

	public void setBookFlagName(String bookFlagName) {
		this.bookFlagName = bookFlagName;
	}

	public String getPrintFlag() {
		return printFlag;
	}

	public void setPrintFlag(String printFlag) {
		this.printFlag = printFlag;
	}

	public String getOverTimeFlag() {
		return overTimeFlag;
	}

	public void setOverTimeFlag(String overTimeFlag) {
		this.overTimeFlag = overTimeFlag;
	}

	public String getSpecServName() {
//		if (StringUtils.isBlank(specServName)&&!StringUtils.isBlank(specServId)) {
//            CacheManager cacheManager = CacheManager.getInstance();
//            specServName = (String) cacheManager.get("table.cache.idvalue.specserv", specServId);
//        }
		return specServName;
	}

	public void setSpecServName(String specServName) {
		this.specServName = specServName;
	}
	
	@Override
	public void setSpecServId(String specServId) {
		super.setSpecServId(specServId);
		
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public String getNotifyNbr() {
		return notifyNbr;
	}

	public void setNotifyNbr(String notifyNbr) {
		this.notifyNbr = notifyNbr;
	}

	public String getNotifyRemarks() {
		return notifyRemarks;
	}

	public void setNotifyRemarks(String notifyRemarks) {
		this.notifyRemarks = notifyRemarks;
	}

	public String getDetailStaffId() {
		return detailStaffId;
	}

	public void setDetailStaffId(String detailStaffId) {
		this.detailStaffId = detailStaffId;
	}

	public String getDetailStaffName() {
		return detailStaffName;
	}

	public void setDetailStaffName(String detailStaffName) {
		this.detailStaffName = detailStaffName;
	}

	public String getCurStaffId() {
		return curStaffId;
	}

	public void setCurStaffId(String curStaffId) {
		this.curStaffId = curStaffId;
	}

	public String getCurStaffName() {
		return curStaffName;
	}

	public void setCurStaffName(String curStaffName) {
		this.curStaffName = curStaffName;
	}

	public String getAreaName() {
//		if (StringUtils.isBlank(areaName) && !StringUtils.isBlank(areaId)) {
//			AreaMVO area = (AreaMVO) CacheManager.getInstance().get(
//					"table.cache.codeentity.area", areaId);
//			if (area != null && area.getName() != null
//					&& !area.getName().equals("")) {
//				areaName = area.getName();
//			}
//		}
		return areaName;
	}
	@Override
	public void setAreaId(String areaId) { 
		super.setAreaId(areaId);
		
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getBookTime() {
		return bookTime;
	}

	public void setBookTime(String bookTime) {
		this.bookTime = bookTime;
	}

	public String getBusiStsName() {
//		if (StringUtils.isBlank(busiStsName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			busiStsName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.BUSI_STS."
//							+ busiSts)+"/"+busiStsDate;
//		}
		return busiStsName;
	}

	public void setBusiStsName(String busiStsName) {
		this.busiStsName = busiStsName;
	}
	
	@Override
	public void setBusiSts(String busiSts) {
		super.setBusiSts(busiSts);
		
	}

	public String getRunStsName() {
//		if (StringUtils.isBlank(runStsName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			runStsName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.RUN_STS."
//							+ runSts);
//		}
		return runStsName;
	}

	public void setRunStsName(String runStsName) {
		this.runStsName = runStsName;
	}
	
	@Override
	public void setRunSts(String runSts) {
		super.setRunSts(runSts);
		
	}

	public String getHaltName() {
//		if (StringUtils.isBlank(haltName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			haltName = (String) cacheManager.get("table.cache.idvalue.status",
//					"TASK_ORDER.HALT." + halt);
//		}
		return haltName;
	}

	public void setHaltName(String haltName) {
		this.haltName = haltName;
	}
	
	@Override
	public void setHalt(String halt) {
		super.setHalt(halt);
		
	}

	public String getBookedFlagName() {
//		if (StringUtils.isBlank(bookedFlagName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			bookedFlagName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.BOOKED_FLAG."
//							+ bookedFlag);
//		}
		return bookedFlagName;
	}

	public void setBookedFlagName(String bookedFlagName) {
		this.bookedFlagName = bookedFlagName;
	}
	
	@Override
	public void setBookedFlag(String bookedFlag) {
		super.setBookedFlag(bookedFlag);
		
	}

	public String getToTypeName() {
//		if (StringUtils.isBlank(toTypeName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			toTypeName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.TO_TYPE."
//							+ toType);
//		}
		return toTypeName;
	}

	public void setToTypeName(String toTypeName) {
		this.toTypeName = toTypeName;
	}
	
	@Override
	public void setToType(String toType) {
		super.setToType(toType);


	}

	public String getFromSystemName() {
//		if (StringUtils.isBlank(fromSystemName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			fromSystemName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.FROM_SYSTEM."
//							+ fromSystem);
//		}
		return fromSystemName;
	}

	public void setFromSystemName(String fromSystemName) {
		this.fromSystemName = fromSystemName;
	}
	
	@Override
	public void setFromSystem(String fromSystem) {
		super.setFromSystem(fromSystem);
		
	}

	public String getActTypeName() {
//		if (StringUtils.isBlank(actTypeName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			actTypeName = (String) cacheManager.get(
//					"table.cache.idvalue.status", "TASK_ORDER.ACT_TYPE."
//							+ actType);
//		}
		return actTypeName;
	}

	public void setActTypeName(String actTypeName) {
		this.actTypeName = actTypeName;
	}
	
	@Override
	public void setActType(String actType) {
		super.setActType(actType);
	}

	public String getCulmName() {
		return culmName;
	}

	public void setCulmName(String culmName) {
		this.culmName = culmName;
	}

	public String getCloseFlag() {
		return closeFlag;
	}

	public void setCloseFlag(String closeFlag) {
		this.closeFlag = closeFlag;
	}

	public String getPageCode() {
		return pageCode;
	}

	public void setPageCode(String pageCode) {
		this.pageCode = pageCode;
	}

	public String getStaffId() {
		return staffId;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
	}

	public String getOrgTypeId() {
		return orgTypeId;
	}

	public void setOrgTypeId(String orgTypeId) {
		this.orgTypeId = orgTypeId;
	}

	public String getImagValue() {
		return imagValue;
	}

	public void setImagValue(String imagValue) {
		this.imagValue = imagValue;
	}

	public String getImagCustLevel() {
		return imagCustLevel;
	}

	public void setImagCustLevel(String imagCustLevel) {
		this.imagCustLevel = imagCustLevel;
	}

	public String getOrgName() {
//		if (StringUtils.isBlank(orgName)) {
//			PositionsMVO pos = new PositionsMVO();
//			pos.setPositionsId(orgId);
//			pos.setSts("A");
//			IPositionsMDAO positionMDAO = (IPositionsMDAO) BeanUtil
//					.getBean("positionsMDAO");
//			pos = positionMDAO.queryBean(pos);
//			if(pos!=null){
//				orgName=pos.getName();
//			}
//		}
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@Override
	public void setOrgId(String orgId) {
		super.setOrgId(orgId);
	}
	
	public String getFailReasonName() {
//		if (StringUtils.isBlank(failReasonName)) {
//			CacheManager cacheManager = CacheManager.getInstance();
//			failReasonName = (String) cacheManager.get(
//					"table.cache.idvalue.failreason", failReasonId);
//		}
		return failReasonName;
	}

	public void setFailReasonName(String failReasonName) {
		this.failReasonName = failReasonName;
	}
	
	@Override
	public void setFailReasonId(String failReasonId) {
		super.setFailReasonId(failReasonId);
	}

	public String getMosFlagName() {
		return mosFlagName;
	}

	public void setMosFlagName(String mosFlagName) {
		this.mosFlagName = mosFlagName;
	}

	@Override
	public void setMosFlag(String mosFlag) {
		super.setMosFlag(mosFlag);
	}
	
	public String getTookDate() {
		return tookDate;
	}

	public void setTookDate(String tookDate) {
		this.tookDate = tookDate;
	}
	
}