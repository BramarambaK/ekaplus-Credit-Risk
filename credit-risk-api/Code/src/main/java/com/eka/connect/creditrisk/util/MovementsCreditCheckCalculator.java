/**
 * 
 */
package com.eka.connect.creditrisk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.eka.connect.creditrisk.constants.CreditLimitTypeEnum;
import com.eka.connect.creditrisk.constants.CreditLimitTypeGroupEnum;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;

/**
 * @author rajeshks
 *
 */
public class MovementsCreditCheckCalculator extends CreditCheckCalculator {

	private String counterpartyGroup = null;

	public String getCounterpartyGroup() {
		return counterpartyGroup;
	}

	public void setCounterpartyGroup(String counterpartyGroup) {
		this.counterpartyGroup = counterpartyGroup;
	}

	public MovementsCreditCheckCalculator(
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<TCCRDetails> tccrDetails) {

		super(limitMaintenanceDetails, tccrDetails);
	}

	@Override
	public boolean precheck() {

		// Step 1
		for (TCCRDetails tccrDetails : this.getTccrDetails()) {

			itemResponse.setCounterParty(tccrDetails.getCounterParty());
			itemResponse.setCounterPartyGroup(tccrDetails
					.getCounterPartyGroup());
			if (null == tccrDetails.getCounterpartyDetails()) {
				itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
				itemResponse.setDescription(String.format(
						CreditRiskConstants.INVALID_COUNTERPARTY_PBS,
						this.getEventName(), itemResponse.getCounterParty()));
				itemResponse.setStatus(CreditRiskConstants.FAILURE);
				return false;

			}
			else{
				if(!validateCounterpartyDetails(tccrDetails.getCounterpartyDetails(),itemResponse)){
					return false;
				}
			}
			String counterPartyCreditRiskStatus = tccrDetails
					.getCounterpartyDetails().getCreditRiskStatusDisplayName();

			if (!isValidStatus(counterPartyCreditRiskStatus, itemResponse)) {
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

				// TODO: to fix performance issue here.

				for (Item item : tccrDetails.getItems()) {
				if (item.getLimitRefNo() != null) {
					Optional<LimitMaintenanceDetails> findFirst = this
							.getLimitMaintenanceDetails()
							.stream()
							.filter(e -> item.getLimitRefNo()
									.equalsIgnoreCase(e.getLimitRefNo()))
							.findFirst();
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
							tccrDetails.getCounterpartyDetails().getCurrency())) {
						if (this.getFxRatesMap() == null
								|| !this.getFxRatesMap().containsKey(
										new FxRatesKey(tccrDetails
												.getCounterpartyDetails()
												.getCurrency(), item
												.getPayInCurrency()))) {
							this.getItemResponse().setBlockType(
									CreditRiskConstants.HARD_BLOCK);
							this.getItemResponse()
									.setDescription(
											String.format(
													CreditRiskConstants.FX_RATE_CONVERSION_MISSING,
													tccrDetails
															.getCounterpartyDetails()
															.getCurrency(),
													item.getPayInCurrency()));
							this.getItemResponse().setStatus(
									CreditRiskConstants.FAILURE);
							return false;
						}
					}
				}
				boolean isSuccess = doCounterpartyExposureFxConversion(tccrDetails.getCounterpartyDetails());
				if(!isSuccess){
					return false;
				}

			}
		}
		return true;

	}

	@Override
	public Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimitMaintenanceByLimitType() {

		Map<String, Object> counterpartyNames = new HashMap<>();
		Map<String, Object> counterpartyGroupNames = new HashMap<>();
		for (TCCRDetails tccrDetails : this.getTccrDetails()) {
			counterpartyNames.put(tccrDetails.getCounterParty(), null);
			counterpartyGroupNames
					.put(tccrDetails.getCounterPartyGroup(), null);
		}

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
				if (counterpartyNames.containsKey(limitMaintenanceDetails2
						.getCounterpartyGroupNameDisplayName())) {
					limitMaintenanceDetails2.setChartingOrder(-1);
				} else if (counterpartyGroupNames
						.containsKey(limitMaintenanceDetails2
								.getCounterpartyGroupNameDisplayName())) {
					limitMaintenanceDetails2.setChartingOrder(0);
				}

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

	@Override
	public void deriveFinalResponse() {

		if (this.getCounterpartyGroup() != null) {
			itemResponse.setCounterParty(null);
			itemResponse.setCounterPartyGroup(this.getCounterpartyGroup());
		}

		TCCRDetails t = this.getTccrDetails().get(0);
		String counterparty = t.getCounterParty();
		String paymentTerm = t.getPaymentTerm();
		String cpCrmStats = t.getCounterpartyDetails()
				.getCreditRiskStatusDisplayName();
		String currency = t.getCounterpartyDetails().getCurrency();
		if (this.finalAvailableBalance.compareTo(BIG_DECIMAL_ZERO) >= 0) {
			itemResponse.setBlockType(null);
			itemResponse.setDescription(CreditRiskConstants.creditCheckSuccess);
			itemResponse.setStatus(CreditRiskConstants.SUCCESS);
		} else {

			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			itemResponse.setBlockType(CreditRiskConstants.SOFT_BLOCK);

			if (this.getCounterpartyGroup() != null) {
				if (derivedPaymentTerms.get(paymentTerm).equalsIgnoreCase(
						CreditRiskConstants.LC)) {
					itemResponse
							.setDescription(String
									.format(CreditRiskConstants.COUNTERPARTYGROUP_PBS_MO_MESSAGE,
											this.getCounterpartyGroup(),
											CreditCheckCalculator.getLocaleFormatedStringAmount(
													finalAvailableBalance.abs(),
													this.getCurrenyLocale()), currency));
				} else if (derivedPaymentTerms.get(paymentTerm)
						.equalsIgnoreCase(CreditRiskConstants.PP)) {
					itemResponse
							.setDescription(String
									.format(CreditRiskConstants.COUNTERPARTYGROUP_PBS_MO_MESSAGE,
											this.getCounterpartyGroup(),
											CreditCheckCalculator.getLocaleFormatedStringAmount(
													finalAvailableBalance.abs(),
													this.getCurrenyLocale()), currency));
				} else {
					itemResponse
							.setDescription(String
									.format(CreditRiskConstants.COUNTERPARTYGROUP_PBS_MO_MESSAGE_HARD_BLOCK,
											this.getCounterpartyGroup(),
											CreditCheckCalculator.getLocaleFormatedStringAmount(
													finalAvailableBalance.abs(),
													this.getCurrenyLocale()), currency));
					itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);

				}
			}

			else {

				if (derivedPaymentTerms.get(paymentTerm).equalsIgnoreCase(
						CreditRiskConstants.LC)) {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.COUNTERPARTY_PBS_MO_MESSAGE,
							counterparty, cpCrmStats, CreditCheckCalculator.getLocaleFormatedStringAmount(
									finalAvailableBalance.abs(),
									this.getCurrenyLocale()), currency));
				} else if (derivedPaymentTerms.get(paymentTerm)
						.equalsIgnoreCase(CreditRiskConstants.PP)) {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.COUNTERPARTY_PBS_MO_MESSAGE,
							counterparty, cpCrmStats, CreditCheckCalculator.getLocaleFormatedStringAmount(
									finalAvailableBalance.abs(),
									this.getCurrenyLocale()), currency));
				} else {
					itemResponse.setDescription(String.format(
							CreditRiskConstants.COUNTERPARTY_PBS_MO_MESSAGE_HARD_BLOCK,
							counterparty, cpCrmStats, CreditCheckCalculator.getLocaleFormatedStringAmount(
									finalAvailableBalance.abs(),
									this.getCurrenyLocale()), currency));
					itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);

				}

			}

		}

	}
	
	public List<LimitMaintenanceDetails> filterLimit(
			List<LimitMaintenanceDetails> list, TCCRDetails tccrDetails) {
		if (list == null || list.size() == 0) {
			return list;
		}
		List<LimitMaintenanceDetails> newList = list
				.stream()
				.filter(l ->

				l.getCounterpartyGroupNameDisplayName().equalsIgnoreCase(
						tccrDetails.getCounterParty())
						||

						l.getCounterpartyGroupNameDisplayName()
								.equalsIgnoreCase(
										tccrDetails.getCounterPartyGroup()))
				.collect(Collectors.toList());

		return newList;
	}
	protected boolean isValidStatus(String counterPartyCreditRiskStatus,
			ItemResponse ir) {
		boolean isValid = true;
		switch (counterPartyCreditRiskStatus) {
		case CreditRiskConstants.INACTIVE:
			ir.setDescription(String.format(CreditRiskConstants.INVALID_COUNTERPARTY_PBS,this.getEventName(),ir.getCounterParty()));
			isValid = false;
			break;
		case CreditRiskConstants.AUTOMATIC_SUSPENSION:
			ir.setDescription(String.format(CreditRiskConstants.MESSAGE_AUTOMATIC_SUSPENSION_PBS,this.getEventName(),ir.getCounterParty()));
			isValid = false;
			break;

		case CreditRiskConstants.DELIVERY_STOP:
			ir.setDescription(String.format(CreditRiskConstants.MESSAGE_DELIVERY_STOP_PBS,this.getEventName(),ir.getCounterParty()));
			isValid = false;
			break;
		default:
			break;
		}

		return isValid;
	}
	 

}
