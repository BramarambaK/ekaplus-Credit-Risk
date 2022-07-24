package com.eka.connect.creditrisk.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.util.StringUtils;

import com.eka.connect.creditrisk.constants.CreditLimitTypeGroupEnum;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CounterpartyExposure;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.MutableDouble;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;

public abstract class CreditCheckCalculator {

	private static final Logger LOGGER = ESAPI
			.getLogger(CreditCheckCalculator.class);

	
	private List<LimitMaintenanceDetails> limitMaintenanceDetails;
	private List<TCCRDetails> tccrDetailsList;
	private List<CounterpartyExposure> counterPartyExposure;
	
	private Map<FxRatesKey,BigDecimal> fxRatesMap;

	private double tccrAmount;

	private double availableBalanceAfterExposure;

	protected ItemResponse itemResponse;

	private String exposureDataUrl;

	public BigDecimal BIG_DECIMAL_ZERO = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);
	
	protected BigDecimal finalAvailableBalance = BigDecimal.ZERO.setScale(
			CreditRiskConstants.SCALE, CreditRiskConstants.ROUNDING_MODE);
	
	protected Map<String, String> derivedPaymentTerms;

	private Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimits;

	private String blockType;
	private String status;
	private double value;
	private boolean isHardStop;
	
	private String eventName;
	
	private Locale currenyLocale;

	
	/***
	 * Below two maps are created for parallel execution(multiple threads
	 * accessing same list).....
	 * */
	//protected Map<String, CreditCheckDataPojo> limitRiskData = null;
	
	//protected Map<Integer,Boolean> exposureConsiderationMap = null;

	public CreditCheckCalculator() {
		// TODO Auto-generated constructor stub
	}

	public CreditCheckCalculator(
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<TCCRDetails> tccrDetails) {
		this.limitMaintenanceDetails = limitMaintenanceDetails;
		this.tccrDetailsList = tccrDetails;
	}

	public List<LimitMaintenanceDetails> getLimitMaintenanceDetails() {
		return limitMaintenanceDetails;
	}

	public void setLimitMaintenanceDetails(
			List<LimitMaintenanceDetails> limitMaintenanceDetails) {
		this.limitMaintenanceDetails = limitMaintenanceDetails;
	}

	public List<TCCRDetails> getTccrDetails() {
		return tccrDetailsList;
	}

	public void setTccrDetails(List<TCCRDetails> tccrDetails) {
		this.tccrDetailsList = tccrDetails;
	}

	public List<CounterpartyExposure> getCounterPartyExposure() {
		return counterPartyExposure;
	}

	public void setCounterPartyExposure(
			List<CounterpartyExposure> counterPartyExposure) {
		this.counterPartyExposure = counterPartyExposure;
	}

	public ItemResponse calculate() {

		itemResponse = new ItemResponse();
		itemResponse.setDescription(CreditRiskConstants.creditCheckSuccess);
		itemResponse.setStatus(CreditRiskConstants.SUCCESS);

		boolean isValid = precheck();
		if (!isValid)
			return itemResponse;

		Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupByLimit = this.groupLimitMaintenanceByLimitType();
		this.setGroupLimits(groupByLimit);
		this.massageItemData();
		//initDataForParallelProcessing();
		this.doExposureCharting();
		this.doTccrAmountCharting();
		this.doBalanceCharting();
		if (isHardStop) {
			return itemResponse;
		}
		this.deriveFinalResponse();

		return itemResponse;
	}

	protected void massageItemData() {
		for (TCCRDetails tccrDetails : tccrDetailsList) {
		List<Item> items = tccrDetails.getItems();
		for (Item item : items) {

			// convert item value to value in counterparty currency and use this
			// new value going forward.
			if (!item.getTccrDetails().getCounterpartyDetails().getCurrency()
					.equalsIgnoreCase(item.getPayInCurrency())) {
				FxRatesKey key = new FxRatesKey( item.getPayInCurrency(),item.getTccrDetails().getCounterpartyDetails()
						.getCurrency());
				BigDecimal conversionFactor = fxRatesMap.get(key);
				if (conversionFactor != null) {
					item.setValueInCounterPartyCurrency(conversionFactor
							.multiply(item.getValueInCounterPartyCurrency()).setScale(
									CreditRiskConstants.SCALE,
									CreditRiskConstants.ROUNDING_MODE));
				}
			}
		}
		}
	}

	public abstract boolean precheck();

	// this method updates final balance and prepares final Message as well.
	protected void doBalanceCharting() {

		doBalanceChartingForPreBookLimits();
		this.considerNonChartedExposures();
		this.considerNonChartedTccrAmounts();
		

	}

	protected void considerNonChartedTccrAmounts() {

		for (TCCRDetails tccrDetails : tccrDetailsList) {

			tccrDetails
					.getItems()
					.forEach(
							item -> {
								if (!item.isConsideredForCalculation()) {
									this.setFinalAvailableBalance(this
											.getFinalAvailableBalance()
											.subtract(
													item.getValueInCounterPartyCurrency()));
								}
							});
		}

	}
	
	protected void doBalanceChartingForPreBookLimits() {

		Map<String, LimitMaintenanceDetails> limitMap = groupLimits
				.get(CreditLimitTypeGroupEnum.PRE_BOOKED)
				.stream()
				.filter(l -> l.isConsiderForBalanceCharting())
				.collect(
						Collectors.groupingBy(
								LimitMaintenanceDetails::getLimitRefNo,
								Collectors.collectingAndThen(
										Collectors.toList(), l -> l.get(0))));

		if (limitMap != null && limitMap.size() > 0) {
			for (TCCRDetails tccrdetails : this.getTccrDetails()) {
				for (Item item : tccrdetails.getItems()) {
					
				
				if (item.getLimitRefNo() != null
						&& limitMap.containsKey(item.getLimitRefNo())) {
					LimitMaintenanceDetails lm = limitMap.get(item
							.getLimitRefNo());
					if (lm.isTccrAmountAllocated() && (!lm.isBalanceCalculated())) {
						lm.setBalance(lm.getAmount()
								.subtract(lm.getExposureAmount())
								.subtract(lm.getTccrAmount()));
						lm.setBalanceCalculated(true);
						finalAvailableBalance = finalAvailableBalance.add(lm
								.getBalance());
						LOGGER.debug(Logger.EVENT_SUCCESS,
								ESAPI.encoder().encodeForHTML(lm.toString()));
					}

				}
			}
			}

			
			this.isHardStop(
					groupLimits.get(CreditLimitTypeGroupEnum.PRE_BOOKED),
					CreditLimitTypeGroupEnum.PRE_BOOKED);
			if(this.isHardStop){
				return;
			}
			
			
			limitMap.forEach((k, v) -> {
				if (v.isConsiderForBalanceCharting()
						&& (!v.isTccrAmountAllocated())&&(!v.isBalanceCalculated())) {

					BigDecimal subtract = v.getAmount().subtract(
							v.getExposureAmount());
					if (subtract.doubleValue() < 0) {
						finalAvailableBalance = finalAvailableBalance
								.add(subtract);
						v.setBalanceCalculated(true);
						LOGGER.debug(Logger.EVENT_SUCCESS,
								ESAPI.encoder().encodeForHTML("consider -ve Balance ;"+v.toString()));
					}
				}

			});
		}

		this.doBalanceChartingForTemporaryLimits();
	}

	private void doBalanceChartingForTemporaryLimits() {
		groupLimits.get(CreditLimitTypeGroupEnum.TEMPORARY).stream()
				.filter(e -> e.isConsiderForBalanceCharting() &&(!e.isBalanceCalculated())).forEach(lm -> {
					lm.setBalance(lm.getAmount().subtract(lm.getExposureAmount()).subtract(lm.getTccrAmount()));
					finalAvailableBalance = finalAvailableBalance.add(lm.getBalance());
					lm.setBalanceCalculated(true);
					LOGGER.debug(Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(lm.toString()));
				});

		this.doBalanceChartingForPoolLimits();

	}

	protected void doBalanceChartingForPoolLimits() {
		groupLimits.get(CreditLimitTypeGroupEnum.POOL).stream()
				.filter(e -> e.isConsiderForBalanceCharting() && (!e.isBalanceCalculated())).forEach(lm -> {
					lm.setBalance(lm.getAmount().subtract(lm.getExposureAmount()).subtract(lm.getTccrAmount()));
					finalAvailableBalance = finalAvailableBalance.add(lm.getBalance());
					lm.setBalanceCalculated(true);
					LOGGER.debug(Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(lm.toString()));
				});
		
		
	}

	private void considerNonChartedExposures() {

		this.getCounterPartyExposure()
				.stream()
				.filter(e -> (!e.isConsideredForCreditCheck()))
				.forEach(
						ce -> {
							finalAvailableBalance = finalAvailableBalance
									.subtract(ce.getValue());
						});

	}

	protected void isHardStop(
			List<LimitMaintenanceDetails> list, CreditLimitTypeGroupEnum groupEnum) {
		this.isHardStop = false;
	}

	protected void doTccrAmountCharting() {
		for (TCCRDetails tccrDetails : tccrDetailsList) {
		List<Item> items = tccrDetails.getItems();
		for (Item item : items) {
			this.doTccrAmountChartingForPreBookLimits(item);

		}
		}
		

	}

	// this method has chaining with other methods as this method decides
	// whether to call other method or not
	protected void doTccrAmountChartingForTemporaryLimits(Item item) {
		// TODO Auto-generated method stub

		if (item.getFromPeriod() != null && item.getToPeriod() != null) {
			List<LimitMaintenanceDetails> lm = filterLimit(groupLimits
					.get(CreditLimitTypeGroupEnum.TEMPORARY),item.getTccrDetails());
			List<LimitMaintenanceDetails> findFirst = lm
					.stream()
					.filter(e -> item.getFromPeriod().compareTo(
							e.getFromPeriod()) >= 0
							&& item.getToPeriod().compareTo(e.getToPeriod()) <= 0

							&& item.getTccrDetails().getCounterParty().equalsIgnoreCase(
									e.getCounterpartyGroupNameDisplayName()))
					.collect(Collectors.toList());

			if (!findFirst.isEmpty()) {
				LimitMaintenanceDetails details = findFirst.get(0);
				details.setTccrAmount(details.getTccrAmount()
						.add(item.getValueInCounterPartyCurrency()));
				details.setTccrAmountAllocated(true);
				item.setConsideredForCalculation(true);
				details.setConsiderForBalanceCharting(true);
				for (int i = 1; i < findFirst.size(); i++) {
					findFirst.get(i).setConsiderForBalanceCharting(true);
				}
				return;
			} else {
				if (item.getTccrDetails().getCounterPartyGroup() != null) {
					findFirst = lm
							.stream()
							.filter(e -> item.getFromPeriod().compareTo(
									e.getFromPeriod()) >= 0
									&& item.getToPeriod().compareTo(
											e.getToPeriod()) <= 0

									&& item.getTccrDetails()
											.getCounterPartyGroup()
											.equalsIgnoreCase(
													e.getCounterpartyGroupNameDisplayName()))
							.collect(Collectors.toList());
					if (!findFirst.isEmpty()) {
						LimitMaintenanceDetails details = findFirst.get(0);
						details.setTccrAmount(details.getTccrAmount().
								add( item.getValueInCounterPartyCurrency()));
						details.setTccrAmountAllocated(true);
						item.setConsideredForCalculation(true);
						details.setConsiderForBalanceCharting(true);
						for (int i = 1; i < findFirst.size(); i++) {
							findFirst.get(i)
									.setConsiderForBalanceCharting(true);
						}
						return;
					}
				}
			}

		}
		this.doTccrAmountChartingForPoolLimits(item);

	}

	protected void doTccrAmountChartingForPoolLimits(Item item) {
		List<LimitMaintenanceDetails> lm = filterLimit(groupLimits
				.get(CreditLimitTypeGroupEnum.POOL),item.getTccrDetails());
		List<LimitMaintenanceDetails> findFirst = lm
				.stream()
				.filter(e -> item.getTccrDetails().getCounterParty().equalsIgnoreCase(
						e.getCounterpartyGroupNameDisplayName()))
				.collect(Collectors.toList());
		if (!findFirst.isEmpty()) {

			LimitMaintenanceDetails details = findFirst.get(0);
			details.setTccrAmount(details.getTccrAmount().add(
					item.getValueInCounterPartyCurrency()));
			details.setTccrAmountAllocated(true);
			item.setConsideredForCalculation(true);
			details.setConsiderForBalanceCharting(true);
			for (int i = 1; i < findFirst.size(); i++) {
				findFirst.get(i).setConsiderForBalanceCharting(true);
			}
			return;
		} else {
			if (item.getTccrDetails().getCounterPartyGroup() != null) {
				findFirst = lm
						.stream()
						.filter(e -> item.getTccrDetails()
								.getCounterPartyGroup()
								.equalsIgnoreCase(
										e.getCounterpartyGroupNameDisplayName()))
						.collect(Collectors.toList());
				;
				if (!findFirst.isEmpty()) {
					LimitMaintenanceDetails details = findFirst.get(0);
					details.setTccrAmount(details.getTccrAmount().add
							(item.getValueInCounterPartyCurrency()));
					item.setConsideredForCalculation(true);
					details.setTccrAmountAllocated(true);
					details.setConsiderForBalanceCharting(true);
					for (int i = 1; i < findFirst.size(); i++) {
						findFirst.get(i).setConsiderForBalanceCharting(true);
					}
					return;

				}
			}
		}
	}

	protected void doTccrAmountChartingForPreBookLimits(Item item) {

		List<LimitMaintenanceDetails> preBookedLimits = filterLimit(groupLimits
				.get(CreditLimitTypeGroupEnum.PRE_BOOKED),item.getTccrDetails());
		LimitMaintenanceDetails lm = null;
		if (item.getLimitRefNo() != null) {
			Optional<LimitMaintenanceDetails> findFirst = preBookedLimits
					.stream()
					.filter(e -> e.getCounterpartyGroupNameDisplayName()
							.equalsIgnoreCase(item.getTccrDetails().getCounterParty())
							&& item.getLimitRefNo().equalsIgnoreCase(
									e.getLimitRefNo())).findFirst();
			if (findFirst.isPresent()) {
				lm = findFirst.get();
			} else {

				if (item.getTccrDetails().getCounterPartyGroup() != null) {
					findFirst = preBookedLimits
							.stream()
							.filter(e -> e
									.getCounterpartyGroupNameDisplayName()
									.equalsIgnoreCase(
											item.getTccrDetails().getCounterPartyGroup())
									&& item.getLimitRefNo()
											.equalsIgnoreCase(e.getLimitRefNo()))
							.findFirst();
					if (findFirst.isPresent()) {
						lm = findFirst.get();
					}
				}
			}
			if (lm != null) {
				item.setConsideredForCalculation(true);
				lm.setConsiderForBalanceCharting(true);
				lm.setTccrAmount(lm.getTccrAmount().add(
						 item.getValueInCounterPartyCurrency()));
				lm.setTccrAmountAllocated(true);
				return;
			}
		}
		this.doTccrAmountChartingForTemporaryLimits(item);

	}

	// ideally all limit list should have decision ref no as the FS.
	// Hence we might not require limit.getdecisionRefNo check here
	private void decisionRefNoCheck(List<LimitMaintenanceDetails> list,
			Item item, ItemResponse itemResponse,
			List<CounterpartyExposure> exposureList) {

		for (LimitMaintenanceDetails l : list) {
		 
				Stream<CounterpartyExposure> filter = exposureList.stream()
						.filter(exposure -> l.getLimitRefNo().equalsIgnoreCase(
								exposure.getDecisionRefNo())
								&& (!exposure.isConsideredForCreditCheck()));

				MutableDouble sum = new MutableDouble();
				filter.forEach(e -> {
					sum.add(e.getValue().doubleValue());
					e.setConsideredForCreditCheck(true);
					l.setConsiderForBalanceCharting(true);
				});
				l.setExposureAmount(l.getExposureAmount().add(new BigDecimal( sum.getValue()).
						setScale(CreditRiskConstants.SCALE,
						CreditRiskConstants.ROUNDING_MODE)));
			
		}

	}

	/**
	 * This method does exposure charting.
	 * 
	 * @param groupByLimit
	 */
	protected void doExposureCharting() {
		for (TCCRDetails tccrDetails : tccrDetailsList) {
			List<Item> items = tccrDetails.getItems();
			for (Item item : items) {

				doExposureChartingForPreBookedLimit(item);

				doExposureChartingForTempraryLimit(item);

				doExposureChartingForPoolLimit(item);

			}
		}
	}

	protected void doExposureChartingForPoolLimit(Item item) {
		// POOL
		List<LimitMaintenanceDetails> poolLimit = filterLimit(groupLimits
				.get(CreditLimitTypeGroupEnum.POOL),item.getTccrDetails());
		for (LimitMaintenanceDetails pl : poolLimit) {
			List<CounterpartyExposure> cpExposureList = counterPartyExposure
					.stream().filter(e -> !e.isConsideredForCreditCheck())
					.collect(Collectors.toList());

			MutableDouble sum = new MutableDouble();
			cpExposureList
					.stream()
					.filter(e -> (e.getCounterparty() != null
							&& e.getCounterparty().equalsIgnoreCase(
									pl.getCounterpartyGroupNameDisplayName()) && !e
							.isConsideredForCreditCheck())).forEach(e -> {
						sum.add(e.getValue().doubleValue());
						e.setConsideredForCreditCheck(true);
					});
			pl.setExposureAmount(pl.getExposureAmount().add(
					new BigDecimal(sum.getValue()).setScale(
							CreditRiskConstants.SCALE,
							CreditRiskConstants.ROUNDING_MODE)));

			sum.setValue(0d);
			cpExposureList
					.stream()
					.filter(e -> (e.getCounterpartyGroup() != null
							&& e.getCounterpartyGroup().equalsIgnoreCase(
									pl.getCounterpartyGroupNameDisplayName()) && !e
							.isConsideredForCreditCheck())).forEach(e -> {
						sum.add(e.getValue().doubleValue());
						e.setConsideredForCreditCheck(true);
					});
			pl.setExposureAmount(pl.getExposureAmount() .add(new BigDecimal( sum.getValue()).
						setScale(CreditRiskConstants.SCALE,
						CreditRiskConstants.ROUNDING_MODE)));
			pl.setConsiderForBalanceCharting(true);
		}
	}

	protected void doExposureChartingForPreBookedLimit(Item item) {
			decisionRefNoCheck(
					filterLimit(
							groupLimits
									.get(CreditLimitTypeGroupEnum.PRE_BOOKED),
							item.getTccrDetails()), item, itemResponse,
					counterPartyExposure);
		
	}

	protected void doExposureChartingForTempraryLimit(Item item) {
		// Temporary
		if (!(StringUtils.isEmpty(item.getFromPeriod()) || StringUtils
				.isEmpty(item.getToPeriod()))) {
			List<LimitMaintenanceDetails> temporaryLlist = filterLimit(groupLimits
					.get(CreditLimitTypeGroupEnum.TEMPORARY),item.getTccrDetails());
			{
				for (LimitMaintenanceDetails lm : temporaryLlist) {
					if (lm.getFromPeriod() != null && lm.getToPeriod() != null) {

						List<CounterpartyExposure> cpExposures = counterPartyExposure
								.stream()
								.filter(c -> (!c.isConsideredForCreditCheck()
										&& c.getFromDate() != null
										&& c.getToDate() != null
										&& c.getFromDate().compareTo(
												lm.getFromPeriod()) >= 0 && c
										.getToDate()
										.compareTo(lm.getToPeriod()) <= 0)
										&& (!c.isConsideredForCreditCheck()))
								.collect(Collectors.toList());
						MutableDouble sum = new MutableDouble();
						cpExposures
								.stream()
								.filter(e -> e.getCounterparty() != null
										&& lm.getCounterpartyGroupNameDisplayName()
												.equalsIgnoreCase(
														e.getCounterparty()))
								.forEach(cp -> {
									sum.add(cp.getValue().doubleValue()
											);
									cp.setConsideredForCreditCheck(true);
									//CCR-612
									lm.setConsiderForBalanceCharting(true);

								});
						lm.setExposureAmount(lm.getExposureAmount()
								.add(new BigDecimal( sum.getValue()).
										setScale(CreditRiskConstants.SCALE,
												CreditRiskConstants.ROUNDING_MODE)));

						sum.setValue(0d);
						cpExposures
								.stream()
								.filter(e -> (e.getCounterpartyGroup() != null && !e
										.isConsideredForCreditCheck())
										&& lm.getCounterpartyGroupNameDisplayName()
												.equalsIgnoreCase(
														e.getCounterpartyGroup()))
								.forEach(cp -> {
									sum.add(cp.getValue().doubleValue());
									cp.setConsideredForCreditCheck(true);
									//CCR-612
									lm.setConsiderForBalanceCharting(true);

								});
						lm.setExposureAmount(lm.getExposureAmount()
								.add(new BigDecimal( sum.getValue()).
										setScale(CreditRiskConstants.SCALE,
												CreditRiskConstants.ROUNDING_MODE)));

					}
				}
			}
		}

	}

	protected boolean isValidStatus(String counterPartyCreditRiskStatus,
			ItemResponse ir,String contractType) {
		boolean isValid = true;
		switch (counterPartyCreditRiskStatus) {
		case CreditRiskConstants.INACTIVE:
			ir.setDescription(CreditRiskConstants.INVALID_COUNTERPARTY);
			isValid = false;
			break;
		case CreditRiskConstants.AUTOMATIC_SUSPENSION:
			ir.setDescription(CreditRiskConstants.MESSAGE_AUTOMATIC_SUSPENSION);
			if(!"Purchase".equalsIgnoreCase(contractType))
			isValid = false;
			break;

		case CreditRiskConstants.DELIVERY_STOP:
			ir.setDescription(CreditRiskConstants.MESSAGE_DELIVERY_STOP);
			if(!"Purchase".equalsIgnoreCase(contractType))
			isValid = false;
			break;
			
		case CreditRiskConstants.PRE_PAYMENT_STOP:
			if("Purchase".equalsIgnoreCase(contractType)){
			ir.setDescription(CreditRiskConstants.MESSAGE_PREPAYMENT_STOP);
			isValid = false;
			}
			break;
		default:
			break;
		}

		return isValid;
	}

	public abstract Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimitMaintenanceByLimitType();	

	public double getTccrAmount() {
		return tccrAmount;
	}

	public void setTccrAmount(double tccrAmount) {
		this.tccrAmount = tccrAmount;
	}

	public double getAvailableBalanceAfterExposure() {
		return availableBalanceAfterExposure;
	}

	public void setAvailableBalanceAfterExposure(
			double availableBalanceAfterExposure) {
		this.availableBalanceAfterExposure = availableBalanceAfterExposure;
	}

	public abstract void deriveFinalResponse();

	public ItemResponse getItemResponse() {
		return itemResponse;
	}

	public void setItemResponse(ItemResponse itemResponse) {
		this.itemResponse = itemResponse;
	}

	public String getExposureDataUrl() {
		return exposureDataUrl;
	}

	public void setExposureDataUrl(String exposureDataUrl) {
		this.exposureDataUrl = exposureDataUrl;
	}

	public Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> getGroupLimits() {
		return groupLimits;
	}

	public void setGroupLimits(
			Map<CreditLimitTypeGroupEnum, List<LimitMaintenanceDetails>> groupLimits) {
		this.groupLimits = groupLimits;
	}

	public BigDecimal getFinalAvailableBalance() {
		return finalAvailableBalance;
	}

	public void setFinalAvailableBalance(BigDecimal finalAvailableBalance) {
		this.finalAvailableBalance = finalAvailableBalance;
	}

	public String getBlockType() {
		return blockType;
	}

	public void setBlockType(String blockType) {
		this.blockType = blockType;
	} 

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isHardStop() {
		return isHardStop;
	}

	public void setHardStop(boolean isHardStop) {
		this.isHardStop = isHardStop;
	}

	public Map<String, String> getDerivedPaymentTerms() {
		return derivedPaymentTerms;
	}

	public void setDerivedPaymentTerms(Map<String, String> derivedPaymentTerms) {
		this.derivedPaymentTerms = derivedPaymentTerms;
	}

	public Map<FxRatesKey, BigDecimal> getFxRatesMap() {
		return fxRatesMap;
	}

	public void setFxRatesMap(Map<FxRatesKey, BigDecimal> fxRatesMap) {
		this.fxRatesMap = fxRatesMap;
	}

	
	/*private void initDataForParallelProcessing() {
		limitRiskData = new HashMap<>();
		this.limitMaintenanceDetails.forEach(l -> {
			limitRiskData.put(l.getLimitRefNo(), new CreditCheckDataPojo());
		});
		exposureConsiderationMap = new HashMap<>();
		this.getCounterPartyExposure().forEach(e -> {
			exposureConsiderationMap.put(e.hashCode(), false);
		});

	}*/
	
	
	public boolean doCounterpartyExposureFxConversion(
			CounterPartyDetails counterPartyDetails) {

		if (this.getCounterPartyExposure() != null
				&& this.getCounterPartyExposure().size() > 0) {
			for (CounterpartyExposure ce : this.getCounterPartyExposure()) {

				if (!counterPartyDetails.getCurrency().equalsIgnoreCase(
						ce.getValueCurrency())) {

					FxRatesKey key = new FxRatesKey(ce.getValueCurrency(),
							counterPartyDetails.getCurrency());
					if (this.getFxRatesMap() == null
							|| !this.getFxRatesMap().containsKey(key)) {
						this.getItemResponse().setBlockType(
								CreditRiskConstants.HARD_BLOCK);
						this.getItemResponse()
								.setDescription(
										String.format(
												CreditRiskConstants.FX_RATE_CONVERSION_MISSING,
												counterPartyDetails
														.getCurrency(), ce
														.getValueCurrency()));
						this.getItemResponse().setStatus(
								CreditRiskConstants.FAILURE);
						return false;
					}else{
						ce.setValue(ce.getValue().multiply(this.getFxRatesMap().get(key)).setScale(
								CreditRiskConstants.SCALE,
								CreditRiskConstants.ROUNDING_MODE));
					}
				}

			}

		}
		return true;
	}
	
/**
 * default behavior. Return the same list without filtering. 
 * @param list
 * @param tccrDetails
 * @return
 */
	public List<LimitMaintenanceDetails> filterLimit(
			List<LimitMaintenanceDetails> list, TCCRDetails tccrDetails) {
		 
		return list;
	}
	
	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Locale getCurrenyLocale() {
		return currenyLocale;
	}

	public void setCurrenyLocale(Locale currenyLocale) {
		this.currenyLocale = currenyLocale;
	}
	
	
	public static String getLocaleFormatedStringAmount(BigDecimal value,Locale locale){
		
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		DecimalFormat df = (DecimalFormat)nf;
		return df.format(value);
	}

	public boolean validateCounterpartyDetails(
			CounterPartyDetails counterpartyDetails, ItemResponse itemResponse) {

		boolean isValid = true;
		if (StringUtils.isEmpty(counterpartyDetails.getCurrency())
				|| counterpartyDetails.getCurrency().trim().length() == 0
				|| "NA".equalsIgnoreCase(counterpartyDetails.getCurrency())) {
			itemResponse.setBlockType(CreditRiskConstants.HARD_BLOCK);
			itemResponse
					.setDescription(CreditRiskConstants.COUNTERPARTY_CURRENCY_NOT_FOUND);
			itemResponse.setStatus(CreditRiskConstants.FAILURE);
			isValid = false;
		}
		return isValid;
	}
}
