package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CreditCheckRequestResponse {

	private String requestID;
	private String entityRefNo;
	private String operationType;
	private String eventName;
	private String counterParty;
	private String counterPartyGroup;
	private String limitRefNo;
	private String contractType;
	private String paymenTerm;
	private String contractRefNo;
	private String contractItemRefNo;
	private BigDecimal value;
	private String fromPeriod;
	private String toPeriod;
	private String invoiceCounterParty;
	private String GMRRefNo;
	private String responseStatus;
	private String responseBlockType;
	private String responseMessage;
	private String requestBy;
	private String requestedOn;

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public String getEntityRefNo() {
		return entityRefNo;
	}

	public void setEntityRefNo(String entityRefNo) {
		this.entityRefNo = entityRefNo;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getCounterParty() {
		return counterParty;
	}

	public void setCounterParty(String counterParty) {
		this.counterParty = counterParty;
	}

	public String getCounterPartyGroup() {
		return counterPartyGroup;
	}

	public void setCounterPartyGroup(String counterPartyGroup) {
		this.counterPartyGroup = counterPartyGroup;
	}

	public String getLimitRefNo() {
		return limitRefNo;
	}

	public void setLimitRefNo(String limitRefNo) {
		this.limitRefNo = limitRefNo;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public String getPaymenTerm() {
		return paymenTerm;
	}

	public void setPaymenTerm(String paymenTerm) {
		this.paymenTerm = paymenTerm;
	}

	public String getContractRefNo() {
		return contractRefNo;
	}

	public void setContractRefNo(String contractRefNo) {
		this.contractRefNo = contractRefNo;
	}

	public String getContractItemRefNo() {
		return contractItemRefNo;
	}

	public void setContractItemRefNo(String contractItemRefNo) {
		this.contractItemRefNo = contractItemRefNo;
	}

	public String getInvoiceCounterParty() {
		return invoiceCounterParty;
	}

	public void setInvoiceCounterParty(String invoiceCounterParty) {
		this.invoiceCounterParty = invoiceCounterParty;
	}

	public String getGMRRefNo() {
		return GMRRefNo;
	}

	public void setGMRRefNo(String gMRRefNo) {
		GMRRefNo = gMRRefNo;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponseBlockType() {
		return responseBlockType;
	}

	public void setResponseBlockType(String responseBlockType) {
		this.responseBlockType = responseBlockType;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getRequestBy() {
		return requestBy;
	}

	public void setRequestBy(String requestBy) {
		this.requestBy = requestBy;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getFromPeriod() {
		return fromPeriod;
	}

	public void setFromPeriod(String fromPeriod) {
		this.fromPeriod = fromPeriod;
	}

	public String getToPeriod() {
		return toPeriod;
	}

	public void setToPeriod(String toPeriod) {
		this.toPeriod = toPeriod;
	}

	public String getRequestedOn() {
		return requestedOn;
	}

	public void setRequestedOn(String requestedOn) {
		this.requestedOn = requestedOn;
	}

}
