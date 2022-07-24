package com.eka.connect.creditrisk.dataobject;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PrepaymetContractDetails {
	
	@NotBlank(message = "contractRefNo cannot be empty")
	@ApiModelProperty(name = "Contract Ref No")
	private String contractRefNo;

	@NotBlank(message = "contractItemRefNo cannot be empty")
	@ApiModelProperty(name = "Contract Item Ref No")
	private String contractItemRefNo;
	
	
	@NotBlank(message = "payInCurrency cannot be empty")
	@ApiModelProperty(name = "Contract Item Pay In Currency")
	private String payInCurrency;
	 
	
	@NotNull(message = "value cannot be empty")
	@ApiModelProperty(name = "Contract Value")
	private BigDecimal value;
	
	
	@NotNull(message = "fromPeriod cannot be empty")
	@ApiModelProperty(name = "From Period", dataType = "Date", example = "2017-07-21T17:32:28Z")
	// @JsonSerialize(as = Date.class)
	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern =
	// "yyyy-MM-ddThh:mm:ssZ")
	private Date fromPeriod;
	
	// @JsonSerialize(as = Date.class)
	@NotNull(message = "toPeriod cannot be empty")
	@ApiModelProperty(name = "To Period", dataType = "Date", example = "2017-09-21T17:32:28Z")
	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date toPeriod;

	
	@ApiModelProperty(name = "Limit Maintenance Reference number.")
	private String  limitRefNo;

	public String getContractRefNo() {
		return contractRefNo;
	}

	public void setContractRefNo(String contractRefNo) {
		this.contractRefNo = contractRefNo;
	}

	public String getContractItemRefNo() {
		return contractItemRefNo;
	}

	public void setContractItemRefNo(String contractItemRefNo) {
		this.contractItemRefNo = contractItemRefNo;
	}

	public String getPayInCurrency() {
		return payInCurrency;
	}

	public void setPayInCurrency(String payInCurrency) {
		this.payInCurrency = payInCurrency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
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

	public String getLimitRefNo() {
		return limitRefNo;
	}

	public void setLimitRefNo(String limitRefNo) {
		this.limitRefNo = limitRefNo;
	}

	 }
