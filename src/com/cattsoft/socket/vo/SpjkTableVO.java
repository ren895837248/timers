package com.cattsoft.socket.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** @author Hibernate CodeGenerator */
public class SpjkTableVO implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7803192390346812326L;

	/** identifier field */
    private Long spjkId;

    /** persistent field */
    private String jkType;

    /** persistent field */
    private int priority;

    /** nullable persistent field */
    private String soNbr;

    private String prodID;
    /** nullable persistent field */
    private Long servId;

    /** persistent field */
    private int localNetId;

    /** persistent field */
    private int areaId;

    /** persistent field */
    private String areaCode;

    /** persistent field */
    private String accNbr;

    /** persistent field */
    private Long workItemId;

    /** persistent field */
    private String workItemType;

    /** persistent field */
    private int servSpecId;

    /** persistent field */
    private int sendCount;

    /** persistent field */
    private Date createTime;

    /** nullable persistent field */
    private Date sendTime;

    /** nullable persistent field */
    private Date recvTime;

    /** persistent field */
    private String sts;

    /** nullable persistent field */
    private String pauseOper;

    /** nullable persistent field */
    private Long staffId;

    /** nullable persistent field */
    private String remarks;

    /** nullable persistent field */
    private String errCode;

    /** nullable persistent field */
    private String errMsg;

    /** persistent field */
    private String backfillFlag;
    
    private List spjkOrderVOList;
    
    private String DataRoute;
    
    private String standardCode ;

    /** full constructor */
    public SpjkTableVO(String jkType, int priority, String soNbr, Long servId, int localNetId, int areaId, String areaCode, String accNbr, Long workItemId, String workItemType, int servSpecId, int sendCount, Date createTime, Date sendTime, Date recvTime, String sts, String pauseOper, Long staffId, String remarks, String errCode, String errMsg, String backfillFlag) {
        this.jkType = jkType;
        this.priority = priority;
        this.soNbr = soNbr;
        this.servId = servId;
        this.localNetId = localNetId;
        this.areaId = areaId;
        this.areaCode = areaCode;
        this.accNbr = accNbr;
        this.workItemId = workItemId;
        this.workItemType = workItemType;
        this.servSpecId = servSpecId;
        this.sendCount = sendCount;
        this.createTime = createTime;
        this.sendTime = sendTime;
        this.recvTime = recvTime;
        this.sts = sts;
        this.pauseOper = pauseOper;
        this.staffId = staffId;
        this.remarks = remarks;
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.backfillFlag = backfillFlag;
    }

    /** default constructor */
    public SpjkTableVO() {
    }

    /** minimal constructor */
    public SpjkTableVO(String jkType, int priority, int localNetId, int areaId, String areaCode, String accNbr, Long workItemId, String workItemType, int servSpecId, int sendCount, Date createTime, String sts, String backfillFlag) {
        this.jkType = jkType;
        this.priority = priority;
        this.localNetId = localNetId;
        this.areaId = areaId;
        this.areaCode = areaCode;
        this.accNbr = accNbr;
        this.workItemId = workItemId;
        this.workItemType = workItemType;
        this.servSpecId = servSpecId;
        this.sendCount = sendCount;
        this.createTime = createTime;
        this.sts = sts;
        this.backfillFlag = backfillFlag;
    }

    public Long getSpjkId() {
        return this.spjkId;
    }

    public void setSpjkId(Long spjkId) {
        this.spjkId = spjkId;
    }

    public String getJkType() {
        return this.jkType;
    }

    public void setJkType(String jkType) {
        this.jkType = jkType;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getSoNbr() {
        return this.soNbr;
    }

    public void setSoNbr(String soNbr) {
        this.soNbr = soNbr;
    }

    public Long getServId() {
        return this.servId;
    }

    public void setServId(Long servId) {
        this.servId = servId;
    }

    public int getLocalNetId() {
        return this.localNetId;
    }

    public void setLocalNetId(int localNetId) {
        this.localNetId = localNetId;
    }

    public int getAreaId() {
        return this.areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAccNbr() {
        return this.accNbr;
    }

    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr;
    }

    public Long getWorkItemId() {
        return this.workItemId;
    }

    public void setWorkItemId(Long workItemId) {
        this.workItemId = workItemId;
    }

    public String getWorkItemType() {
        return this.workItemType;
    }

    public void setWorkItemType(String workItemType) {
        this.workItemType = workItemType;
    }

    public int getServSpecId() {
        return this.servSpecId;
    }

    public void setServSpecId(int servSpecId) {
        this.servSpecId = servSpecId;
    }

    public int getSendCount() {
        return this.sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getRecvTime() {
        return this.recvTime;
    }

    public void setRecvTime(Date recvTime) {
        this.recvTime = recvTime;
    }

    public String getSts() {
        return this.sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
    }

    public String getPauseOper() {
        return this.pauseOper;
    }

    public void setPauseOper(String pauseOper) {
        this.pauseOper = pauseOper;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getBackfillFlag() {
        return this.backfillFlag;
    }

    public void setBackfillFlag(String backfillFlag) {
        this.backfillFlag = backfillFlag;
    }

	/**
	 * @return the spjkOrderVOList
	 */
	public List getSpjkOrderVOList() {
		return spjkOrderVOList;
	}

	/**
	 * @param spjkOrderVOList the spjkOrderVOList to set
	 */
	public void setSpjkOrderVOList(List spjkOrderVOList) {
		this.spjkOrderVOList = spjkOrderVOList;
	}

	/**
	 * @return the prodID
	 */
	public String getProdID() {
		return prodID;
	}

	/**
	 * @param prodID the prodID to set
	 */
	public void setProdID(String prodID) {
		this.prodID = prodID;
	}

	public String getDataRoute() {
		return DataRoute;
	}

	public void setDataRoute(String dataRoute) {
		DataRoute = dataRoute;
	}

	public String getStandardCode() {
		return standardCode;
	}

	public void setStandardCode(String standardCode) {
		this.standardCode = standardCode;
	}

	/*public String toString(){
		return "spjkId:"+this.spjkId.toString()+TestSpjkTableTimerDAO.toString(spjkOrderVOList);
	}*/
	

}

