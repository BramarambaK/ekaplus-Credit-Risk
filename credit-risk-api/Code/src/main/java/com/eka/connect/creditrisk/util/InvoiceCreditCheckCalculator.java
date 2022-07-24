package com.eka.connect.creditrisk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.eka.connect.creditrisk.constants.CreditLimitTypeEnum;
import com.eka.connect.creditrisk.constants.CreditLimitTypeGroupEnum;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;

public class InvoiceCreditCheckCalculator extends CreditCheckCalculator {

	private String paymentTerm =null;
	private CounterPartyDetails counterpartyDetails  = null;
	private String contractType;
	
	public InvoiceCreditCheckCalculator(CounterPartyDetails c,
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<TCCRDetails> tccrDetails) {

		super(limitMaintenanceDetails,tccrDetails);
		this.counterpartyDetails = c;
	}

	@Override
	public Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimitMaintenanceByLimitType() {

		final String counterParty = this.getTccrDetails().get(0).getCounterParty();
		final String counterPartyGroup = this.getTccrDetails().get(0).getCounterPartyGroup();
		Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> map = new HashMap<>();
		map.put(CreditLimitTypeGroupEnum.PRE_BOOKED,
				new ArrayList<LimitMaintenanceDetails>());
		map.put(CreditLimitTypeGroupEnum.TEMPORARY,
				new ArrayList<LimitMaintenanceDetails>());
		map.put(CreditLimitTypeGroupEnum.POOL,
				new ArrayList<LimitMaintenanceDetails>());

		if (this.getLimitMaintenanceDetails() != null) {
			for (LimitMaintenanceDetails limitMaintenanceDetails2 : this.getLimitMaintenanceDetails()) {
				if (limitMaintenanceDetails2
						.getCounterpartyGroupNameDisplayName()
						.equalsIgnoreCase(counterParty)) {
					limitMaintenanceDetails2.setChartingOrder(-1);
				} else if (limitMaintenanceDetails2
						.getCounterpartyGroupNameDisplayName()
						.equalsIgnoreCase(counterPartyGroup)) {
					limitMaintenanceDetails2.setChartingOrder(0);
				}
	 
				/*
				 * if(limitMaintenanceDetails2.getReference()==null)
				 * limitMaintenanceDetails2.setBalance(limitMaintenanceDetails2
				 * .getLmAmount());
				 */
				CreditLimitTypeGroupEnum e = CreditLimitTypeGroupEnum
						.getCreditLimitTypeGroupEnumByLimitType(CreditLimitTypeEnum
								.getEnumByName(limitMaintenanceDetails2
										.getCreditLimitTypeDisplayName()));
				if (map.containsKey(e)) {

					map.get(e).add(limitMaintenanceDetails2);
				} else {
					List<LimitMaintenanceDetails> l = new ArrayList<>();
					map.put(e, l);
					l.add(limitMaintenanceDetails2);
				}
			}

			final LimitMaintenanceComparator c = new LimitMaintenanceComparator();
			map.forEach((k, v) -> {
				v.sort(c);
			});
		}

		return map;
	
	
	}
	
	
	public void deriveFinalResponse() {

		if (this.finalAvailableBalance.compareTo(BIG_DECIMAL_ZERO) >= 0) {
			itemResponse.setBlockType(null);
			itemResponse.setDescription(CreditRiskConstants.creditCheckSuccess);
			itemResponse.setStatus(CreditRiskConstants.SUCCESS);
		} else {
			Map<String, Object> limitRefNos = new HashMap<>();
			int itemCount = 0;
			int count= 0;
			for (TCCRDetails tccr : this.getTccrDetails()) {
				for (Item item : tccr.getItems()) {
					itemCount++;
					limitRefNos.put(item.getLimitRefNo(), null);
					if(item.getLimitRefNo()!=null)
						count++;
				}
			}
			if(itemCount==count){
				itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
				itemResponse.setDescription(CreditRiskConstants.INVOICE_FAILURE_MESSAGE);
				itemResponse.setStatus(CreditRiskConstants.FAILURE);
			}
			else{


				itemResponse.setStatus(CreditRiskConstants.FAILURE);
				itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);
				if (derivedPaymentTerms.get(this.getPaymentTerm())
						.equalsIgnoreCase(CreditRiskConstants.LC)) {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.INVOIC_MESSAGE_LC_PP_OTHERS, this
									.counterpartyDetails
									.getCreditRiskStatusDisplayName(),
									CreditCheckCalculator.getLocaleFormatedStringAmount(
											finalAvailableBalance.abs(),
											this.getCurrenyLocale()), this
									.counterpartyDetails.getCurrency()));
				} else if (derivedPaymentTerms.get(this.getPaymentTerm())
						.equalsIgnoreCase(CreditRiskConstants.PP)) {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.INVOIC_MESSAGE_LC_PP_OTHERS, this
									.counterpartyDetails
									.getCreditRiskStatusDisplayName(),
									CreditCheckCalculator.getLocaleFormatedStringAmount(
											finalAvailableBalance.abs(),
											this.getCurrenyLocale()), this
									.counterpartyDetails.getCurrency()));
				} else {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.INVOIC_MESSAGE_LC_PP_OTHERS, this
									.counterpartyDetails
									.getCreditRiskStatusDisplayName(),
									CreditCheckCalculator.getLocaleFormatedStringAmount(
											finalAvailableBalance.abs(),
											this.getCurrenyLocale()), this
									.counterpartyDetails.getCurrency()));
					itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);

				}

			
			}

		}

	}


	@Override
	public boolean precheck() {


		// Step 1
		TCCRDetails tccrDetails = this.getTccrDetails().get(0);
		itemResponse.setCounterParty(tccrDetails.getCounterParty());
		itemResponse.setCounterPartyGroup(tccrDetails.getCounterPartyGroup());
		if (null == this.counterpartyDetails) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse
					.setDescription(CreditRiskConstants.INVALID_COUNTERPARTY);
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;

		}
		else{
			if(!validateCounterpartyDetails(tccrDetails.getCounterpartyDetails(),itemResponse)){
				return false;
			}
		}
		String counterPartyCreditRiskStatus = this.counterpartyDetails
				.getCreditRiskStatusDisplayName();

		if (!isValidStatus(counterPartyCreditRiskStatus, itemResponse,contractType)) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;
		}

		if (null == derivedPaymentTerms
				|| derivedPaymentTerms.isEmpty()
				|| (!derivedPaymentTerms.containsKey(tccrDetails
						.getPaymentTerm()))) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse
					.setDescription(String
							.format(CreditRiskConstants.DERIVED_PAYMENT_TERMS_NOT_FOUND_CONTRACT_MODIFY,
									tccrDetails.getPaymentTerm()));
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;

		}
		if (null == this.getLimitMaintenanceDetails()
				|| this.getLimitMaintenanceDetails().isEmpty()) {
			itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);
			itemResponse
					.setDescription(String.format(CreditRiskConstants.MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK,
							itemResponse.getCounterParty(),counterPartyCreditRiskStatus));
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;
		} else {
			
			//TODO: to fix performance issue here. 
			for (TCCRDetails tccr : this.getTccrDetails()) {

				for (Item item : tccr.getItems()) {
				if (item.getLimitRefNo() != null) {
					Optional<LimitMaintenanceDetails> findFirst = this
							.getLimitMaintenanceDetails()
							.stream()
							.filter(e -> item.getLimitRefNo().equalsIgnoreCase(
									e.getLimitRefNo())).findFirst();
					if (!findFirst.isPresent()) {
						itemResponse
								.setBlockType(CreditRiskConstants.HARD_BLOCK);
						itemResponse
								.setDescription(String.format(CreditRiskConstants.LIMIT_REF_NO_NOT_MATCH,
										item.getLimitRefNo()));
						itemResponse.setStatus(CreditRiskConstants.FAILURE);
						return false;
					}
				}
				 

					if (!item.getPayInCurrency().equalsIgnoreCase(
							this.counterpartyDetails.getCurrency())) {
						if (this.getFxRatesMap() == null
								|| !this.getFxRatesMap().containsKey(
										new FxRatesKey(this
												.counterpartyDetails
												.getCurrency(), item
												.getPayInCurrency()))) {
							this.getItemResponse().setBlockType(
									CreditRiskConstants.HARD_BLOCK);
							this.getItemResponse()
									.setDescription(
											String.format(
													CreditRiskConstants.FX_RATE_CONVERSION_MISSING,
													this.counterpartyDetails
															.getCurrency(),
													item.getPayInCurrency()));
							this.getItemResponse().setStatus(
									CreditRiskConstants.FAILURE);
							return false;
						}
					}
				}
			}
	}
		
		boolean isSuccess  = doCounterpartyExposureFxConversion(tccrDetails.getCounterpartyDetails());

		return isSuccess;
	
	}

	public String getPaymentTerm() {
		return paymentTerm;
	}

	public void setPaymentTerm(String paymentTerm) {
		this.paymentTerm = paymentTerm;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}
	
	@Override
	protected boolean isValidStatus(String counterPartyCreditRiskStatus,
			ItemResponse ir,String contractType) {
		boolean isValid = true;
		switch (counterPartyCreditRiskStatus) {
		case CreditRiskConstants.INACTIVE:
			ir.setDescription(CreditRiskConstants.INVALID_COUNTERPARTY_INVOICE);
			isValid = false;
			break;
		case CreditRiskConstants.AUTOMATIC_SUSPENSION:
			ir.setDescription(CreditRiskConstants.MESSAGE_AUTOMATIC_SUSPENSION_INVOICE);
			isValid = false;
			break;

		case CreditRiskConstants.DELIVERY_STOP:
			ir.setDescription(CreditRiskConstants.MESSAGE_DELIVERY_STOP_INVOICE);
			isValid = false;
			break;
		default:
			break;
		}

		return isValid;
	}

}
