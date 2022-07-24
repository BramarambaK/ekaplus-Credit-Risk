package com.eka.connect.creditrisk.dataobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApiModel(description = "Credit Risk check api model for Sales Final Invoice creation..")

public class InvoiceCreditCheckRequest {

	private String entityRefNo;
	
	@ApiModelProperty(required = true, dataType = "String", value = "Counter party name")
	@NotBlank(message = "counterParty is mandatory")
	private String counterParty;
	
	@ApiModelProperty(required = true, dataType = "String", value = "Invoice Counter party name")
	@NotBlank(message = "invoiceCounterParty is mandatory")
	private String invoiceCounterParty;
	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Type of Contract(Purchase/Sales)")
	@Pattern(regexp="Sales" ,flags={Flag.CASE_INSENSITIVE},message="contractType values should be 'Sales'")
	@NotBlank(message = "Contract type is mandatory")
	private String contractType;	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Requested Event Name")
	//@NotBlank(message = "eventName is mandatory")
	private String eventName;
	
	@ApiModelProperty(required = false, dataType = "String", value = "Group to which Counter pary belongs to")
	private String counterPartyGroup;
	
	
	
	
	@ApiModelProperty(required = true, dataType = "String", value = "Payment Term")
	@NotBlank(message = "Payment Term is mandatory")
	private String paymentTerm;
	

	@ApiModelProperty(required = true, dataType = "List", value = "Contract Item Details")
	@Size(min = 1)
	@NotNull(message = "contractDetails cannot be empty")
	private @Valid List<ContractDetails> contractDetails;


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


	public String getInvoiceCounterParty() {
		return invoiceCounterParty;
	}


	public void setInvoiceCounterParty(String invoiceCounterParty) {
		this.invoiceCounterParty = invoiceCounterParty;
	}


	public String getContractType() {
		return contractType;
	}


	public void setContractType(String contractType) {
		this.contractType = contractType;
	}


	public String getCounterPartyGroup() {
		return counterPartyGroup;
	}


	public void setCounterPartyGroup(String counterPartyGroup) {
		this.counterPartyGroup = counterPartyGroup;
	}


	public List<ContractDetails> getContractDetails() {
		return contractDetails;
	}


	public void setContractDetails(List<ContractDetails> contractDetails) {
		this.contractDetails = contractDetails;
	}


	public String getPaymentTerm() {
		return paymentTerm;
	}


	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}
	
	@Override
	public String toString() {

		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {

		}
		return this.toString();

	}


	public String getEventName() {
		return eventName;
	}


	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	}
