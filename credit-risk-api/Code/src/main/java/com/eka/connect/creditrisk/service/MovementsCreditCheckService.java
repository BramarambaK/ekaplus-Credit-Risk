package com.eka.connect.creditrisk.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import com.eka.connect.creditrisk.dataobject.MongoOperations;
import com.eka.connect.creditrisk.dataobject.MovementContractDetails;
import com.eka.connect.creditrisk.dataobject.MovementsCreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.executor.MovementsCreditCheckCallable;
import com.eka.connect.creditrisk.util.MovementsCreditCheckCalculator;
import com.eka.connect.creditrisk.util.RestTemplateUtil;

@Service("movementsCreditCheckService")
public class MovementsCreditCheckService extends CreditCheckService {

	private static final Logger LOGGER = ESAPI
			.getLogger(MovementsCreditCheckService.class);

	@Autowired
	private PlatformDataService platformDataService;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	public ItemResponse doCreditCheck(
			MovementsCreditCheckRequest movementsCreditCheckRequest)
			throws Exception {
		CreditCheckRequest ccRequest = populateCreditCheckRequestObject(movementsCreditCheckRequest);

		ItemResponse ir = checkIfNoSalesContractItemsFound(ccRequest);
		if (ir != null) {
			return ir;
		}

		Map<String, List<TCCRDetails>> uniqueCounterParties = new HashMap<>();
		Map<String, Set<String>> uniqueCounterPartyGroups = new HashMap<>();

		for (TCCRDetails tccrDetails : ccRequest.getTccrDetails()) {

			if (uniqueCounterParties.containsKey(tccrDetails.getCounterParty())) {
				uniqueCounterParties.get(tccrDetails.getCounterParty()).add(
						tccrDetails);
			} else {
				List<TCCRDetails> list = new ArrayList<>();
				list.add(tccrDetails);
				uniqueCounterParties.put(tccrDetails.getCounterParty(), list);
			}

			if (tccrDetails.getCounterPartyGroup() != null) {
				if (uniqueCounterPartyGroups.containsKey(tccrDetails
						.getCounterPartyGroup())) {
					uniqueCounterPartyGroups.get(
							tccrDetails.getCounterPartyGroup()).add(
							tccrDetails.getCounterParty());
				} else {
					Set<String> distinctCounterpartiesPerGroup = new HashSet<>();
					distinctCounterpartiesPerGroup.add(tccrDetails
							.getCounterParty());
					uniqueCounterPartyGroups.put(
							tccrDetails.getCounterPartyGroup(),
							distinctCounterpartiesPerGroup);
				}
			}

		}

		String[] array = uniqueCounterParties.keySet().toArray(
				new String[uniqueCounterParties.size()]);
		
		String commaSeparatedCounterparties = String.join(",", array);
		 
		FilterData counterPartyFilterData = new FilterData();
		counterPartyFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations operation = new MongoOperations();
		operation.setOperator("in");
		operation.setFieldName("counterpartyName");
		operation.setValue(array);
		counterPartyFilterData.getFilter().add(operation);
	

		MovementsCreditCheckService service = SpringBeanContext.getBean(
				"movementsCreditCheckService",
				MovementsCreditCheckService.class);

		List<CounterPartyDetails> counterPartyDetails = null;

		CompletableFuture<List<CounterPartyDetails>> counterPartyDetailsFuturesObj = service
				.getCounterPartyDetailsAsync(restTemplateUtil.getCommonHttpHeaders(),counterPartyFilterData);

		// get limit maintenance data
		// TODO : to revisit this code once CC-752 is addressed
		//TODO: to revisit once CCR-811 is developed. 
		//as of now counterparty group is not handled.		

		//TODO: to think of counterpartyGroup in limit maintenance.UI doesnot support this for now.
		FilterData limitFilterData = new FilterData();
		limitFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations mongoOperation = new MongoOperations();
		mongoOperation.setOperator("in");
		mongoOperation.setFieldName("counterpartyGroupNameDisplayName");
		mongoOperation.setValue(array);
		limitFilterData.getFilter().add(mongoOperation);
		MongoOperations mongoOperation1 = new MongoOperations();
		mongoOperation1.setOperator("eq");
		mongoOperation1.setFieldName("limitStatusDisplayName");
		mongoOperation1.setValue("Active");
		limitFilterData.getFilter().add(mongoOperation1);
		Map<String, Object> limitQueryMap = new HashMap<>();
		limitQueryMap.put("limitStatusDisplayName", "Active");
		List<LimitMaintenanceDetails> limitMaintenanceDetails = null;
		
		CompletableFuture<List<LimitMaintenanceDetails>> limitMaintenanceDetailsFuturesObj = service
				.getLimitMaintenanceDetailsAsync(
						restTemplateUtil.getCommonHttpHeaders(),limitFilterData);

		List<CounterpartyExposure> counterPartyExposures = null;
		CompletableFuture<List<CounterpartyExposure>> counterpartyExposuresFuturesObj = service
				.prepareFilterAndGetCounterpartyExposuresAsync( restTemplateUtil
						.getCommonHttpHeaders(),
						commaSeparatedCounterparties, requestContextHolder
								.getCurrentContext().getAppProperties());

		List<DerivedPaymentTerms> derivedPaymentTerms = null;
		CompletableFuture<List<DerivedPaymentTerms>> derivedPaymentTermsFuturesObj = service
				.getDerivedPaymentTermsAsync(restTemplateUtil
						.getCommonHttpHeaders(),null, requestContextHolder
						.getCurrentContext().getAppProperties());

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
		counterPartyExposures = counterpartyExposuresFuturesObj.get();
		derivedPaymentTerms = derivedPaymentTermsFuturesObj.get();
		fxRatesList = fxRateListFuturesObj.get();

		for (CounterPartyDetails cpDetails : counterPartyDetails) {
			if (uniqueCounterParties.containsKey(cpDetails
					.getCounterpartyName())) {
				uniqueCounterParties.get(cpDetails.getCounterpartyName())
						.forEach(t -> {
							t.setCounterpartyDetails(cpDetails);
						});
			}
		}

		if (!CollectionUtils.isEmpty(limitMaintenanceDetails)) {

			Map<String, Object> map = new HashMap<>();
			map.put(CreditRiskConstants.CONTRACT_FULL_TERM, null);
			map.put(CreditRiskConstants.TEMPORARY, null);
			map.put(CreditRiskConstants.CREDIT_LIMIT, null);

			map.put(CreditRiskConstants.OWN_LIMIT, null);
			map.put(CreditRiskConstants.TOP_UP_CONTRACT_FULL_TERM, null);
			map.put(CreditRiskConstants.TOP_UP_CREDIT_LIMIT, null);

			//TODO:CCR-811 has effect here.
			limitMaintenanceDetails = limitMaintenanceDetails
					.stream()
					.filter(l -> (uniqueCounterParties.containsKey(l
							.getCounterpartyGroupNameDisplayName()) || uniqueCounterPartyGroups
							.containsKey(l
									.getCounterpartyGroupNameDisplayName()))
							&& map.containsKey(l
									.getCreditLimitTypeDisplayName()))
					.collect(Collectors.toList());
		}

		Map<FxRatesKey, BigDecimal> fxRatesMap = prepareFxRatesMap(fxRatesList);
		Map<String, String> map = new HashMap<>();
		if (derivedPaymentTerms != null && !derivedPaymentTerms.isEmpty()) {
			derivedPaymentTerms.forEach(d -> {
				map.put(d.getPaymentTerm(), d.getDerivedPaymentTerm());
			});
		}

		List<MovementsCreditCheckCalculator> calculatorList = groupPayLoadForParallelProcessing(
				uniqueCounterParties, uniqueCounterPartyGroups,
				limitMaintenanceDetails, counterPartyExposures, map, fxRatesMap,ccRequest.getEventName());

		List<ItemResponse> itemResponseList = executeParallelly(calculatorList);

		ItemResponse ir1 = mergeIntoSingleResponse(itemResponseList);
		// async call to save request response.
		service.saveCreditCheckRequestResponseAsync(restTemplateUtil.getCommonHttpHeaders(),
				ccRequest, ir1);

		return ir1;

	}

	protected ItemResponse checkIfNoSalesContractItemsFound(
			CreditCheckRequest ccRequest) {
		if (ccRequest.getTccrDetails().isEmpty()) {

			ItemResponse ir = new ItemResponse();
			ir.setStatus(CreditRiskConstants.SUCCESS);
			ir.setDescription("Credit Check is successful as there are no Sales Contract Items");
			return ir;
		}

		return null;
	}

	private List<ItemResponse> executeParallelly(
			List<MovementsCreditCheckCalculator> calculatorList) {

		ExecutorService executor = Executors.newFixedThreadPool(calculatorList
				.size());
		try {
			List<ItemResponse> responseList = new ArrayList<>();
			List<Future<ItemResponse>> futureList = new ArrayList<Future<ItemResponse>>();
			for (MovementsCreditCheckCalculator calculator : calculatorList) {

				Future<ItemResponse> futureObj = executor
						.submit(new MovementsCreditCheckCallable(calculator));
				futureList.add(futureObj);
			}

			for (Future<ItemResponse> fut : futureList) {
				try {
					ItemResponse itemResponse = fut.get();
					responseList.add(itemResponse);
				} catch (InterruptedException | ExecutionException e) {

					LOGGER.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error occured while executing credit check......."),e);
 
					throw new RuntimeException(e);
				}
			}

			return responseList;
		} finally {
			// shut down the executor service now
			executor.shutdown();
		}
	}

	private List<MovementsCreditCheckCalculator> groupPayLoadForParallelProcessing(
			Map<String, List<TCCRDetails>> uniqueCounterParties,
			Map<String, Set<String>> uniqueCounterPartyGroups,
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<CounterpartyExposure> counterPartyExposures,
			Map<String, String> map, Map<FxRatesKey, BigDecimal> fxRatesMap,String eventName) {

		List<MovementsCreditCheckCalculator> calculatorList = new ArrayList<>();

		if (uniqueCounterPartyGroups.size() != 0) {

			Iterator<Entry<String, Set<String>>> iterator = uniqueCounterPartyGroups
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Set<String>> next = iterator.next();
				Set<String> counterPartyNameList = next.getValue();
				List<LimitMaintenanceDetails> limitList = limitMaintenanceDetails
						.stream()
						.filter(l -> (next.getKey().equalsIgnoreCase(
								l.getCounterpartyGroupNameDisplayName()) || counterPartyNameList
								.contains(l
										.getCounterpartyGroupNameDisplayName())))
						.collect(Collectors.toList());

				List<CounterpartyExposure> exposureList = counterPartyExposures
						.stream()
						.filter(e -> (counterPartyNameList.contains(e
								.getCounterparty())))
						.collect(Collectors.toList());

				for (String counterparty : counterPartyNameList) {
					List<TCCRDetails> list = uniqueCounterParties
							.get(counterparty);
					MovementsCreditCheckCalculator movementsCalculator = new MovementsCreditCheckCalculator(
							limitList, list);
					movementsCalculator.setCounterPartyExposure(exposureList);
					movementsCalculator.setDerivedPaymentTerms(map);
					movementsCalculator.setFxRatesMap(fxRatesMap);
					movementsCalculator.setCounterpartyGroup(next.getKey());
					movementsCalculator.setEventName(eventName);
					movementsCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
					calculatorList.add(movementsCalculator);
					uniqueCounterParties.remove(counterparty);
				}
			}
		}

		if (uniqueCounterParties.size() != 0) {
			List<String> removeList = new ArrayList<>();
			uniqueCounterParties
					.forEach((k, v) -> {
						List<LimitMaintenanceDetails> limitList = limitMaintenanceDetails
								.stream()
								.filter(l -> (k.contains(l
										.getCounterpartyGroupNameDisplayName())))
								.collect(Collectors.toList());

						List<CounterpartyExposure> exposureList = counterPartyExposures
								.stream()
								.filter(e -> (e.getCounterparty()
										.equalsIgnoreCase(k)))
								.collect(Collectors.toList());
						List<TCCRDetails> list = v;
						MovementsCreditCheckCalculator movementsCalculator = new MovementsCreditCheckCalculator(
								limitList, list);
						movementsCalculator
								.setCounterPartyExposure(exposureList);
						movementsCalculator.setDerivedPaymentTerms(map);
						movementsCalculator.setFxRatesMap(fxRatesMap);
						movementsCalculator.setEventName(eventName);
						movementsCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
						calculatorList.add(movementsCalculator);
						removeList.add(k);

					});
			if (!removeList.isEmpty())
				for (String k : removeList) {

					uniqueCounterParties.remove(k);
				}
		}

		return calculatorList;

	}

	public CreditCheckRequest populateCreditCheckRequestObject(
			MovementsCreditCheckRequest movementsCreditCheckRequest) {
		CreditCheckRequest ccr = new CreditCheckRequest();
		ccr.setEntityType(CreditRiskConstants.MOVEMENTS);
		ccr.setEntityRefNo(movementsCreditCheckRequest.getEntityRefNo());

		Map<String,TCCRDetails> map = new HashMap<>();
		
		List<TCCRDetails> tccrDetailsList = new ArrayList<>();
		ccr.setTccrDetails(tccrDetailsList);
		ccr.setEventName(movementsCreditCheckRequest.getEventName());
		for (MovementContractDetails contractDetails : movementsCreditCheckRequest
				.getContractDetails()) {

			if ("Purchase".equalsIgnoreCase(contractDetails.getContractType())) {
				continue;

			}

			TCCRDetails tccrDetails = null;
			List<Item> itemsList = null;

			if (map.containsKey(contractDetails.getCounterParty())) {
				tccrDetails = map.get(contractDetails.getCounterParty());
				itemsList = tccrDetails.getItems();
			} else {
				tccrDetails = new TCCRDetails();
				tccrDetailsList.add(tccrDetails);
				itemsList = new ArrayList<>();
				tccrDetails.setItems(itemsList);
				map.put(contractDetails.getCounterParty(), tccrDetails);
			}
			
			tccrDetails.setContractRefNo(contractDetails.getContractRefNo());
			tccrDetails.setCounterParty(contractDetails.getCounterParty());
			tccrDetails.setCounterPartyGroup(contractDetails
					.getCounterPartyGroup());

			tccrDetails.setPaymentTerm(contractDetails.getPaymentTerm());

			Item item = new Item();
			if (!(StringUtils.isEmpty(contractDetails.getLimitRefNo()) || contractDetails
					.getLimitRefNo().trim().length() == 0)) {
				item.setLimitRefNo(contractDetails.getLimitRefNo().trim());
			}
			item.setPayInCurrency(contractDetails.getPayInCurrency());
			item.setValue(contractDetails.getValue());
			item.setValueInCounterPartyCurrency(contractDetails.getValue());
			item.setContractItemRefNo(contractDetails.getContractItemRefNo());
			item.setFromPeriod(contractDetails.getFromPeriod());
			item.setToPeriod(contractDetails.getToPeriod());
			item.setTccrDetails(tccrDetails);
			itemsList.add(item);

		}

		return ccr;

	}

	public List<LimitMaintenanceDetails> filterLimitDataBylimitType(
			List<LimitMaintenanceDetails> limitMaintenanceDetails) {

		if (CollectionUtils.isEmpty(limitMaintenanceDetails)) {
			return limitMaintenanceDetails;
		}
		Map<String, Object> map = new HashMap<>();
		map.put(CreditRiskConstants.CONTRACT_FULL_TERM, null);
		map.put(CreditRiskConstants.TEMPORARY, null);
		map.put(CreditRiskConstants.CREDIT_LIMIT, null);

		map.put(CreditRiskConstants.OWN_LIMIT, null);
		map.put(CreditRiskConstants.TOP_UP_CONTRACT_FULL_TERM, null);
		map.put(CreditRiskConstants.TOP_UP_CREDIT_LIMIT, null);

		limitMaintenanceDetails = limitMaintenanceDetails
				.stream()
				.filter(e -> map.containsKey(e.getCreditLimitTypeDisplayName()))
				.collect(Collectors.toList());
		return limitMaintenanceDetails;
	}

	@Async
	public CompletableFuture<List<CounterpartyExposure>> prepareFilterAndGetCounterpartyExposuresAsync(
			 HttpHeaders httpHeaders, String commaSeparatedCounterparties,
			Map<String, Object> appProperties) {
		JSONObject filterObject = prepareFilterObjectForExposures(commaSeparatedCounterparties);
		List<CounterpartyExposure> counterPartyExposures = platformDataService
				.getCounterPartyExposures(filterObject, httpHeaders,
						appProperties);
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
		return CompletableFuture.completedFuture(counterPartyExposures);
	}

	protected JSONObject prepareFilterObjectForExposures(String counterparties) {

		JSONObject filterObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject counterpartyInObj = new JSONObject();
		counterpartyInObj.put(PlatformConstants.FIELD_NAME, "counterparty");
		counterpartyInObj.put(PlatformConstants.VALUE, counterparties);
		counterpartyInObj.put(PlatformConstants.OPERATOR, "in");
		jsonArray.put(counterpartyInObj);

		JSONObject exposureTypesInObj = new JSONObject();
		exposureTypesInObj.put(PlatformConstants.FIELD_NAME, "exposureType");
		exposureTypesInObj.put(PlatformConstants.VALUE, String.join(",",
				CreditRiskConstants.EXPOSURE_TYPES_MO));
		exposureTypesInObj.put(PlatformConstants.OPERATOR, "in");
		jsonArray.put(exposureTypesInObj);

		filterObject.put(PlatformConstants.FILTER, jsonArray);

		return filterObject;
	}

	@Async
	public CompletableFuture<List<DerivedPaymentTerms>> getDerivedPaymentTermsAsync(
			 HttpHeaders httpHeaders, JSONObject filterObject,
			Map<String, Object> appProperties) {
		List<DerivedPaymentTerms> derivedPaymentTerms = platformDataService
				.getDerivedPaymentTerms(filterObject, httpHeaders,
						appProperties);

		return CompletableFuture.completedFuture(derivedPaymentTerms);
	}

}
