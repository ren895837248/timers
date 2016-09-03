package com.cattsoft.socket.vo;

import java.io.Serializable;
import java.util.List;

public class SpjkOrderVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3153749048173056318L;

	private Long spjkOrderID;

	private Long spjkId;

	private String orderCode;

	private String orderSEQ;
	
	private String orderGroupSEQ;

	private String sts;

	private String ERRCode;

	private String ERR_MSG;

	private String actType;

	private List spjkParaVOList;

	public String getActType() {
		return actType;
	}

	public void setActType(String actType) {
		this.actType = actType;
	}

	public String getERR_MSG() {
		return ERR_MSG;
	}

	public void setERR_MSG(String err_msg) {
		ERR_MSG = err_msg;
	}

	public String getERRCode() {
		return ERRCode;
	}

	public void setERRCode(String code) {
		ERRCode = code;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getOrderSEQ() {
		return orderSEQ;
	}

	public void setOrderSEQ(String orderSEQ) {
		this.orderSEQ = orderSEQ;
	}

	public Long getSpjkId() {
		return spjkId;
	}

	public void setSpjkId(Long spjkId) {
		this.spjkId = spjkId;
	}

	public Long getSpjkOrderID() {
		return spjkOrderID;
	}

	public void setSpjkOrderID(Long spjkOrderID) {
		this.spjkOrderID = spjkOrderID;
	}

	/**
	 * @return the spjkParaVOList
	 */
	public List getSpjkParaVOList() {
		return spjkParaVOList;
	}

	/**
	 * @param spjkParaVOList
	 *            the spjkParaVOList to set
	 */
	public void setSpjkParaVOList(List spjkParaVOList) {
		this.spjkParaVOList = spjkParaVOList;
	}

	/**
	 * @return the sts
	 */
	public String getSts() {
		return sts;
	}

	/**
	 * @param sts
	 *            the sts to set
	 */
	public void setSts(String sts) {
		this.sts = sts;
	}

	/**
	 * @return the orderGroupSEQ
	 */
	public String getOrderGroupSEQ() {
		return orderGroupSEQ;
	}

	/**
	 * @param orderGroupSEQ the orderGroupSEQ to set
	 */
	public void setOrderGroupSEQ(String orderGroupSEQ) {
		this.orderGroupSEQ = orderGroupSEQ;
	}
	
	/*public String toString(){
		return "spjkOrderId:"+this.spjkOrderID.toString()+TestSpjkTableTimerDAO.toString(spjkParaVOList);
	}*/

}
