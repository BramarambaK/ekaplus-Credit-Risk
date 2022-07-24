package com.eka.connect.creditrisk.dataobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.validator.NotNullDependsOn;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApiModel(description = "Credit Risk check api model for Contract..")

@NotNullDependsOn(fieldToValidate = {"contractRefNo" }, fieldMessages = {
		"{contractRefNo.not.empty}" }, dependsOn = "operationType", dependsOnPropertyValueAcceptableList = {
		CreditRiskConstants.OPERATION_TYPE_AMEND,
		CreditRiskConstants.OPERATION_TYPE_MODIFY }, message = "This field is mandatory")

public class ContractCreditCheckRequest {

	private String entityRefNo;
	
	@ApiModelProperty(required = true, dataType = "String", value = "Counter party name")
	@NotNull(message = "counterParty is mandatory")
	private String counterParty;
	
	@ApiModelProperty(required = false, dataType = "String", value = "Group to which Counter pary belongs to")
	private String counterPartyGroup;
	
	private String contractRefNo;
	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Operation Type")
	@Pattern(regexp="create|modify|amend" ,flags={Flag.CASE_INSENSITIVE},message="operationType values should be either 'create' or 'modify' or 'amend'")
	@NotNull(message = "Operation Type is mandatory")
	private String operationType;
	
	@ApiModelProperty(required = true, dataType = "String", value = "Requested Event Name")
	//@NotBlank(message = "eventName is mandatory")
	private String eventName;
	
	private String limitRefNo;
	
	@ApiModelProperty(required = true, dataType = "String", value = "External Reference Number of Counter party")
	private String counterpartyRefNo;
	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Type of Contract(Purchase/Sales)")
	@Pattern(regexp="Purchase|Sales" ,flags={Flag.CASE_INSENSITIVE},message="contractType values should be either 'Purchase' or 'Sales'")
	@NotNull(message = "Contract type is mandatory")
	private String contractType;
	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Payment Term")
	@NotNull(message = "Payment Term is mandatory")
	private String paymentTerm;
	
	@ApiModelProperty(required = false, dataType = "String", value = "Purchase contract Pre Payment Percentage")
	private BigDecimal prepaymentPercentage;
	@ApiModelProperty(required = true, dataType = "List", value = "Contract Item Details")
	@Size(min = 1)
	@NotNull(message = "itemDetails cannot be empty")
	private @Valid List<ContractItemDetails> itemDetails;

	public String getEntityRefNo() {
		return entityRefNo;
	}

	public void setEntityRefNo(String entityRefNo) {
		this.entityRefNo = entityRefNo;
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

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

	public List<ContractItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<ContractItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public String getCounterpartyRefNo() {
		return counterpartyRefNo;
	}

	public void setCounterpartyRefNo(String counterpartyRefNo) {
		this.counterpartyRefNo = counterpartyRefNo;
	}

	@Override
	public String toString() {

		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {

		}
		return this.toString();

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

	public BigDecimal getPrepaymentPercentage() {
		return prepaymentPercentage;
	}

	public void setPrepaymentPercentage(BigDecimal prepaymentPercentage) {
		this.prepaymentPercentage = prepaymentPercentage;
	}

}
