package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;
import java.util.Date;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitMaintenanceDetails {

	private String limitRefNo;
	private String cpid;
	private String counterpartyGroupName;// holds both counterparty and
											// counterparty group
	private String counterpartyGroupNameDisplayName;
	private String creditLimitSource;
	private String creditLimitSourceDisplayName;
	private String creditLimitType;
	private String creditLimitTypeDisplayName;
	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date fromPeriod;
	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date toPeriod;

	private BigDecimal amount;

	// field to hold double amount in new field. Reason being , in connect all
	// number fields are saved as string
	@JsonAlias("amount")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String limitStatus;
	private String limitStatusDisplayName;

	private boolean isExposureConsideredBalanceCharting;
	private BigDecimal tccrAmount = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);
	private boolean isTccrAmountAllocated;
	private boolean considerForBalanceCharting;

	// Below fields are for calculation.
	private BigDecimal exposureAmount = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);

	private BigDecimal balance = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);

	// for ordering
	private int chartingOrder = 1;
	
	private boolean isBalanceCalculated;
	

	public String getLimitRefNo() {
		return limitRefNo;
	}

	public void setLimitRefNo(String limitRefNo) {
		this.limitRefNo = limitRefNo;
	}

	public String getCpid() {
		return cpid;
	}

	public void setCpid(String cpid) {
		this.cpid = cpid;
	}

	public String getCounterpartyGroupName() {
		return counterpartyGroupName;
	}

	public void setCounterpartyGroupName(String counterpartyGroupName) {
		this.counterpartyGroupName = counterpartyGroupName;
	}

	public String getCreditLimitSource() {
		return creditLimitSource;
	}

	public void setCreditLimitSource(String creditLimitSource) {
		this.creditLimitSource = creditLimitSource;
	}

	public String getCreditLimitType() {
		return creditLimitType;
	}

	public void setCreditLimitType(String creditLimitType) {
		this.creditLimitType = creditLimitType;
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

	public String getLimitStatus() {
		return limitStatus;
	}

	public void setLimitStatus(String limitStatus) {
		this.limitStatus = limitStatus;
	}


	public BigDecimal getExposureAmount() {
		return exposureAmount;
	}

	public void setExposureAmount(BigDecimal exposureAmount) {
		this.exposureAmount = exposureAmount;
	}

	public BigDecimal getBalance() {
		return this.balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public int getChartingOrder() {
		return chartingOrder;
	}

	public void setChartingOrder(int chartingOrder) {
		this.chartingOrder = chartingOrder;
	}

	public String getCreditLimitSourceDisplayName() {
		return creditLimitSourceDisplayName;
	}

	public void setCreditLimitSourceDisplayName(
			String creditLimitSourceDisplayName) {
		this.creditLimitSourceDisplayName = creditLimitSourceDisplayName;
	}

	public String getCreditLimitTypeDisplayName() {
		return creditLimitTypeDisplayName;
	}

	public void setCreditLimitTypeDisplayName(String creditLimitTypeDisplayName) {
		this.creditLimitTypeDisplayName = creditLimitTypeDisplayName;
	}

	public String getLimitStatusDisplayName() {
		return limitStatusDisplayName;
	}

	public void setLimitStatusDisplayName(String limitStatusDisplayName) {
		this.limitStatusDisplayName = limitStatusDisplayName;
	}

	public String getCounterpartyGroupNameDisplayName() {
		return counterpartyGroupNameDisplayName;
	}

	public void setCounterpartyGroupNameDisplayName(
			String counterpartyGroupNameDisplayName) {
		this.counterpartyGroupNameDisplayName = counterpartyGroupNameDisplayName;
	}

	public boolean isExposureConsideredBalanceCharting() {
		return isExposureConsideredBalanceCharting;
	}

	public void setExposureConsideredBalanceCharting(
			boolean isExposureConsideredBalanceCharting) {
		this.isExposureConsideredBalanceCharting = isExposureConsideredBalanceCharting;
	}

	public BigDecimal getTccrAmount() {
		return tccrAmount;
	}

	public void setTccrAmount(BigDecimal tccrAmount) {
		this.tccrAmount = tccrAmount;
	}

	public boolean isTccrAmountAllocated() {
		return isTccrAmountAllocated;
	}

	public void setTccrAmountAllocated(boolean isTccrAmountAllocated) {
		this.isTccrAmountAllocated = isTccrAmountAllocated;
	}

	public boolean isConsiderForBalanceCharting() {
		return considerForBalanceCharting;
	}

	public void setConsiderForBalanceCharting(boolean considerForBalanceCharting) {
		this.considerForBalanceCharting = considerForBalanceCharting;
	}

	@Override
	public String toString() {
		return "LimitRefNo : "+this.limitRefNo + " ; LimitSource : "+this.getCreditLimitSourceDisplayName() + " ; LimitType : "+
				 this.getCreditLimitTypeDisplayName() + "; considerForBalanceCharting : "+this.isConsiderForBalanceCharting()
				 +" ; exposureAmount : "+this.exposureAmount +" ; tccrAmount : "+this.tccrAmount+" ; balanceAmount : "+this.balance
				 +" ; Limit Amount : "+this.amount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public boolean isBalanceCalculated() {
		return isBalanceCalculated;
	}

	public void setBalanceCalculated(boolean isBalanceCalculated) {
		this.isBalanceCalculated = isBalanceCalculated;
	}
}
