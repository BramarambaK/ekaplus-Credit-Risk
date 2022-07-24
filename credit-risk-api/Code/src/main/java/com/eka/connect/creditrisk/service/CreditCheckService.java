package com.eka.connect.creditrisk.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.CreditCheckRequestResponse;
import com.eka.connect.creditrisk.dataobject.FilterData;
import com.eka.connect.creditrisk.dataobject.FxRates;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.MongoOperations;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.dataobject.WorkflowPojo;

/**
 * 
 * @author rajeshks
 *
 */
@Service
public class CreditCheckService {

	@Autowired
	ConnectRestTemplate connectRestTemplate;

	@Autowired
	PlatformDataService platformDataService;

	@Autowired
	private RequestContextHolder requestContextHolder;
	
	@Autowired
	private UtilityService utilityService;
	
	private static final Logger LOGGER = ESAPI
			.getLogger(CreditCheckService.class);

	public List<CounterPartyDetails> getCounterPartyDetails(
			FilterData filterData, HttpHeaders httpHeaders) {

		List<CounterPartyDetails> counterPartyDetails = connectRestTemplate
				.getCounterpartyData(filterData, httpHeaders);
		return counterPartyDetails;
	}

	@Async
	public CompletableFuture<List<CounterPartyDetails>> getCounterPartyDetailsAsync(
			HttpHeaders httpHeaders,FilterData filterData) {
	
		List<CounterPartyDetails> counterPartyDetails = this
				.getCounterPartyDetails(filterData, httpHeaders);
		return CompletableFuture.completedFuture(counterPartyDetails);
	}

	public List<CounterPartyDetails> getAllCounterPartyDetails(
			HttpHeaders httpHeaders) {
		List<CounterPartyDetails> counterPartyDetails = connectRestTemplate
				.getCounterpartyDataByWorkFlow(null, httpHeaders);
		return counterPartyDetails;
	}

	public List<LimitMaintenanceDetails> getLimitMaintenanceDetails(
			FilterData filterData, HttpHeaders httpHeaders) {

		List<LimitMaintenanceDetails> limitMaintenanceDetails = connectRestTemplate
				.getLimitMaintenanceData(filterData, httpHeaders);

		return limitMaintenanceDetails;
	}

	@Async
	public CompletableFuture<List<LimitMaintenanceDetails>> getLimitMaintenanceDetailsAsync(
			HttpHeaders httpHeaders,FilterData filterData) {

		List<LimitMaintenanceDetails> limitMaintenanceDetails = connectRestTemplate
				.getLimitMaintenanceData(filterData, httpHeaders);

		return CompletableFuture.completedFuture(limitMaintenanceDetails);
	}

	protected List<LimitMaintenanceDetails> filterLimitDataBylimitType(
			List<LimitMaintenanceDetails> limitMaintenanceDetails) {

		if (CollectionUtils.isEmpty(limitMaintenanceDetails)) {
			return limitMaintenanceDetails;
		}
		Map<String, Object> map = new HashMap<>();
		map.put(CreditRiskConstants.CONTRACT_FULL_TERM, null);
		map.put(CreditRiskConstants.CONTRACT_PARTIAL_TERM, null);
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
	public CompletableFuture<List<FxRates>> getFxRatesAsync(
			HttpHeaders httpHeaders, Map<String, Object> appProperties) throws Exception {

		List<FxRates> fxRates = this.getFxRates(httpHeaders, appProperties);

		return CompletableFuture.completedFuture(fxRates);
	}

	public List<FxRates> getFxRates(HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws Exception {

		return platformDataService.getFxRates(httpHeaders, appProperties);

	}

	protected Map<FxRatesKey, BigDecimal> prepareFxRatesMap(
			List<FxRates> fxRatesList) {
		Map<FxRatesKey, BigDecimal> fxRatesMap = null;
		if (!CollectionUtils.isEmpty(fxRatesList)) {
			fxRatesMap = new HashMap<>();
			for (FxRates fxRates : fxRatesList) {

				FxRatesKey key = new FxRatesKey(fxRates.getBaseCurrency(),
						fxRates.getForeignCurrency());
				fxRatesMap.put(key, fxRates.getFxRate());
			}

			for (FxRates fxRates : fxRatesList) {
				FxRatesKey reveseKey = new FxRatesKey(
						fxRates.getForeignCurrency(), fxRates.getBaseCurrency());
				if (fxRatesMap.containsKey(reveseKey)) {
					continue;
				}
				fxRatesMap.put(reveseKey, BigDecimal.ONE.divide(
						fxRates.getFxRate(), CreditRiskConstants.SCALE,
						CreditRiskConstants.ROUNDING_MODE));
			}
		}
		return fxRatesMap;
	}

	public ItemResponse mergeIntoSingleResponse(
			List<ItemResponse> itemResponseList) {

		StringBuffer finalMessage = new StringBuffer();

		ItemResponse ir = new ItemResponse();
		ir.setStatus(CreditRiskConstants.SUCCESS);
		ir.setBlockType(CreditRiskConstants.SOFT_BLOCK);
		for (ItemResponse itemResponse : itemResponseList) {

			finalMessage.append(itemResponse.getDescription()).append("\n");
			if (CreditRiskConstants.FAILURE.equalsIgnoreCase(itemResponse
					.getStatus())) {
				ir.setStatus(CreditRiskConstants.FAILURE);

				if (CreditRiskConstants.HARD_BLOCK
						.equalsIgnoreCase(itemResponse.getBlockType())) {
					ir.setBlockType(CreditRiskConstants.HARD_BLOCK);
				}
			}

		}
		ir.setDescription(finalMessage.toString());
		return ir;
	}

	@Async
	public void saveCreditCheckRequestResponseAsync( HttpHeaders httpHeaders, CreditCheckRequest ccr,
			ItemResponse itemResponse) {
		try {

			List<CreditCheckRequestResponse> creditCheckRquestResponseList = populateRequestResponseObj(
					ccr, itemResponse, httpHeaders.getFirst("requestId"));
			WorkflowPojo workflowObj = new WorkflowPojo();
			workflowObj
					.setAppId(CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID);
			workflowObj.setTask("createcreditchecklist");
			workflowObj.setId("");
			workflowObj.setWorkflowTaskName(workflowObj.getTask());
			for (CreditCheckRequestResponse creditCheckRequestResponse : creditCheckRquestResponseList) {

				workflowObj.getOutput().put(workflowObj.getWorkflowTaskName(),
						creditCheckRequestResponse);
				connectRestTemplate
						.saveWorkflowObject(workflowObj, httpHeaders);
			}
		} catch (HttpClientErrorException he) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("Exception occured while saving request/response .."
							+ he.getResponseBodyAsString()));
			 
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("error"),he);
		} catch (Exception e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder()
							.encodeForHTML(
									"Exception occured while saving request/response .."),
					e);

		}

	}

	private List<CreditCheckRequestResponse> populateRequestResponseObj(
			CreditCheckRequest ccr, ItemResponse itemResponse, String requestId) {
		String dateFormat = "yyyy-MM-dd";
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		List<CreditCheckRequestResponse> list = new ArrayList<>();
		for (TCCRDetails tccrDetails : ccr.getTccrDetails()) {
			for (Item item : tccrDetails.getItems()) {
				CreditCheckRequestResponse creditCheckRquestResponse = new CreditCheckRequestResponse();

				creditCheckRquestResponse.setRequestID(requestId);
				creditCheckRquestResponse.setEntityRefNo(ccr.getEntityRefNo());
				creditCheckRquestResponse.setOperationType(tccrDetails
						.getOperationType());
				creditCheckRquestResponse.setEventName(ccr.getEventName());
				creditCheckRquestResponse.setCounterParty(tccrDetails
						.getCounterParty());
				creditCheckRquestResponse.setCounterPartyGroup(tccrDetails
						.getCounterPartyGroup());
				creditCheckRquestResponse.setLimitRefNo(item
						.getLimitRefNo());
				creditCheckRquestResponse
						.setContractType(ccr.getContractType());
				creditCheckRquestResponse.setPaymenTerm(tccrDetails
						.getPaymentTerm());
				creditCheckRquestResponse.setContractRefNo(tccrDetails
						.getContractRefNo());
				creditCheckRquestResponse.setContractItemRefNo(item
						.getContractItemRefNo());
				creditCheckRquestResponse.setValue(item.getValue());
				if (item.getFromPeriod() != null)
					creditCheckRquestResponse.setFromPeriod(format.format(item
							.getFromPeriod()));
				if (item.getToPeriod() != null)
					creditCheckRquestResponse.setToPeriod(format.format(item
							.getToPeriod()));

				creditCheckRquestResponse.setInvoiceCounterParty(null);
				creditCheckRquestResponse
						.setGMRRefNo(tccrDetails.getGmrRefNo());
				creditCheckRquestResponse.setResponseStatus(itemResponse
						.getStatus());
				creditCheckRquestResponse.setResponseBlockType(itemResponse
						.getBlockType());
				creditCheckRquestResponse.setResponseMessage(itemResponse
						.getDescription());
				creditCheckRquestResponse.setRequestBy(null);
				// creditCheckRquestResponse.setRequestedOn(new Date());
				list.add(creditCheckRquestResponse);
			}
		}

		return list;
	}
	
	
	public FilterData getLimitMaintenanceFilterData(String counterpartyName){
		FilterData limitFilterData = new FilterData();
		limitFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations mongoOperation = new MongoOperations();
		mongoOperation.setOperator("eq");
		mongoOperation.setFieldName("counterpartyGroupNameDisplayName");
		mongoOperation.setValue(counterpartyName);
		limitFilterData.getFilter().add(mongoOperation);
		MongoOperations mongoOperation1 = new MongoOperations();
		mongoOperation1.setOperator("eq");
		mongoOperation1.setFieldName("limitStatusDisplayName");
		mongoOperation1.setValue("Active");
		limitFilterData.getFilter().add(mongoOperation1);
		return limitFilterData;
	}
	
	public FilterData getCounterpartyFilterData(String counterpartyName){
		FilterData counterPartyFilterData = new FilterData();
		counterPartyFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations operation = new MongoOperations();
		operation.setOperator("eq");
		operation.setFieldName("counterpartyName");
		operation.setValue(counterpartyName);
		counterPartyFilterData.getFilter().add(operation);
		return counterPartyFilterData;
	}

	public UtilityService getUtilityService() {
		return utilityService;
	}

	public void setUtilityService(UtilityService utilityService) {
		this.utilityService = utilityService;
	}


}



class LimitMaintenanceComparator implements Comparator<LimitMaintenanceDetails> {

	@Override
	public int compare(LimitMaintenanceDetails o1, LimitMaintenanceDetails o2) {
		// TODO Auto-generated method stub
		return LimitMaintenanceComparator.compareInt(o1.getChartingOrder(),
				o2.getChartingOrder());
	}

	public static int compareInt(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

}
