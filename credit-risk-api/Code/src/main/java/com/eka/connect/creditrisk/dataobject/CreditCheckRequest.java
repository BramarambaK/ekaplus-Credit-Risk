package com.eka.connect.creditrisk.dataobject;

import java.util.List;

public class CreditCheckRequest {

	
	private String entityRefNo;
	private String entityType;
	private String eventName;
	private String contractType;
	private List<TCCRDetails> tccrDetails;
	
	
	public String getEntityRefNo() {
		return entityRefNo;
	}
	public void setEntityRefNo(String entityRefNo) {
		this.entityRefNo = entityRefNo;
	}

	public List<TCCRDetails> getTccrDetails() {
		return tccrDetails;
	}
	public void setTccrDetails(List<TCCRDetails> tccrDetails) {
		this.tccrDetails = tccrDetails;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getContractType() {
		return contractType;
	}
	public void setContractType(String contractType) {
		this.contractType = contractType;
	}
}
