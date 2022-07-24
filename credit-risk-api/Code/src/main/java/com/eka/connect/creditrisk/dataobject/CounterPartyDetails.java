package com.eka.connect.creditrisk.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_NULL)
public class CounterPartyDetails {

	private String cpid;
	private String counterpartyGroup;
	private String counterpartyName;
	private String creditRiskStatus;
	private String creditRiskStatusDisplayName;
	private String statusDate;
	private String reference;
	private String debtorNumber;//mandatory for update.
	private String annexNumber;//mandatory for update
	//DateDecisionFormat  = 2019-05-28
	private String dateDecision;//mandatory for update
	private String decisionRemarks;//mandatory for update
	@JsonIgnore
	private String counterpartyNameDisplayName;
	@JsonIgnore
	private String counterpartyGroupDisplayName;
	
	private String currency;
	
	
	
	@JsonIgnore
	private String creditStopStatus;
	
	private String creditLimitLevel;
	
	
	private String sys__UUID;
	@JsonProperty("_id")
	private String connectId;
	
	private String id;
	
	private String creditCollectionStatus;
	private String creditCollectionStatusDisplayName;
	
	
	public String getCpid() {
		return cpid;
	}

	public void setCpid(String cpid) {
		this.cpid = cpid;
	}

	public String getCreditRiskStatus() {
		return creditRiskStatus;
	}

	public void setCreditRiskStatus(String creditRiskStatus) {
		this.creditRiskStatus = creditRiskStatus;
	}

	public String getCreditRiskStatusDisplayName() {
		return creditRiskStatusDisplayName;
	}

	public void setCreditRiskStatusDisplayName(String creditRiskStatusDisplayName) {
		this.creditRiskStatusDisplayName = creditRiskStatusDisplayName;
	}

	public String getCounterpartyName() {
		return counterpartyName;
	}

	public void setCounterpartyName(String counterpartyName) {
		this.counterpartyName = counterpartyName;
	}

	public String getSys__UUID() {
		return sys__UUID;
	}

	public void setSys__UUID(String sys__UUID) {
		this.sys__UUID = sys__UUID;
	}

	public String getDebtorNumber() {
		return debtorNumber;
	}

	public void setDebtorNumber(String debtorNumber) {
		this.debtorNumber = debtorNumber;
	}

	public String getAnnexNumber() {
		return annexNumber;
	}

	public void setAnnexNumber(String annexNumber) {
		this.annexNumber = annexNumber;
	}

	public String getDateDecision() {
		return dateDecision;
	}

	public void setDateDecision(String dateDecision) {
		this.dateDecision = dateDecision;
	}

	public String getDecisionRemarks() {
		return decisionRemarks;
	}

	public void setDecisionRemarks(String decisionRemarks) {
		this.decisionRemarks = decisionRemarks;
	}

	@JsonIgnore
	public String getConnectId() {
		return connectId;
	}

	public void setConnectId(String connectId) {
		this.connectId = connectId;
	}

	public String getCounterpartyGroup() {
		return counterpartyGroup;
	}

	public void setCounterpartyGroup(String counterpartyGroup) {
		this.counterpartyGroup = counterpartyGroup;
	}

	public String getCreditStopStatus() {
		return creditStopStatus;
	}

	public void setCreditStopStatus(String creditStopStatus) {
		this.creditStopStatus = creditStopStatus;
	}

	public String getCounterpartyGroupDisplayName() {
		return counterpartyGroupDisplayName;
	}

	public void setCounterpartyGroupDisplayName(String counterpartyGroupDisplayName) {
		this.counterpartyGroupDisplayName = counterpartyGroupDisplayName;
	}

	public String getCounterpartyNameDisplayName() {
		return counterpartyNameDisplayName;
	}

	public void setCounterpartyNameDisplayName(String counterpartyNameDisplayName) {
		this.counterpartyNameDisplayName = counterpartyNameDisplayName;
	}

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCreditLimitLevel() {
		return creditLimitLevel;
	}

	public void setCreditLimitLevel(String creditLimitLevel) {
		this.creditLimitLevel = creditLimitLevel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreditCollectionStatus() {
		return creditCollectionStatus;
	}

	public void setCreditCollectionStatus(String creditCollectionStatus) {
		this.creditCollectionStatus = creditCollectionStatus;
	}

	public String getCreditCollectionStatusDisplayName() {
		return creditCollectionStatusDisplayName;
	}

	public void setCreditCollectionStatusDisplayName(
			String creditCollectionStatusDisplayName) {
		this.creditCollectionStatusDisplayName = creditCollectionStatusDisplayName;
	}

}
