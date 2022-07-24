package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;
import java.util.Date;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown=true)
public class CounterpartyExposure {
	
	private boolean consideredForCreditCheck;
	

	private String counterparty;

	private String counterpartyGroup;
	
	private String counterpartyRefNo;//businessPartnerRefNo;
	
	private String exposureType;
	
	private BigDecimal value = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);
	
	private String cpid;
	private String cpgid;
	 
	private String  valueCurrency;
	
	private String decisionRefNo;
	
	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date fromDate;
	
	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date toDate;
	
	
	private String contractRefNo;
	
	private String contractItemRefNo;
	
	private String gmrRefNo;

	public boolean isConsideredForCreditCheck() {
		return consideredForCreditCheck;
	}

	public void setConsideredForCreditCheck(boolean consideredForCreditCheck) {
		this.consideredForCreditCheck = consideredForCreditCheck;
	}

	public String getCounterparty() {
		return counterparty;
	}

	public void setCounterparty(String counterparty) {
		this.counterparty = counterparty;
	}

	public String getCounterpartyGroup() {
		return counterpartyGroup;
	}

	public void setCounterpartyGroup(String counterpartyGroup) {
		this.counterpartyGroup = counterpartyGroup;
	}

	public String getCounterpartyRefNo() {
		return counterpartyRefNo;
	}

	public void setCounterpartyRefNo(String counterpartyRefNo) {
		this.counterpartyRefNo = counterpartyRefNo;
	}

	public String getExposureType() {
		return exposureType;
	}

	public void setExposureType(String exposureType) {
		this.exposureType = exposureType;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getCpid() {
		return cpid;
	}

	public void setCpid(String cpid) {
		this.cpid = cpid;
	}

	public String getCpgid() {
		return cpgid;
	}

	public void setCpgid(String cpgid) {
		this.cpgid = cpgid;
	}

	public String getValueCurrency() {
		return valueCurrency;
	}

	public void setValueCurrency(String valueCurrency) {
		this.valueCurrency = valueCurrency;
	}

	public String getDecisionRefNo() {
		return decisionRefNo;
	}

	public void setDecisionRefNo(String decisionRefNo) {
		this.decisionRefNo = decisionRefNo;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
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

	public String getGmrRefNo() {
		return gmrRefNo;
	}

	public void setGmrRefNo(String gmrRefNo) {
		this.gmrRefNo = gmrRefNo;
	}

	 
}
