package com.eka.connect.creditrisk.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.eka.connect.creditrisk.constants.CreditLimitTypeEnum;
import com.eka.connect.creditrisk.constants.CreditLimitTypeGroupEnum;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;

public class ContractCreditCheckCalculator extends CreditCheckCalculator {
	
	private CounterPartyDetails counterpartyDetails;
	private String contractType;
	private BigDecimal prePaymentPercentage;

	public ContractCreditCheckCalculator() {
		super();
	}

	public ContractCreditCheckCalculator(
			CounterPartyDetails counterPartyDetails,
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<TCCRDetails> tccrDetails) {
		super(limitMaintenanceDetails, tccrDetails);
		this.counterpartyDetails = counterPartyDetails;
	}

	public ItemResponse calculate() {
		return super.calculate();
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
		if("purchase".equalsIgnoreCase(this.getContractType())){
			
			if(derivedPaymentTerms!=null && derivedPaymentTerms.containsKey(tccrDetails
						.getPaymentTerm())){
			String derivedPP = 	derivedPaymentTerms.get(tccrDetails
						.getPaymentTerm());
			if(this.prePaymentPercentage==null || (derivedPP!=null && !(CreditRiskConstants.PP.equalsIgnoreCase(derivedPP)))){
				itemResponse.setBlockType(null);
				itemResponse
						.setDescription(CreditRiskConstants.PURCHASE_CONTRACT_SUCCESS_NO_PREPAYMENT);
				itemResponse.setStatus(CreditRiskConstants.SUCCESS);
				return false;
			}
			
			}
		}
		String counterPartyCreditRiskStatus = this.counterpartyDetails
				.getCreditRiskStatusDisplayName();

		if (!isValidStatus(counterPartyCreditRiskStatus, itemResponse,this.getContractType())) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;
		}

		if (null == derivedPaymentTerms
				|| derivedPaymentTerms.isEmpty()
				|| (!derivedPaymentTerms.containsKey(tccrDetails
						.getPaymentTerm()))) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			if ("create".equalsIgnoreCase(tccrDetails.getOperationType())) {
				itemResponse
						.setDescription(String
								.format(CreditRiskConstants.DERIVED_PAYMENT_TERMS_NOT_FOUND_CONTRACT_CREATE,
										tccrDetails.getPaymentTerm()));
			} else {
				itemResponse
						.setDescription(String
								.format(CreditRiskConstants.DERIVED_PAYMENT_TERMS_NOT_FOUND_CONTRACT_MODIFY,
										tccrDetails.getPaymentTerm()));
			}
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;

		}
		if (null == this.getLimitMaintenanceDetails()
				|| this.getLimitMaintenanceDetails().isEmpty()) {
			
			if("purchase".equalsIgnoreCase(this.getContractType())){
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);
			itemResponse
					.setDescription(String.format(CreditRiskConstants.MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK,itemResponse.getCounterParty(),
							counterPartyCreditRiskStatus));	
			}else{
			if(derivedPaymentTerms.get(tccrDetails.getPaymentTerm())
					.equalsIgnoreCase(CreditRiskConstants.OTHERS)){
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse
					.setDescription(String.format(CreditRiskConstants.MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK,itemResponse.getCounterParty(),
							counterPartyCreditRiskStatus));
			}else{
				itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);
				itemResponse.setStatus(CreditRiskConstants.FAILURE);
				itemResponse
						.setDescription(String.format(CreditRiskConstants.MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK,itemResponse.getCounterParty(),
								counterPartyCreditRiskStatus));	
			}
			}
			return false;
		} else {

			if (tccrDetails.getItems().get(0).getLimitRefNo() != null) { //all items will have same limit ref no for Contract (Sales/Purchase)
				Optional<LimitMaintenanceDetails> findFirst = this
						.getLimitMaintenanceDetails()
						.stream()
						.filter(e -> tccrDetails.getItems().get(0).getLimitRefNo()
								.equalsIgnoreCase(e.getLimitRefNo()))
						.findFirst();
				if (!findFirst.isPresent()) {
					itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
					itemResponse
							.setDescription(String.format(CreditRiskConstants.LIMIT_REF_NO_NOT_MATCH,
									tccrDetails.getItems().get(0).getLimitRefNo()));
					itemResponse.setStatus(CreditRiskConstants.FAILURE);
					return false;
				}
			}
		}

		// check if Fx Rates is available for given counterparty and item
		// pay-in currency.
		for (Item item : this.getTccrDetails().get(0).getItems()) {
			if (!item.getPayInCurrency().equalsIgnoreCase(
					this.counterpartyDetails.getCurrency())) {
				if (this.getFxRatesMap() == null
						|| !this.getFxRatesMap()
								.containsKey(
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
													.getCurrency(), item
													.getPayInCurrency()));
					this.getItemResponse().setStatus(
							CreditRiskConstants.FAILURE);
					return false;
				}
			}
		}
		boolean isSuccess = doCounterpartyExposureFxConversion(tccrDetails.getCounterpartyDetails());

		return isSuccess;
	}

	@Override
	protected void isHardStop(List<LimitMaintenanceDetails> list,
			CreditLimitTypeGroupEnum groupEnum) {
		Set<String> limitRefNos = new HashSet<>();
		for (TCCRDetails tccrDetails : this.getTccrDetails()) {
			if (tccrDetails.getItems().get(0).getLimitRefNo() != null) {//for contract limit ref no is same across items
				limitRefNos.add(tccrDetails.getItems().get(0).getLimitRefNo());
			}
		}
		if (groupEnum == CreditLimitTypeGroupEnum.PRE_BOOKED && list != null
				&& list.size() > 0 && limitRefNos.size() > 0) {

			List<LimitMaintenanceDetails> collect = list
					.stream()
					.filter(lm ->limitRefNos.contains(lm.getLimitRefNo()))
					.collect(Collectors.toList());

			if (collect != null
					&& collect.size() > 0
					&& this.getFinalAvailableBalance().compareTo(
							this.BIG_DECIMAL_ZERO) > 0d) {
				ItemResponse itemResponse = this.getItemResponse();
				itemResponse.setStatus(CreditRiskConstants.SUCCESS);
				itemResponse.setBlockType(null);
				itemResponse.setDescription(CreditRiskConstants.creditCheckSuccess);
				this.setHardStop(true);
			}

		}

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

		TCCRDetails tccrDetails = this.getTccrDetails().get(0);
		if (this.finalAvailableBalance.compareTo(BIG_DECIMAL_ZERO) >= 0) {
			itemResponse.setBlockType(null);
			itemResponse.setDescription(CreditRiskConstants.creditCheckSuccess);
			itemResponse.setStatus(CreditRiskConstants.SUCCESS);
		} else {

			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);
			 if (derivedPaymentTerms.get(tccrDetails.getPaymentTerm())
					.equalsIgnoreCase(CreditRiskConstants.PP)) {
				itemResponse.setDescription(String.format(
						CreditRiskConstants.MESSAGE_PAYMENTTERM_PP, this
								.counterpartyDetails
								.getCreditRiskStatusDisplayName(),
						CreditCheckCalculator.getLocaleFormatedStringAmount(
								finalAvailableBalance.abs(),
								this.getCurrenyLocale()), this
								.counterpartyDetails.getCurrency()));
				return;
			}
			 if("Purchase".equalsIgnoreCase(this.contractType)){
					itemResponse.setStatus(CreditRiskConstants.SUCCESS);
					itemResponse.setBlockType(null);
				 return;
			 }
			 if (derivedPaymentTerms.get(tccrDetails.getPaymentTerm())
					.equalsIgnoreCase(CreditRiskConstants.LC)) {
				itemResponse.setDescription(String.format(
						CreditRiskConstants.MESSAG3E_PAYMENTTERM_LC, this
								.counterpartyDetails
								.getCreditRiskStatusDisplayName(),
								CreditCheckCalculator.getLocaleFormatedStringAmount(
										finalAvailableBalance.abs(),
										this.getCurrenyLocale()), this
								.counterpartyDetails.getCurrency()));
			}  else {
				itemResponse.setDescription(String.format(
						CreditRiskConstants.MESSAGE_PAYMENTTERM_OTHER, this
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
	

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public BigDecimal getPrePaymentPercentage() {
		return prePaymentPercentage;
	}

	public void setPrePaymentPercentage(BigDecimal prePaymentPercentage) {
		this.prePaymentPercentage = prePaymentPercentage;
	}
	

}
