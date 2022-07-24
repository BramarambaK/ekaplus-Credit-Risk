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

public class PrepaymentInvoiceCreditCheckCalculator extends
		CreditCheckCalculator {

	private CounterPartyDetails counterpartyDetails;
	private String contractType;
	private BigDecimal prePaymentPercentage;

	public PrepaymentInvoiceCreditCheckCalculator() {
		super();
	}

	public PrepaymentInvoiceCreditCheckCalculator(
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

		String counterPartyCreditRiskStatus = this.counterpartyDetails
				.getCreditRiskStatusDisplayName();

		if (!isValidStatus(counterPartyCreditRiskStatus, itemResponse,
				this.getContractType())) {
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
					.setDescription(String
							.format(CreditRiskConstants.MESSAGE_LIMIT_MAINTENANCE_NOT_FOUND_SOFT_BLOCK,
									itemResponse.getCounterParty(),counterPartyCreditRiskStatus));
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			return false;
		} else {
			
			Map<String,Object> limitRefNos = new HashMap<>();
			for (Item  item : tccrDetails.getItems()) {
				if(item.getLimitRefNo()!=null)
				limitRefNos.put(item.getLimitRefNo(), null);
			}
			 
			if (!limitRefNos.isEmpty()) {
				Set<String> limitRefNo = limitRefNos.keySet();
				for(String l : limitRefNo){
				Optional<LimitMaintenanceDetails> findFirst = this
						.getLimitMaintenanceDetails()
						.stream()
						.filter(e -> l.equalsIgnoreCase(e.getLimitRefNo()))
						.findFirst();
				if (!findFirst.isPresent()) {
					itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
					itemResponse
							.setDescription(String.format(CreditRiskConstants.LIMIT_REF_NO_NOT_MATCH,
									l));
					itemResponse.setStatus(CreditRiskConstants.FAILURE);
					return false;
				}
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
										new FxRatesKey(this.counterpartyDetails
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
		boolean isSuccess = doCounterpartyExposureFxConversion(tccrDetails
				.getCounterpartyDetails());

		return isSuccess;
	}

	@Override
	protected void isHardStop(List<LimitMaintenanceDetails> list,
			CreditLimitTypeGroupEnum groupEnum) {
		Set<String> limitRefNos = new HashSet<>();
		for (TCCRDetails tccrDetails : this.getTccrDetails()) {
			for (Item  item : tccrDetails.getItems()) {
				
				if (item.getLimitRefNo() != null) {
					limitRefNos.add(item.getLimitRefNo());
				}
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

	protected boolean isValidStatus(String counterPartyCreditRiskStatus,
			ItemResponse ir, String contractType) {
		boolean isValid = true;
		switch (counterPartyCreditRiskStatus) {
		case CreditRiskConstants.INACTIVE:
			ir.setDescription(CreditRiskConstants.INVALID_COUNTERPARTY);
			isValid = false;
			break;

		case CreditRiskConstants.PRE_PAYMENT_STOP:
			if ("Purchase".equalsIgnoreCase(contractType)) {
				ir.setDescription(CreditRiskConstants.MESSAGE_PREPAYMENT_STOP);
				isValid = false;
			}
			break;
		default:
			break;
		}

		return isValid;
	}

	@Override
	public Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimitMaintenanceByLimitType() {

		final String counterParty = this.getTccrDetails().get(0)
				.getCounterParty();
		final String counterPartyGroup = this.getTccrDetails().get(0)
				.getCounterPartyGroup();
		Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> map = new HashMap<>();
		map.put(CreditLimitTypeGroupEnum.PRE_BOOKED,
				new ArrayList<LimitMaintenanceDetails>());
		map.put(CreditLimitTypeGroupEnum.TEMPORARY,
				new ArrayList<LimitMaintenanceDetails>());
		map.put(CreditLimitTypeGroupEnum.POOL,
				new ArrayList<LimitMaintenanceDetails>());

		if (this.getLimitMaintenanceDetails() != null) {
			for (LimitMaintenanceDetails limitMaintenanceDetails2 : this
					.getLimitMaintenanceDetails()) {
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
						CreditRiskConstants.PPI_MESSAGE_PAYMENTTERM_PP,
						this.counterpartyDetails
								.getCreditRiskStatusDisplayName(),
								CreditCheckCalculator.getLocaleFormatedStringAmount(
										finalAvailableBalance.abs(),
										this.getCurrenyLocale()),
						this.counterpartyDetails.getCurrency()));
			} else {
				itemResponse.setBlockType(null);
				itemResponse
						.setDescription(CreditRiskConstants.creditCheckSuccess);
				itemResponse.setStatus(CreditRiskConstants.SUCCESS);
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
