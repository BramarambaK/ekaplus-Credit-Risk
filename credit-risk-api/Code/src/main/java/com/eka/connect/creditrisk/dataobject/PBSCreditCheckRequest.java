package com.eka.connect.creditrisk.dataobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApiModel(description = "Credit Risk check api model for Plan Bulk Shipment..")
public class PBSCreditCheckRequest {

	@ApiModelProperty(required = true, dataType = "String", value = "Entity Reference Number")
	@NotBlank(message = "entityRefNo is mandatory")
	private String entityRefNo;
	
	@ApiModelProperty(required = true, dataType = "String", value = "Requested Event Name")
	//@NotBlank(message = "eventName is mandatory")
	private String eventName;
	
	@ApiModelProperty(required = true, dataType = "List", value = "Contract Item Details")
	@Size(min = 1)
	@NotNull(message = "contractDetails cannot be empty")
	private @Valid List<MovementContractDetails> contractDetails;

	public String getEntityRefNo() {
		return entityRefNo;
	}

	public void setEntityRefNo(String entityRefNo) {
		this.entityRefNo = entityRefNo;
	}


	public List<MovementContractDetails> getContractDetails() {
		return contractDetails;
	}

	public void setContractDetails(List<MovementContractDetails> contractDetails) {
		this.contractDetails = contractDetails;
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
