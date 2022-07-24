
package com.eka.connect.creditrisk.dataobject;

public class FxRatesKey {

	
	private String baseCurrency;
	private String foreignCurrency;
	
	public FxRatesKey(String baseCurrency,String foreignCurrency) {

		this.baseCurrency  = baseCurrency;
		this.foreignCurrency =  foreignCurrency;
	}
	
	 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseCurrency == null) ? 0 : baseCurrency.toLowerCase().hashCode());
		result = prime * result
				+ ((foreignCurrency == null) ? 0 : foreignCurrency.toLowerCase().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FxRatesKey other = (FxRatesKey) obj;
		if (baseCurrency == null) {
			if (other.baseCurrency != null)
				return false;
		} else if (!baseCurrency.equalsIgnoreCase(other.baseCurrency))
			return false;
		if (foreignCurrency == null) {
			if (other.foreignCurrency != null)
				return false;
		} else if (!foreignCurrency.equalsIgnoreCase(other.foreignCurrency))
			return false;
		return true;
	}
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
	
	public FxRatesKey reverseKeys() {
		String temp = this.baseCurrency;
		this.baseCurrency = this.foreignCurrency;
		this.foreignCurrency = temp;
		return this;
	}
	
	@Override
	public String toString() {

		return this.baseCurrency + " --> " + this.foreignCurrency + " = ";
	
	}
}
