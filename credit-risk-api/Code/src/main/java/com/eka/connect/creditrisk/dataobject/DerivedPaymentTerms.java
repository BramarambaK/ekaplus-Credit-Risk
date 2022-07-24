package com.eka.connect.creditrisk.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DerivedPaymentTerms {

	private String derivedPaymentTerm;
	private String paymentTerm;

	public String getDerivedPaymentTerm() {
		return derivedPaymentTerm;
	}

	public void setDerivedPaymentTerm(String derivedPaymentTerm) {
		this.derivedPaymentTerm = derivedPaymentTerm;
	}

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

}
