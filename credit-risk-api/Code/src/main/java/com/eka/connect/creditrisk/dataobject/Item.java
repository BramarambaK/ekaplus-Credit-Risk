package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;
import java.util.Date;

public class Item {

	private String contractItemRefNo;
	private String payInCurrency;
	private BigDecimal value;
	private BigDecimal valueInCounterPartyCurrency;
	private Date fromPeriod;
	private Date toPeriod;
	private String limitRefNo;
	
	private TCCRDetails tccrDetails;
	
	//below field is used for calculation purpose only
	private boolean isConsideredForCalculation;

	public String getContractItemRefNo() {
		return contractItemRefNo;
	}

	public void setContractItemRefNo(String contractItemRefNo) {
		this.contractItemRefNo = contractItemRefNo;
	}

	public String getPayInCurrency() {
		return payInCurrency;
	}

	public void setPayInCurrency(String payInCurrency) {
		this.payInCurrency = payInCurrency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Date getFromPeriod() {
		return fromPeriod;
	}

	public void setFromPeriod(Date fromPeriod) {
		this.fromPeriod = fromPeriod;
	}

	public Date getToPeriod() {
		return toPeriod;
	}

	public void setToPeriod(Date toPeriod) {
		this.toPeriod = toPeriod;
	}

	public boolean isConsideredForCalculation() {
		return isConsideredForCalculation;
	}

	public void setConsideredForCalculation(boolean isConsideredForCalculation) {
		this.isConsideredForCalculation = isConsideredForCalculation;
	}

	public BigDecimal getValueInCounterPartyCurrency() {
		return valueInCounterPartyCurrency;
	}

	public void setValueInCounterPartyCurrency(
			BigDecimal valueInCounterPartyCurrency) {
		this.valueInCounterPartyCurrency = valueInCounterPartyCurrency;
	}

	public TCCRDetails getTccrDetails() {
		return tccrDetails;
	}

	public void setTccrDetails(TCCRDetails tccrDetails) {
		this.tccrDetails = tccrDetails;
	}

	public String getLimitRefNo() {
		return limitRefNo;
	}

	public void setLimitRefNo(String limitRefNo) {
		this.limitRefNo = limitRefNo;
	}

 

}
