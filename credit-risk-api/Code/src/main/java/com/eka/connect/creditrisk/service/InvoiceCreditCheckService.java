package com.eka.connect.creditrisk.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.eka.connect.creditrisk.SpringBeanContext;
import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.PlatformConstants;
import com.eka.connect.creditrisk.dataobject.ContractDetails;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CounterpartyExposure;
import com.eka.connect.creditrisk.dataobject.CreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.DerivedPaymentTerms;
import com.eka.connect.creditrisk.dataobject.FilterData;
import com.eka.connect.creditrisk.dataobject.FxRates;
import com.eka.connect.creditrisk.dataobject.FxRatesKey;
import com.eka.connect.creditrisk.dataobject.InvoiceCreditCheckRequest;
import com.eka.connect.creditrisk.dataobject.Item;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.TCCRDetails;
import com.eka.connect.creditrisk.util.InvoiceCreditCheckCalculator;
import com.eka.connect.creditrisk.util.RestTemplateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InvoiceCreditCheckService extends CreditCheckService {

	private static final Logger LOGGER = ESAPI
			.getLogger(InvoiceCreditCheckService.class);

	@Autowired
	private PlatformDataService platformDataService;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	public ItemResponse doCreditCheck(
			InvoiceCreditCheckRequest invoiceCreditCheckRequest)
			throws Exception {

		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"initiating credit check for contract entity........."));

		CounterPartyDetails c = null;
		// Get counter party data.
	 FilterData counterpartyFilterData = this
				.getCounterpartyFilterData(invoiceCreditCheckRequest.getInvoiceCounterParty());
		
		List<CounterPartyDetails> counterPartyDetails = this
				.getCounterPartyDetails(counterpartyFilterData,
						restTemplateUtil.getCommonHttpHeaders());

		if (counterPartyDetails != null && (!counterPartyDetails.isEmpty())) {
			c = counterPartyDetails.get(0);
		}
		CreditCheckRequest ccRequest = populateCreditCheckRequestObject(
				invoiceCreditCheckRequest, c);
		
		// get limit maintenance data
		FilterData limitFilterData = this
				.getLimitMaintenanceFilterData(invoiceCreditCheckRequest.getInvoiceCounterParty());
		
		List<LimitMaintenanceDetails> limitMaintenanceDetails = getLimitMaintenanceDetails(
				limitFilterData, restTemplateUtil.getCommonHttpHeaders());

		limitMaintenanceDetails = this
				.filterLimitDataBylimitType(limitMaintenanceDetails);

		JSONObject filterObject = prepareFilterObjectForExposures(invoiceCreditCheckRequest
				.getInvoiceCounterParty());
		List<CounterpartyExposure> counterPartyExposures = platformDataService
				.getCounterPartyExposures(filterObject, restTemplateUtil
						.getCommonHttpHeaders(), requestContextHolder
						.getCurrentContext().getAppProperties());

		counterPartyExposures = furtherFilterExposureData(
				counterPartyExposures, invoiceCreditCheckRequest);

		List<DerivedPaymentTerms> derivedPaymentTerms = platformDataService
				.getDerivedPaymentTerms(null, restTemplateUtil
						.getCommonHttpHeaders(), requestContextHolder
						.getCurrentContext().getAppProperties());

		List<FxRates> fxRatesList = getFxRates(
				restTemplateUtil.getCommonHttpHeaders(), requestContextHolder
						.getCurrentContext().getAppProperties());
		Map<FxRatesKey, BigDecimal> fxRatesMap = prepareFxRatesMap(fxRatesList);
		Map<String, String> map = new HashMap<>();
		if (derivedPaymentTerms != null && !derivedPaymentTerms.isEmpty()) {
			derivedPaymentTerms.forEach(d -> {
				map.put(d.getPaymentTerm(), d.getDerivedPaymentTerm());
			});
		}

		InvoiceCreditCheckCalculator invoiceCalculator = new InvoiceCreditCheckCalculator(
				c, limitMaintenanceDetails, ccRequest.getTccrDetails());
		invoiceCalculator.setContractType(invoiceCreditCheckRequest
				.getContractType());
		invoiceCalculator.setLimitMaintenanceDetails(limitMaintenanceDetails);
		invoiceCalculator.setCounterPartyExposure(counterPartyExposures);
		invoiceCalculator.setDerivedPaymentTerms(map);
		invoiceCalculator.setFxRatesMap(fxRatesMap);
		invoiceCalculator.setPaymentTerm(invoiceCreditCheckRequest
				.getPaymentTerm());
		invoiceCalculator.setCurrenyLocale(this.getUtilityService().getCurrencyLocale());
		ItemResponse itemResponse = invoiceCalculator.calculate();

		// async call to save request response.
		InvoiceCreditCheckService service = SpringBeanContext
				.getBean(InvoiceCreditCheckService.class);
		service.saveCreditCheckRequestResponseAsync(restTemplateUtil.getCommonHttpHeaders(),ccRequest, itemResponse);
		return itemResponse;

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

	private List<CounterpartyExposure> furtherFilterExposureData(
			List<CounterpartyExposure> counterPartyExposures,
			InvoiceCreditCheckRequest invoiceRequest) {

		List<String> contractRefNoList = new ArrayList<>();
		List<String> contractItemRefNoList = new ArrayList<>();
		List<String> gmrRefNoList = new ArrayList<>();
		invoiceRequest.getContractDetails().forEach(e -> {
			contractRefNoList.add(e.getContractRefNo());
			contractItemRefNoList.add(e.getContractItemRefNo());
		});

		if (CollectionUtils.isEmpty(counterPartyExposures)) {
			return counterPartyExposures;
		}

		counterPartyExposures = counterPartyExposures
				.stream()
				.filter(e -> !(CreditRiskConstants.EXPOSURE_TYPE_ACTIVE_SALES_CONTRACTS_NOT_PLANNED
						.equalsIgnoreCase(e.getExposureType()) || (CreditRiskConstants.EXPOSURE_TYPE_SHIPPED_BUT_NOT_INVOICED
						.equalsIgnoreCase(e.getExposureType())
						&& contractItemRefNoList.contains(e
								.getContractItemRefNo())
						&& contractRefNoList.contains(e.getContractRefNo()) && gmrRefNoList
						.contains(e.getGmrRefNo()))))
				.collect(Collectors.toList());
		
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

	private JSONObject prepareFilterObjectForExposures(String counterparty) {

		JSONObject filterObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject inObj1 = new JSONObject();
		inObj1.put(PlatformConstants.FIELD_NAME, "counterparty");
		inObj1.put(PlatformConstants.VALUE, counterparty);
		inObj1.put(PlatformConstants.OPERATOR, "eq");
		jsonArray.put(inObj1);

		JSONObject exposureTypes = new JSONObject();
		exposureTypes.put(PlatformConstants.FIELD_NAME, "exposureType");
		exposureTypes.put(
				PlatformConstants.VALUE,
				String.join(",",CreditRiskConstants.EXPOSURE_TYPES_SALES_FINAL_INVOICE));
		exposureTypes.put(PlatformConstants.OPERATOR, "in");
		jsonArray.put(exposureTypes);
		
		filterObject.put(PlatformConstants.FILTER, jsonArray);

		return filterObject;
	}

	private CreditCheckRequest populateCreditCheckRequestObject(
			InvoiceCreditCheckRequest invoiceCreditCheckRequest,
			CounterPartyDetails c) {
		CreditCheckRequest ccr = new CreditCheckRequest();
		ccr.setEntityType(CreditRiskConstants.INVOICE);
		ccr.setEventName(invoiceCreditCheckRequest.getEventName());
		List<TCCRDetails> tccrDetailsList = new ArrayList<>();
		ccr.setTccrDetails(tccrDetailsList);

		for (ContractDetails contractDetails : invoiceCreditCheckRequest
				.getContractDetails()) {

			TCCRDetails tccrDetails = new TCCRDetails();
			tccrDetailsList.add(tccrDetails);
			tccrDetails.setCounterpartyDetails(c);
			tccrDetails.setContractRefNo(contractDetails.getContractRefNo());
			tccrDetails.setCounterParty(invoiceCreditCheckRequest
					.getInvoiceCounterParty());
			tccrDetails.setCounterPartyGroup(invoiceCreditCheckRequest
					.getCounterPartyGroup());
			tccrDetails.setGmrRefNo(contractDetails.getGmrRefNo());

			tccrDetails.setPaymentTerm(invoiceCreditCheckRequest
					.getPaymentTerm());

			List<Item> itemsList = new ArrayList<>();
			tccrDetails.setItems(itemsList);
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

}
