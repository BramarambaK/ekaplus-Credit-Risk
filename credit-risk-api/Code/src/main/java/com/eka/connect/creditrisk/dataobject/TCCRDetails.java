package com.eka.connect.creditrisk.dataobject;

import java.util.List;

public class TCCRDetails {

	private String counterParty;
	private String counterPartyGroup;
	private String contractRefNo;
	private String  counterPartyRefNo;//business partner ref no.
	private String limitRefNo;
	private String paymentTerm;
	private String operationType;
	private String gmrRefNo;
	private List<Item> items;
	
	private CounterPartyDetails counterpartyDetails;

	public CounterPartyDetails getCounterpartyDetails() {
		return counterpartyDetails;
	}

	public void setCounterpartyDetails(CounterPartyDetails counterpartyDetails) {
		this.counterpartyDetails = counterpartyDetails;
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

	public String getContractRefNo() {
		return contractRefNo;
	}

	public void setContractRefNo(String contractRefNo) {
		this.contractRefNo = contractRefNo;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public String getCounterPartyRefNo() {
		return counterPartyRefNo;
	}

	public void setCounterPartyRefNo(String counterPartyRefNo) {
		this.counterPartyRefNo = counterPartyRefNo;
	}

	 

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getGmrRefNo() {
		return gmrRefNo;
	}

	public void setGmrRefNo(String gmrRefNo) {
		this.gmrRefNo = gmrRefNo;
	}

}
