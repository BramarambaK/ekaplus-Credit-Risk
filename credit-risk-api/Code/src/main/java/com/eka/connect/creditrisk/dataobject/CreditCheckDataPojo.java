package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;

public class CreditCheckDataPojo {

	
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

	public BigDecimal getExposureAmount() {
		return exposureAmount;
	}

	public void setExposureAmount(BigDecimal exposureAmount) {
		this.exposureAmount = exposureAmount;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	
}
