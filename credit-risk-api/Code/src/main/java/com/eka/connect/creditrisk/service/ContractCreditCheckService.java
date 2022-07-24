package com.eka.connect.creditrisk.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.eka.connect.creditrisk.SpringBeanContext;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.PlatformConstants;
import com.eka.connect.creditrisk.dataobject.ContractCreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CounterpartyExposure;
import com.eka.connect.creditrisk.dataobject.CreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.DerivedPaymentTerms;
import com.eka.connect.creditrisk.dataobject.FilterData;
import com.eka.connect.creditrisk.dataobject.FxRates;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.util.ContractCreditCheckCalculator;
import com.eka.connect.creditrisk.util.RestTemplateUtil;

@Service
public class ContractCreditCheckService extends CreditCheckService {

	@Autowired
	private PlatformDataService platformDataService;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	private static final Logger LOGGER = ESAPI
			.getLogger(ContractCreditCheckService.class);

	public ItemResponse doCreditCheck(
			ContractCreditCheckRequest contractCreditCheckRequest)
			throws Exception {
		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("initiating credit check for contract entity........."));

		CreditCheckRequest ccRequest = populateCreditCheckRequestObject(contractCreditCheckRequest);
		// Get counter party data.
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("counterpartyName",
				contractCreditCheckRequest.getCounterParty());
		FilterData counterpartyFilterData = this
				.getCounterpartyFilterData(contractCreditCheckRequest
						.getCounterParty());
		
		FilterData limitFilterData = this
				.getLimitMaintenanceFilterData(contractCreditCheckRequest
						.getCounterParty());
		ContractCreditCheckService service = SpringBeanContext
				.getBean(ContractCreditCheckService.class);
		List<CounterPartyDetails> counterPartyDetails = null;
		CompletableFuture<List<CounterPartyDetails>> counterPartyDetailsFuturesObj = service
				.getCounterPartyDetailsAsync(
						restTemplateUtil.getCommonHttpHeaders(),counterpartyFilterData);
		List<LimitMaintenanceDetails> limitMaintenanceDetails = null;
		CompletableFuture<List<LimitMaintenanceDetails>> limitMaintenanceDetailsFuturesObj = service
				.getLimitMaintenanceDetailsAsync(
						restTemplateUtil.getCommonHttpHeaders(),limitFilterData);

		// get counter party exposure data
		List<CounterpartyExposure> counterpartyExposures = null;
		CompletableFuture<List<CounterpartyExposure>> counterpartyExposuresFuturesObj = service
				.getCounterPartyExposuresAsync(restTemplateUtil.getCommonHttpHeaders(),contractCreditCheckRequest,
						requestContextHolder.getCurrentContext()
								.getAppProperties());

		List<DerivedPaymentTerms> derivedPaymentTerms = null;
		CompletableFuture<List<DerivedPaymentTerms>> derivedPaymentTermsFuturesObj = service
				.getDerivedPaymentTermsAsync(
						restTemplateUtil.getCommonHttpHeaders(),contractCreditCheckRequest,
						requestContextHolder.getCurrentContext()
								.getAppProperties());
		List<FxRates> fxRatesList = null;
		CompletableFuture<List<FxRates>> fxRateListFuturesObj = service
				.getFxRatesAsync(restTemplateUtil.getCommonHttpHeaders(),
						requestContextHolder.getCurrentContext()
								.getAppProperties());

		// Wait until they are all done
		CompletableFuture.allOf(counterPartyDetailsFuturesObj,
				limitMaintenanceDetailsFuturesObj,
				counterpartyExposuresFuturesObj, derivedPaymentTermsFuturesObj,
				fxRateListFuturesObj).join();

		counterPartyDetails = counterPartyDetailsFuturesObj.get();
		limitMaintenanceDetails = limitMaintenanceDetailsFuturesObj.get();
		counterpartyExposures = counterpartyExposuresFuturesObj.get();
		derivedPaymentTerms = derivedPaymentTermsFuturesObj.get();
		fxRatesList = fxRateListFuturesObj.get();

		CounterPartyDetails c = null;
		if (counterPartyDetails != null && (!counterPartyDetails.isEmpty())) {
			c = counterPartyDetails.get(0);
			for (TCCRDetails t : ccRequest.getTccrDetails()) {

				t.setCounterpartyDetails(c);
			}
		}

		limitMaintenanceDetails = this.filterLimitDataBylimitType(
				limitMaintenanceDetails,
				contractCreditCheckRequest.getContractType());

		Map<FxRatesKey, BigDecimal> fxRatesMap = prepareFxRatesMap(fxRatesList);
		Map<String, String> map = new HashMap<>();
		if (derivedPaymentTerms != null && !derivedPaymentTerms.isEmpty()) {
			derivedPaymentTerms.forEach(d -> {
				map.put(d.getPaymentTerm(), d.getDerivedPaymentTerm());
			});
		}

		ContractCreditCheckCalculator contractCalculator = new ContractCreditCheckCalculator(
				c, limitMaintenanceDetails, ccRequest.getTccrDetails());
		contractCalculator.setContractType(contractCreditCheckRequest
				.getContractType());
		contractCalculator.setCounterPartyExposure(counterpartyExposures);
		contractCalculator.setDerivedPaymentTerms(map);
		contractCalculator.setFxRatesMap(fxRatesMap);
		contractCalculator.setPrePaymentPercentage(contractCreditCheckRequest
				.getPrepaymentPercentage());
		contractCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());

		ItemResponse finalResponse = contractCalculator.calculate();

		service.saveCreditCheckRequestResponseAsync(restTemplateUtil.getCommonHttpHeaders(),ccRequest, finalResponse);

		return finalResponse;
	}

	@Async
	public CompletableFuture<List<DerivedPaymentTerms>> getDerivedPaymentTermsAsync(HttpHeaders httpHeaders,
			ContractCreditCheckRequest request,
			Map<String, Object> appProperties)
			throws Exception {

		List<DerivedPaymentTerms> derivedPaymentTerms = this
				.getDerivedPaymentTerms(request, httpHeaders, appProperties);
		return CompletableFuture.completedFuture(derivedPaymentTerms);
	}

	private List<DerivedPaymentTerms> getDerivedPaymentTerms(
			ContractCreditCheckRequest request, HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws Exception {
		JSONObject filterObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject eqobj = new JSONObject();
		eqobj.put(PlatformConstants.FIELD_NAME, "paymentTerm");
		eqobj.put(PlatformConstants.VALUE, request.getPaymentTerm());
		eqobj.put(PlatformConstants.OPERATOR, "eq");
		jsonArray.put(eqobj);
		filterObject.put(PlatformConstants.FILTER, jsonArray);
		return platformDataService.getDerivedPaymentTerms(filterObject,
				httpHeaders, appProperties);
	}

	@Async
	public CompletableFuture<List<CounterpartyExposure>> getCounterPartyExposuresAsync(
			HttpHeaders httpHeaders, ContractCreditCheckRequest request,
			Map<String, Object> appProperties)
			throws Exception {
		List<CounterpartyExposure> counterPartyExposures = this
				.getCounterPartyExposures(request, httpHeaders, appProperties);
		return CompletableFuture.completedFuture(counterPartyExposures);
	}

	public List<CounterpartyExposure> getCounterPartyExposures(
			ContractCreditCheckRequest request, HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws Exception {

		JSONObject filterObject = prepareFilterObjectForExposures(request);
		List<CounterpartyExposure> counterPartyExposures = platformDataService
				.getCounterPartyExposures(filterObject, httpHeaders,
						appProperties);
		if ((!CollectionUtils.isEmpty(counterPartyExposures))
				&& (CreditRiskConstants.OPERATION_TYPE_AMEND
						.equalsIgnoreCase(request.getOperationType()) || CreditRiskConstants.OPERATION_TYPE_MODIFY
						.equalsIgnoreCase(request.getOperationType()))) {
			counterPartyExposures = counterPartyExposures
					.stream()
					.filter(e -> (!request.getContractRefNo().equalsIgnoreCase(
							e.getContractRefNo())))
					.collect(Collectors.toList());
			
		}
		if (!CollectionUtils.isEmpty(counterPartyExposures)) {
			counterPartyExposures
					.stream()
					.filter(e -> (CreditRiskConstants.EXPOSURE_TYPE_AMOUNT_PREPAID_BUT_NOT_UTILIZED
							.equalsIgnoreCase(e.getExposureType()) || CreditRiskConstants.EXPOSURE_TYPE_L_CS_NOT_UTILIZED
							.equalsIgnoreCase(e.getExposureType())))
					.forEach(e -> {
						e.setValue(BigDecimal.ZERO.subtract(e.getValue()));

					});
		}
		return counterPartyExposures;

	}

	private JSONObject prepareFilterObjectForExposures(
			ContractCreditCheckRequest request) {
		JSONObject filterObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		if (CreditRiskConstants.OPERATION_TYPE_AMEND.equalsIgnoreCase(request
				.getOperationType())
				|| CreditRiskConstants.OPERATION_TYPE_MODIFY
						.equalsIgnoreCase(request.getOperationType())) {

			JSONObject neObj = new JSONObject();
			neObj.put(PlatformConstants.FIELD_NAME, "contractRefNo");
			neObj.put(PlatformConstants.VALUE, request.getContractRefNo());
			neObj.put(PlatformConstants.OPERATOR, "ne");
			jsonArray.put(neObj);

			JSONObject inObj = new JSONObject();
			inObj.put(PlatformConstants.FIELD_NAME, "exposureType");
			inObj.put(
					PlatformConstants.VALUE,
					String.join(
							",",
							"Sales".equalsIgnoreCase(request.getContractType()) ? CreditRiskConstants.CONTRACT_EXPOSURE_TYPES_MODIFY_AMEND
									: CreditRiskConstants.PURCHASE_CONTRACT_EXPOSURE_TYPES_CREATE));
			inObj.put(PlatformConstants.OPERATOR, "in");
			jsonArray.put(inObj);

		} else {

			JSONObject inObj = new JSONObject();
			inObj.put(PlatformConstants.FIELD_NAME, "exposureType");
			inObj.put(
					PlatformConstants.VALUE,
					String.join(
							",",
							"Sales".equalsIgnoreCase(request.getContractType()) ? CreditRiskConstants.CONTRACT_EXPOSURE_TYPES_CREATE
									: CreditRiskConstants.PURCHASE_CONTRACT_EXPOSURE_TYPES_CREATE));
			inObj.put(PlatformConstants.OPERATOR, "in");
			jsonArray.put(inObj);

		}
		JSONObject eqobj = new JSONObject();
		eqobj.put(PlatformConstants.FIELD_NAME, "counterparty");
		eqobj.put(PlatformConstants.VALUE, request.getCounterParty());
		eqobj.put(PlatformConstants.OPERATOR, "eq");
		jsonArray.put(eqobj);
		filterObject.put(PlatformConstants.FILTER, jsonArray);

		return filterObject;
	}

	private CreditCheckRequest populateCreditCheckRequestObject(
			ContractCreditCheckRequest contractCreditCheckRequest) {
		CreditCheckRequest ccr = new CreditCheckRequest();
		ccr.setEntityType(CreditRiskConstants.CONTRACT);
		ccr.setEntityRefNo(contractCreditCheckRequest.getEntityRefNo());
		ccr.setContractType(contractCreditCheckRequest.getContractType());
		ccr.setEventName(contractCreditCheckRequest.getEventName());
		List<TCCRDetails> tccrDetailsList = new ArrayList<>();
		ccr.setTccrDetails(tccrDetailsList);
		TCCRDetails tccrDetails = new TCCRDetails();
		tccrDetailsList.add(tccrDetails);
		tccrDetails.setContractRefNo(contractCreditCheckRequest
				.getContractRefNo());
		tccrDetails.setCounterParty(contractCreditCheckRequest
				.getCounterParty());
		tccrDetails.setCounterPartyGroup(contractCreditCheckRequest
				.getCounterPartyGroup());

		tccrDetails.setPaymentTerm(contractCreditCheckRequest.getPaymentTerm());
		tccrDetails.setOperationType(contractCreditCheckRequest
				.getOperationType());
		List<Item> itemsList = new ArrayList<>();
		tccrDetails.setItems(itemsList);
		contractCreditCheckRequest
				.getItemDetails()
				.forEach(
						e -> {

							Item item = new Item();
							item.setPayInCurrency(e.getPayInCurrency());
							item.setValue(e.getValue());

							if (!(StringUtils.isEmpty(contractCreditCheckRequest.getLimitRefNo()) || contractCreditCheckRequest
									.getLimitRefNo().trim().length() == 0)) {
								item.setLimitRefNo(contractCreditCheckRequest
										.getLimitRefNo().trim());
							}
							item.setValueInCounterPartyCurrency(e.getValue());
							if ("Purchase"
									.equalsIgnoreCase(contractCreditCheckRequest
											.getContractType())
									&& contractCreditCheckRequest
											.getPrepaymentPercentage() != null) {
								item.setValueInCounterPartyCurrency(getPercenategeValue(
										contractCreditCheckRequest
												.getPrepaymentPercentage(),
										item.getValue()));
							}
							item.setContractItemRefNo(e.getContractItemRefNo());
							item.setFromPeriod(e.getFromPeriod());
							item.setToPeriod(e.getToPeriod());
							item.setTccrDetails(tccrDetails);
							itemsList.add(item);

						});
		return ccr;

	}

	public static BigDecimal getPercenategeValue(BigDecimal percent,
			BigDecimal value) {
		if (percent != null) {
			return value
					.multiply(percent)
					.divide(new BigDecimal(100))
					.setScale(CreditRiskConstants.SCALE,
							CreditRiskConstants.ROUNDING_MODE);
		}
		return value;
	}

	private List<LimitMaintenanceDetails> filterLimitDataBylimitType(
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			String contractType) {
		if ("Sales".equalsIgnoreCase(contractType))
			return super.filterLimitDataBylimitType(limitMaintenanceDetails);
		else {
			if (CollectionUtils.isEmpty(limitMaintenanceDetails)) {
				return limitMaintenanceDetails;
			}
			Map<String, Object> map = new HashMap<>();
			map.put(CreditRiskConstants.PRE_PAYMENT_FULL_TERM, null);
			map.put(CreditRiskConstants.PRE_PAYMENT_PARTIAL_TERM, null);
			map.put(CreditRiskConstants.PRE_PAYMENT_LIMIT, null);
			map.put(CreditRiskConstants.PRE_PAYMNET_CREDIT_LIMIT, null);
			limitMaintenanceDetails = limitMaintenanceDetails
					.stream()
					.filter(e -> map.containsKey(e
							.getCreditLimitTypeDisplayName()))
					.collect(Collectors.toList());
			return limitMaintenanceDetails;
		}
	}

}
