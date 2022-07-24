package com.eka.connect.creditrisk.constants;

import java.math.RoundingMode;

/**
 * 
 * @author rajeshks
 *
 */
public interface CreditRiskConstants {

	public static final String EXPOSURE_TYPE_L_CS_NOT_UTILIZED = "LCs not utilized";
	public static final String EXPOSURE_TYPE_AMOUNT_PREPAID_BUT_NOT_UTILIZED = "Prepaid but not utilized";
	public static final String EXPOSURE_TYPE_INVOICED_BUT_NOT_PAID = "Invoiced but not paid";
	public static final String EXPOSURE_TYPE_SHIPPED_BUT_NOT_INVOICED = "Shipped but not invoiced";
	public static final String EXPOSURE_TYPE_PLANNED_BUT_NOT_EXECUTED_SHIPMENTS = "Planned but not executed shipments";
	public static final String EXPOSURE_TYPE_ACTIVE_SALES_CONTRACTS_NOT_PLANNED = "Active Sales Contracts not Planned";
	String CREDIT_RISK_APP_REF_TYPE_ID  =  "5539617b-5075-4482-8bcc-26f76849eb89";
	int SCALE = 3;
	RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
	String CONTRACT = "Contract";
	public static final String INVOICE = "Invoice";
	public static final String PP_INVOICE = "PrePayment Invoice";
	public static final String PBS = "PBS";
	public static final String MOVEMENTS = "Movements";
	public static final String INVALID_COUNTERPARTY_PBS= "%1$s not allowed as %2$s is in Inactive/Not available.";
	public static final String MESSAGE_AUTOMATIC_SUSPENSION_PBS = "%1$s not allowed as %2$s  is in Automatic Suspension.";
	public static final String PRE_PAYMENT_LIMIT = "Prepayment Limit";
	public static final String PRE_PAYMNET_CREDIT_LIMIT = "Prepayment Credit Limit";
	String MESSAGE_DELIVERY_STOP_PBS = "%1$s not allowed as %2$s  is in Delivery Stop.";
	String SUCCESS = "Success";
	String FAILURE = "Failure";

	String HARD_BLOCK = "Hard Block";
	String SOFT_BLOCK = "Soft Block";
	
	String INVOICE_FAILURE_MESSAGE = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s, Kindly contact CMD.";
	
	String INVOIC_MESSAGE_LC_PP_OTHERS = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s. Proceeding without approval from Finance can only be done upon specific written instructions from the responsible Trader to do so.";

	String MESSAG3E_PAYMENTTERM_LC = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s, however Contract is created.";

	String MESSAGE_PAYMENTTERM_PP = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s, however Contract is created.";

	String PPI_MESSAGE_PAYMENTTERM_PP = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s, however Contract is created.";

	
	String MESSAGE_PAYMENTTERM_OTHER = "Counterparty is  %1$s and has insufficient credit of %2$s %3$s. Contract  will be Saved As Draft. Kindly contact Trader/CMD";

	String creditCheckSuccess = "Credit check is successful.";
	String COUNTERPARTY_CREDIT_CHECK_SUCCESS = "Credit check is successful for %1$s.";
	String DELIVERY_STOP = "Delivery Stop";
	String MESSAGE_DELIVERY_STOP = "Contract creation not allowed as the Counterparty is in Delivery Stop.";
	String MESSAGE_DELIVERY_STOP_INVOICE = "Invoice creation not allowed as the Counterparty is in Delivery Stop.";
	String MESSAGE_PREPAYMENT_STOP = "Contract creation not allowed as the Countparty is in Prepayment Stop.";
	String MESSAGE_PREPAYMENT_STOP_PPI = "Invoice creation not allowed as the Countparty is in Prepayment Stop.";
	String AUTOMATIC_SUSPENSION = "Automatic Suspension";
	String ACTIVE  = "Active";
	String MESSAGE_AUTOMATIC_SUSPENSION = "Contract creation not allowed as the Counterparty is in Automatic Suspension.";
	String MESSAGE_AUTOMATIC_SUSPENSION_INVOICE = "Invoice creation not allowed as the Counterparty is in Automatic Suspension.";
	String LC = "LC";
	String PP = "PP";
	String OTHERS ="Others";
	String InsufficientFunds = "Insufficient funds.";
	String INVALID_COUNTERPARTY = "Contract creation not allowed as the Counterparty is in Inactive/Not available.";
	String INVALID_COUNTERPARTY_INVOICE= "Invoice creation not allowed as the Counterparty is in Inactive/Not available.";
	String PURCHASE_CONTRACT_SUCCESS_NO_PREPAYMENT = "No credit check done as no pre-payment";
	String INACTIVE = "Inactive";
	String DERIVED_PAYMENT_TERMS_NOT_FOUND_CONTRACT_CREATE= "No derived payment term found for the payment term [%1$s] in the Credit Risk App. Contract will be saved as draft. Kindly contact CMD";
	
	String DERIVED_PAYMENT_TERMS_NOT_FOUND_CONTRACT_MODIFY = "No derived payment term found for the payment term [%1$s] in the Credit Risk App. Changes cannot be saved. Kindly contact CMD";

	String MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND = "There are no Credit limit maintained for this counterparty. Kindly contact CMD";
	String MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK = "%1$s is %2$s and there are no Credit limit maintained for this counterparty.";
	String MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK_COUNTERPARTY_NAME = "There are no Credit limit maintained for %1$s.";
	
	String CONTRACT_FULL_TERM = "Contract (Full Term)";
	String CONTRACT_PARTIAL_TERM = "Contract (Partial Term)";
	String PREPAYMENT_CONTRACT  = "";
	String TEMPORARY = "Temporary";
	String CREDIT_LIMIT = "Credit Limit";
	String OWN_LIMIT = "Own Limit";
	String TOP_UP_CREDIT_LIMIT = "Top Up Credit Limit";
	String TOP_UP_CONTRACT_FULL_TERM = "Top Up Contract (Full Term)";
	String PRE_PAYMENT_FULL_TERM = "Prepayment Contract (Full Term)";
	String PRE_PAYMENT_PARTIAL_TERM = "Prepayment Contract (Partial Term)";
	String INSUFFICIENT_AMOUNT_MESSAGE = "Counterparty has insufficient credit of %1$s %2$s. Contract  will be Saved As Draft. Kindly contact Trader/CMD";
	String PPI_INSUFFICIENT_AMOUNT_MESSAGE = "Counterparty is %1$s and  has insufficient credit of %2$s %3$s. Kindly contact CMD.";
	String LIMIT_REF_NO_NOT_MATCH = "The Limit Ref. No. %1$s is inactive or invalid. Kindly contact Trader/CMD and retry";
	String PRE_PAYMENT_STOP ="Prepayment Stop";
	String FX_RATE_CONVERSION_MISSING = "Fx Rate Conversion is missing for %1$s and %2$s";
	String OPERATION_TYPE_AMEND = "amend";
	String OPERATION_TYPE_MODIFY = "modify";
	String  OPERATION_TYPE_CREATE ="create";
	
	String EXPOSURE_TYPE_PREPAID_ACTIVE_PURCHASE_CONTRACTS = "Prepaid Active Purchase Contracts";
	String EXPOSURE_TYPE_PREPAYMET_INVOICED = "Prepayment Invoiced";
	String EXPOSURE_TYPE_PREPAID_AMOUNT_IN_SHIPPED_GOOD = "Prepaid Amount used in the Shipped Good";

	String EXPOSURE_TYPE_PREPAYMENT_INVOICED = "Prepayment Invoiced";
	
	String[] CONTRACT_EXPOSURE_TYPES_CREATE ={EXPOSURE_TYPE_ACTIVE_SALES_CONTRACTS_NOT_PLANNED,EXPOSURE_TYPE_PLANNED_BUT_NOT_EXECUTED_SHIPMENTS,
			EXPOSURE_TYPE_SHIPPED_BUT_NOT_INVOICED,EXPOSURE_TYPE_INVOICED_BUT_NOT_PAID,EXPOSURE_TYPE_AMOUNT_PREPAID_BUT_NOT_UTILIZED,EXPOSURE_TYPE_L_CS_NOT_UTILIZED};
	
	String[] PURCHASE_CONTRACT_EXPOSURE_TYPES_CREATE ={EXPOSURE_TYPE_PREPAID_ACTIVE_PURCHASE_CONTRACTS,
			EXPOSURE_TYPE_PREPAYMET_INVOICED,EXPOSURE_TYPE_PREPAID_AMOUNT_IN_SHIPPED_GOOD};
	
	
	String[] PREPAYMENT_INVOICE_EXPOSURE_TYPES = {
			EXPOSURE_TYPE_PREPAYMENT_INVOICED,
			EXPOSURE_TYPE_PREPAID_AMOUNT_IN_SHIPPED_GOOD };	
	
	
	String[] PREPAYMENT_INVOICE_EXPOSURE_TYPES_CREATE ={EXPOSURE_TYPE_PREPAID_ACTIVE_PURCHASE_CONTRACTS,
			EXPOSURE_TYPE_PREPAYMET_INVOICED,EXPOSURE_TYPE_PREPAID_AMOUNT_IN_SHIPPED_GOOD};
	
	
	String[] CONTRACT_EXPOSURE_TYPES_MODIFY_AMEND  = {EXPOSURE_TYPE_PLANNED_BUT_NOT_EXECUTED_SHIPMENTS,EXPOSURE_TYPE_SHIPPED_BUT_NOT_INVOICED,
			EXPOSURE_TYPE_INVOICED_BUT_NOT_PAID,EXPOSURE_TYPE_AMOUNT_PREPAID_BUT_NOT_UTILIZED,EXPOSURE_TYPE_L_CS_NOT_UTILIZED};
	
	String[] EXPOSURE_TYPES_SALES_FINAL_INVOICE = CONTRACT_EXPOSURE_TYPES_MODIFY_AMEND;
	
	String[] EXPOSURE_TYPES_MO = CONTRACT_EXPOSURE_TYPES_MODIFY_AMEND;
	
	String[] CONTRACT_EXPOSURE_TYPES_PBS  = EXPOSURE_TYPES_MO;
	
	String  COUNTERPARTY_PBS_MO_MESSAGE= "Counterparty %1$s is %2$s and has insufficient credit of %3$s %4$s.";
	
	String  COUNTERPARTYGROUP_PBS_MO_MESSAGE= "Counterparty Group %1$s has insufficient credit of %2$s %3$s.";
	
	String  COUNTERPARTY_PBS_MO_MESSAGE_HARD_BLOCK= "Counterparty %1$s is %2$s and has insufficient credit of %3$s %4$s.Cannot Proceed,kindly contact CMD";
	
	String  COUNTERPARTYGROUP_PBS_MO_MESSAGE_HARD_BLOCK= "Counterparty Group %1$s has insufficient credit of %2$s %3$s.Cannot Proceed,kindly contact CMD";
	
	String COUNTERPARTY_CURRENCY_NOT_FOUND = "There is no Counterparty Currency maintained. Credit Risk check cannot be performed.";
	 
	String X_TENANT_ID = "X-TenantID";
	String REQUEST_ID = "requestId";
	
	String AUTHORIZATION = "Authorization";
	
	String TENANT_NAME = "tenantName";
 

}
