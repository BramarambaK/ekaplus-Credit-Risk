package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Pojo class for fx rates.
 * @author rajeshks
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FxRates {

	private String baseCurrency;
	private String foreignCurrency;
	private BigDecimal fxRate;
	
	public String getBaseCurrency() {
		return baseCurrency;
	}
	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}
	public String getForeignCurrency() {
		return foreignCurrency;
	}
	public void setForeignCurrency(String foreignCurrency) {
		this.foreignCurrency = foreignCurrency;
	}
	public BigDecimal getFxRate() {
		return fxRate;
	}
	public void setFxRate(BigDecimal fxRate) {
		this.fxRate = fxRate;
	}
	
	@Override
	public String toString() {
		return this.baseCurrency + " --> " + this.foreignCurrency + " = "
				+ this.fxRate.toPlainString();
	}
}
