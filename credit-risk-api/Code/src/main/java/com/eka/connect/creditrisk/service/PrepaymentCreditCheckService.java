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
import com.eka.connect.creditrisk.dataobject.PrepaymentInvoiceCreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.PrepaymetContractDetails;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.util.PrepaymentInvoiceCreditCheckCalculator;
import com.eka.connect.creditrisk.util.RestTemplateUtil;

@Service
public class PrepaymentCreditCheckService extends CreditCheckService {

	private static final Logger LOGGER = ESAPI
			.getLogger(PrepaymentCreditCheckService.class);

	@Autowired
	private PlatformDataService platformDataService;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	public ItemResponse doCreditCheck(
			PrepaymentInvoiceCreditCheckRequest ppCreditCheckRequest)
			throws Exception {

		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("initiating credit check for prepayment invoice entity........."));

		PrepaymentCreditCheckService service = SpringBeanContext
				.getBean(PrepaymentCreditCheckService.class);

		CounterPartyDetails c = null;
		// Get counter party data.
		FilterData counterpartyFilterData = this
				.getCounterpartyFilterData(ppCreditCheckRequest.getInvoiceCounterParty());

		List<CounterPartyDetails> counterPartyDetails = null;

		CompletableFuture<List<CounterPartyDetails>> counterPartyDetailsFuturesObj = service
				.getCounterPartyDetailsAsync(restTemplateUtil.getCommonHttpHeaders(),counterpartyFilterData);

		// get limit maintenance data
		FilterData limitFilterData = this
				.getLimitMaintenanceFilterData(ppCreditCheckRequest.getInvoiceCounterParty());
		
		List<LimitMaintenanceDetails> limitMaintenanceDetails = null;

		CompletableFuture<List<LimitMaintenanceDetails>> limitMaintenanceDetailsFuturesObj = service
				.getLimitMaintenanceDetailsAsync(restTemplateUtil.getCommonHttpHeaders(),limitFilterData);

		List<CounterpartyExposure> counterPartyExposures = null;

		CompletableFuture<List<CounterpartyExposure>> counterpartyExposuresFuturesObj = service
				.getCounterPartyExposuresAsync(
						restTemplateUtil.getCommonHttpHeaders(),ppCreditCheckRequest,
						requestContextHolder.getCurrentContext()
								.getAppProperties());

		List<DerivedPaymentTerms> derivedPaymentTerms = null;
		CompletableFuture<List<DerivedPaymentTerms>> derivedPaymentTermsFuturesObj = service
				.getDerivedPaymentTermsAsync(
						restTemplateUtil.getCommonHttpHeaders(),ppCreditCheckRequest,
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
		counterPartyExposures = counterpartyExposuresFuturesObj.get();
		derivedPaymentTerms = derivedPaymentTermsFuturesObj.get();
		fxRatesList = fxRateListFuturesObj.get();

		if (counterPartyDetails != null && (!counterPartyDetails.isEmpty())) {
			c = counterPartyDetails.get(0);

		}

		limitMaintenanceDetails = this
				.filterLimitDataBylimitType(limitMaintenanceDetails);

		CreditCheckRequest ccRequest = populateCreditCheckRequestObject(
				ppCreditCheckRequest, c);

		Map<FxRatesKey, BigDecimal> fxRatesMap = prepareFxRatesMap(fxRatesList);
		Map<String, String> map = new HashMap<>();
		if (derivedPaymentTerms != null && !derivedPaymentTerms.isEmpty()) {
			derivedPaymentTerms.forEach(d -> {
				map.put(d.getPaymentTerm(), d.getDerivedPaymentTerm());
			});
		}

		PrepaymentInvoiceCreditCheckCalculator ppiCalculator = new PrepaymentInvoiceCreditCheckCalculator(
				c, limitMaintenanceDetails, ccRequest.getTccrDetails());
		ppiCalculator.setContractType(ppCreditCheckRequest.getContractType());
		ppiCalculator.setLimitMaintenanceDetails(limitMaintenanceDetails);
		ppiCalculator.setCounterPartyExposure(counterPartyExposures);
		ppiCalculator.setDerivedPaymentTerms(map);
		ppiCalculator.setFxRatesMap(fxRatesMap);
		ppiCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
		ItemResponse itemResponse = ppiCalculator.calculate();

		service.saveCreditCheckRequestResponseAsync(
				restTemplateUtil.getCommonHttpHeaders(), ccRequest, itemResponse);
		return itemResponse;

	}

	@Async
	public CompletableFuture<List<CounterpartyExposure>> getCounterPartyExposuresAsync(
			HttpHeaders commonHttpHeaders,PrepaymentInvoiceCreditCheckRequest ppCreditCheckRequest,
			Map<String, Object> appProperties) {

		JSONObject filterObject = getExposureFilterObject(ppCreditCheckRequest);
		List<CounterpartyExposure> counterPartyExposures = platformDataService
				.getCounterPartyExposures(filterObject, commonHttpHeaders,
						appProperties);
		return CompletableFuture.completedFuture(counterPartyExposures);

	}

	private JSONObject getExposureFilterObject(
			PrepaymentInvoiceCreditCheckRequest ppCreditCheckRequest) {
		JSONObject filterObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		JSONObject inObj = new JSONObject();
		inObj.put(PlatformConstants.FIELD_NAME, "exposureType");
		inObj.put(PlatformConstants.VALUE, String.join(",",
				CreditRiskConstants.PREPAYMENT_INVOICE_EXPOSURE_TYPES));
		inObj.put(PlatformConstants.OPERATOR, "in");
		jsonArray.put(inObj);

		JSONObject eqobj = new JSONObject();
		eqobj.put(PlatformConstants.FIELD_NAME, "counterparty");
		eqobj.put(PlatformConstants.VALUE,
				ppCreditCheckRequest.getInvoiceCounterParty());
		eqobj.put(PlatformConstants.OPERATOR, "eq");
		jsonArray.put(eqobj);
		filterObject.put(PlatformConstants.FILTER, jsonArray);
		return filterObject;
	}

	public List<LimitMaintenanceDetails> filterLimitDataBylimitType(
			List<LimitMaintenanceDetails> limitMaintenanceDetails) {

		if (CollectionUtils.isEmpty(limitMaintenanceDetails)) {
			return limitMaintenanceDetails;
		}
		Map<String, Object> map = new HashMap<>();
		map.put(CreditRiskConstants.PRE_PAYMENT_FULL_TERM, null);
		map.put(CreditRiskConstants.PRE_PAYMNET_CREDIT_LIMIT, null);
		map.put(CreditRiskConstants.PRE_PAYMENT_LIMIT, null);

		limitMaintenanceDetails = limitMaintenanceDetails
				.stream()
				.filter(e -> map.containsKey(e.getCreditLimitTypeDisplayName()))
				.collect(Collectors.toList());
		return limitMaintenanceDetails;
	}

	private CreditCheckRequest populateCreditCheckRequestObject(
			PrepaymentInvoiceCreditCheckRequest ppCreditCheckRequest,
			CounterPartyDetails c) {
		CreditCheckRequest ccr = new CreditCheckRequest();
		ccr.setEntityType(CreditRiskConstants.PP_INVOICE);
		ccr.setEventName(ppCreditCheckRequest.getEventName());
		List<TCCRDetails> tccrDetailsList = new ArrayList<>();
		ccr.setTccrDetails(tccrDetailsList);

		for (PrepaymetContractDetails contractDetails : ppCreditCheckRequest
				.getContractDetails()) {

			TCCRDetails tccrDetails = new TCCRDetails();
			tccrDetailsList.add(tccrDetails);
			tccrDetails.setCounterpartyDetails(c);
			tccrDetails.setContractRefNo(contractDetails.getContractRefNo());
			tccrDetails.setCounterParty(ppCreditCheckRequest
					.getInvoiceCounterParty());
			tccrDetails.setCounterPartyGroup(ppCreditCheckRequest
					.getCounterPartyGroup());

			tccrDetails.setPaymentTerm(ppCreditCheckRequest.getPaymentTerm());

			List<Item> itemsList = new ArrayList<>();
			tccrDetails.setItems(itemsList);
			Item item = new Item();
			if (!(StringUtils.isEmpty(contractDetails.getLimitRefNo()) || contractDetails
					.getLimitRefNo().trim().length() == 0)) {
				item.setLimitRefNo(contractDetails.getLimitRefNo()
						.trim());
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

	@Async
	public CompletableFuture<List<DerivedPaymentTerms>> getDerivedPaymentTermsAsync(
			HttpHeaders httpHeaders,PrepaymentInvoiceCreditCheckRequest request,
			 Map<String, Object> appProperties) throws Exception {

		List<DerivedPaymentTerms> derivedPaymentTerms = this
				.getDerivedPaymentTerms(request, httpHeaders, appProperties);
		return CompletableFuture.completedFuture(derivedPaymentTerms);
	}

	private List<DerivedPaymentTerms> getDerivedPaymentTerms(
			PrepaymentInvoiceCreditCheckRequest request,
			HttpHeaders httpHeaders, Map<String, Object> appProperties)
			throws Exception {
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

}
