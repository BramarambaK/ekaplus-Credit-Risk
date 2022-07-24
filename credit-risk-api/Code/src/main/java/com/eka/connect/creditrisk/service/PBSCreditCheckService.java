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

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.eka.connect.creditrisk.SpringBeanContext;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
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
import com.eka.connect.creditrisk.dataobject.PBSCreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.executor.PBSCreditCheckCallable;
import com.eka.connect.creditrisk.util.PBSCreditCheckCalculator;
import com.eka.connect.creditrisk.util.RestTemplateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("pbsCreditCheckService")
public class PBSCreditCheckService extends MovementsCreditCheckService {

	private static final Logger LOGGER = ESAPI
			.getLogger(PBSCreditCheckService.class);

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	public ItemResponse doCreditCheck(
			PBSCreditCheckRequest pbsCreditCheckRequest) throws Exception {
		CreditCheckRequest ccRequest = populateCreditCheckRequestObject(pbsCreditCheckRequest);

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
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("counterpartyName", String.join(",", array));

		PBSCreditCheckService service = SpringBeanContext
				.getBean(PBSCreditCheckService.class);
 		List<CounterPartyDetails> counterPartyDetails = null;
 		
 		FilterData counterPartyFilterData = new FilterData();
		counterPartyFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations operation = new MongoOperations();
		operation.setOperator("in");
		operation.setFieldName("counterpartyName");
		operation.setValue(array);
		counterPartyFilterData.getFilter().add(operation);

		CompletableFuture<List<CounterPartyDetails>> counterPartyDetailsFuturesObj = service
				.getCounterPartyDetailsAsync(restTemplateUtil.getCommonHttpHeaders(),
						counterPartyFilterData);

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
		
		List<LimitMaintenanceDetails> limitMaintenanceDetails = null;
		CompletableFuture<List<LimitMaintenanceDetails>> limitMaintenanceDetailsFuturesObj = service
				.getLimitMaintenanceDetailsAsync(restTemplateUtil.getCommonHttpHeaders(),limitFilterData);

		List<CounterpartyExposure> counterPartyExposures = null;
		CompletableFuture<List<CounterpartyExposure>> counterpartyExposuresFuturesObj = service
				.prepareFilterAndGetCounterpartyExposuresAsync(restTemplateUtil
						.getCommonHttpHeaders(),
						commaSeparatedCounterparties, requestContextHolder
								.getCurrentContext().getAppProperties());

		List<DerivedPaymentTerms> derivedPaymentTerms = null;
		CompletableFuture<List<DerivedPaymentTerms>> derivedPaymentTermsFuturesObj = service
				.getDerivedPaymentTermsAsync( restTemplateUtil
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

		List<PBSCreditCheckCalculator> calculatorList = groupPayLoadForParallelProcessing(
				uniqueCounterParties, uniqueCounterPartyGroups,
				limitMaintenanceDetails, counterPartyExposures, map, fxRatesMap,ccRequest.getEventName());

		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Calculation started.........."));
	
		List<ItemResponse> itemResponseList = executeParallelly(calculatorList);

		ItemResponse ir1 = mergeIntoSingleResponse(itemResponseList);

		// async call to save request response.
		service.saveCreditCheckRequestResponseAsync(
				restTemplateUtil.getCommonHttpHeaders(), ccRequest, ir1);
		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Calculation completed successfully.........."));
	
		return ir1;

	}

	private List<ItemResponse> executeParallelly(
			List<PBSCreditCheckCalculator> calculatorList) {

		ExecutorService executor = Executors.newFixedThreadPool(calculatorList
				.size());
		try {
			List<ItemResponse> responseList = new ArrayList<>();
			List<Future<ItemResponse>> futureList = new ArrayList<Future<ItemResponse>>();
			for (PBSCreditCheckCalculator calculator : calculatorList) {

				Future<ItemResponse> futureObj = executor
						.submit(new PBSCreditCheckCallable(calculator));
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
									"Error occured while executing credit check.......")
									,e);
				
					 
					throw new RuntimeException(e);
				}
			}

			return responseList;
		} finally {
			// shut down the executor service now
			executor.shutdown();
		}
	}

	private List<PBSCreditCheckCalculator> groupPayLoadForParallelProcessing(
			Map<String, List<TCCRDetails>> uniqueCounterParties,
			Map<String, Set<String>> uniqueCounterPartyGroups,
			List<LimitMaintenanceDetails> limitMaintenanceDetails,
			List<CounterpartyExposure> counterPartyExposures,
			Map<String, String> map, Map<FxRatesKey, BigDecimal> fxRatesMap,String eventName) {

		List<PBSCreditCheckCalculator> calculatorList = new ArrayList<>();

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
					PBSCreditCheckCalculator pbsCalculator = new PBSCreditCheckCalculator(
							limitList, list);
					pbsCalculator.setEventName(eventName);
					pbsCalculator.setCounterPartyExposure(exposureList);
					pbsCalculator.setDerivedPaymentTerms(map);
					pbsCalculator.setFxRatesMap(fxRatesMap);
					pbsCalculator.setCounterpartyGroup(next.getKey());
					pbsCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
					calculatorList.add(pbsCalculator);
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
						PBSCreditCheckCalculator pbsCalculator = new PBSCreditCheckCalculator(
								limitList, list);
						pbsCalculator.setCounterPartyExposure(exposureList);
						pbsCalculator.setDerivedPaymentTerms(map);
						pbsCalculator.setFxRatesMap(fxRatesMap);
						pbsCalculator.setEventName(eventName);
						pbsCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
						calculatorList.add(pbsCalculator);
						removeList.add(k);

					});
			if (!removeList.isEmpty())
				for (String k : removeList) {

					uniqueCounterParties.remove(k);
				}
		}

		return calculatorList;

	}

	private CreditCheckRequest populateCreditCheckRequestObject(
			PBSCreditCheckRequest pbsCreditCheckRequest) {
		CreditCheckRequest ccr = new CreditCheckRequest();
		ccr.setEntityType(CreditRiskConstants.PBS);
		ccr.setEntityRefNo(pbsCreditCheckRequest.getEntityRefNo());

		Map<String,TCCRDetails> map = new HashMap<>();
		
		List<TCCRDetails> tccrDetailsList = new ArrayList<>();
		ccr.setTccrDetails(tccrDetailsList);
		ccr.setEventName(pbsCreditCheckRequest.getEventName());
		for (MovementContractDetails contractDetails : pbsCreditCheckRequest
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

}
