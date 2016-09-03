package com.cattsoft.socket.vo;

import java.io.Serializable;

public class SpjkParaVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5744878064287338124L;

	private Long spjkOrderID;

	private Long spjkParaID;

	private String para;

	private String paraSEQ;

	private String paraCode;

	private String paraValue;

	private String paraValueOld;

	public String getParaCode() {
		return paraCode;
	}

	public void setParaCode(String paraCode) {
		this.paraCode = paraCode;
	}

	public String getParaSEQ() {
		return paraSEQ;
	}

	public void setParaSEQ(String paraSEQ) {
		this.paraSEQ = paraSEQ;
	}

	public String getParaValue() {
		return paraValue;
	}

	public void setParaValue(String paraValue) {
		this.paraValue = paraValue;
	}

	public String getParaValueOld() {
		return paraValueOld;
	}

	public void setParaValueOld(String paraValueOld) {
		this.paraValueOld = paraValueOld;
	}

	public Long getSpjkOrderID() {
		return spjkOrderID;
	}

	public void setSpjkOrderID(Long spjkOrderID) {
		this.spjkOrderID = spjkOrderID;
	}

	/**
	 * @param spjkParaID
	 *            the spjkParaID to set
	 */
	public void setSpjkParaID(Long spjkParaID) {
		this.spjkParaID = spjkParaID;
	}

	/**
	 * @return the para
	 */
	public String getPara() {
		return para;
	}

	/**
	 * @param para
	 *            the para to set
	 */
	public void setPara(String para) {
		this.para = para;
	}

	/**
	 * @return the spjkParaID
	 */
	public Long getSpjkParaID() {
		return spjkParaID;
	}
	
	public String toString(){
		return "spjkParaID:"+this.spjkParaID.toString();
	}

}
