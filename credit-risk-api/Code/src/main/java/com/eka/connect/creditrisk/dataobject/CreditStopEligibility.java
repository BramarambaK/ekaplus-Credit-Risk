package com.eka.connect.creditrisk.dataobject;

import java.math.BigDecimal;
import java.util.Date;

import com.eka.connect.creditrisk.util.YNBooleanDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditStopEligibility {

	private boolean eligible;
	private boolean declared;
	private boolean overdue;
	private String corporateid;
	
	@JsonAlias("counterpartyGroup")
	private String counterparty;
	private String trader;
	private String invoiceRefNo;
	private String paymentTerm;
	private Date dueDate;
	private BigDecimal totalAmount;
	private BigDecimal pendingAmount;
	private String cpid;

	private String invoiceForm;

	private int deliveryStopDays = 60;

	private int automaticSuspensionDays = 90;
	
	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date deliveryStopDueDate;

	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date automaticSuspensionDueDate;

	@JsonAlias("eligibleforDeliveryStop")
	//@JsonDeserialize(using=YNBooleanDeserializer.class)
	private boolean eligibleForDeliveryStop;

	@JsonAlias("eligibleforAutomaticSuspension")
	//@JsonDeserialize(using=YNBooleanDeserializer.class)
	private boolean eligibleForAutomaticSuspension;

	@JsonSerialize(as = Date.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Date prepaymentStopDueDate;

	@JsonAlias("eligibleforPrePaymentStop")
	//@JsonDeserialize(using=YNBooleanDeserializer.class)
	private boolean eligibleForPrepaymentStop;

	public boolean isEligible() {
		return eligible;
	}

	public void setEligible(boolean eligible) {
		this.eligible = eligible;
	}

	public boolean isDeclared() {
		return declared;
	}

	public void setDeclared(boolean declared) {
		this.declared = declared;
	}

	public boolean isOverdue() {
		return overdue;
	}

	public void setOverdue(boolean overdue) {
		this.overdue = overdue;
	}

	public String getCorporateid() {
		return corporateid;
	}

	public void setCorporateid(String corporateid) {
		this.corporateid = corporateid;
	}

	public String getCounterparty() {
		return counterparty;
	}

	public void setCounterparty(String counterparty) {
		this.counterparty = counterparty;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}

	public String getInvoiceRefNo() {
		return invoiceRefNo;
	}

	public void setInvoiceRefNo(String invoiceRefNo) {
		this.invoiceRefNo = invoiceRefNo;
	}

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getPendingAmount() {
		return pendingAmount;
	}

	public void setPendingAmount(BigDecimal pendingAmount) {
		this.pendingAmount = pendingAmount;
	}

	public String getInvoiceForm() {
		return invoiceForm;
	}

	public void setInvoiceForm(String invoiceForm) {
		this.invoiceForm = invoiceForm;
	}

	public int getDeliveryStopDays() {
		return deliveryStopDays;
	}

	public void setDeliveryStopDays(int deliveryStopDays) {
		this.deliveryStopDays = deliveryStopDays;
	}

	public int getAutomaticSuspensionDays() {
		return automaticSuspensionDays;
	}

	public void setAutomaticSuspensionDays(int automaticSuspensionDays) {
		this.automaticSuspensionDays = automaticSuspensionDays;
	}

	public Date getDeliveryStopDueDate() {
		return deliveryStopDueDate;
	}

	public void setDeliveryStopDueDate(Date deliveryStopDueDate) {
		this.deliveryStopDueDate = deliveryStopDueDate;
	}

	public Date getAutomaticSuspensionDueDate() {
		return automaticSuspensionDueDate;
	}

	public void setAutomaticSuspensionDueDate(Date automaticSuspensionDueDate) {
		this.automaticSuspensionDueDate = automaticSuspensionDueDate;
	}

	public boolean isEligibleForDeliveryStop() {
		return eligibleForDeliveryStop;
	}

	@JsonSetter("eligibleforDeliveryStop")
	public void setEligibleForDeliveryStop(String eligibleForDeliveryStop) {
		this.eligibleForDeliveryStop = "Y"
				.equalsIgnoreCase(eligibleForDeliveryStop) ? true : false;
	}

	public boolean isEligibleForAutomaticSuspension() {
		return eligibleForAutomaticSuspension;
	}

	@JsonSetter("eligibleforAutomaticSuspension")
	public void setEligibleForAutomaticSuspension(
			String eligibleForAutomaticSuspension) {
		this.eligibleForAutomaticSuspension = "Y"
				.equalsIgnoreCase(eligibleForAutomaticSuspension) ? true
				: false;
	}

	public Date getPrepaymentStopDueDate() {
		return prepaymentStopDueDate;
	}

	public void setPrepaymentStopDueDate(Date prepaymentStopDueDate) {
		this.prepaymentStopDueDate = prepaymentStopDueDate;
	}

	public boolean isEligibleForPrepaymentStop() {
		return eligibleForPrepaymentStop;
	}

	@JsonSetter("eligibleForPrepaymentStop")
	public void setEligibleForPrepaymentStop(String eligibleForPrepaymentStop) {

		this.eligibleForPrepaymentStop = "Y"
				.equalsIgnoreCase(eligibleForPrepaymentStop) ? true : false;
	}

	public String getCpid() {
		return cpid;
	}

	public void setCpid(String cpid) {
		this.cpid = cpid;
	}

	public void setEligibleForDeliveryStop(boolean eligibleForDeliveryStop) {
		this.eligibleForDeliveryStop = eligibleForDeliveryStop;
	}

	public void setEligibleForAutomaticSuspension(
			boolean eligibleForAutomaticSuspension) {
		this.eligibleForAutomaticSuspension = eligibleForAutomaticSuspension;
	}

	public void setEligibleForPrepaymentStop(boolean eligibleForPrepaymentStop) {
		this.eligibleForPrepaymentStop = eligibleForPrepaymentStop;
	}

}
