package com.eka.connect.creditrisk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eka.connect.creditrisk.constants.PropertyConstants;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.exception.PlatformException;
import com.eka.connect.creditrisk.util.RestTemplateUtil;

@Service
public class HealthCheckService {

	@Autowired
	private RequestContextHolder requestContextHolder;

	@Autowired
	private PlatformDataService platformDataService;

	@Autowired
	private ConnectRestTemplate connectRestTemplate;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	@Autowired
	private ManifestService manifestService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${eka_connect_host}")
	private String ekaConnectHost;

	private static final Logger LOGGER = ESAPI
			.getLogger(HealthCheckService.class);

	public List<Map<String, Object>> execute() {

		List<Map<String, Object>> responseObject = new ArrayList<>();
		

		platformCollectionsHealthCheck(responseObject);

		connectListingHealthCheck(responseObject);
		
		creditRiskVersionDetails(responseObject);
		
		connectApiHealthCheck(responseObject);
		
		
		return responseObject;
	}

	private void creditRiskVersionDetails(
			List<Map<String, Object>> responseObject) {

		Map<String, Object> creditCheckVersionInfo = new HashMap<>();
		creditCheckVersionInfo.put("targetType", "Credit Risk API Details");
		creditCheckVersionInfo.put("target", "Credit Risk API Version");
		Attributes manifestAttributes = null;
		try {
			manifestAttributes = manifestService.getManifestAttributes();
			creditCheckVersionInfo.put("version",
					manifestAttributes.getValue("Version"));
			creditCheckVersionInfo.put("status", "Success");

			responseObject.add(creditCheckVersionInfo);

			Map<String, Object> envDetails = new HashMap<>();
			envDetails.put("targetType", "Credit Risk API Details");
			envDetails.put("target", "Credit Risk API Version");
			envDetails.put("envDetails", manifestAttributes);
			envDetails.put("status", "Success");

			responseObject.add(envDetails);

		} catch (Exception e) {
			LOGGER.error(Logger.EVENT_FAILURE,
					"Exception occured while fetching maninfest info.", e);
			creditCheckVersionInfo.put("status", "Failed");
			creditCheckVersionInfo.put(
					"statusMessage",
					"Failed to Get Manifest Info. "
							+ UtilityService
									.convertExceptionStackTraceToString(e));
			responseObject.add(creditCheckVersionInfo);

		}

	}

	private void connectApiHealthCheck(List<Map<String, Object>> responseObject) {

		HttpEntity<String> httpEntity = new HttpEntity<>(
				restTemplateUtil.getCommonHttpHeaders());

		ResponseEntity<Map> exchange = null;
		String connectHealthCheckEndPoint = ekaConnectHost + "/healthcheck";
		try {
			;
			exchange = restTemplate.exchange(connectHealthCheckEndPoint,
					HttpMethod.POST, httpEntity, Map.class);
			if (exchange != null) {
				Map body = exchange.getBody();
				if (body.get("data") != null) {
					List<Map<String, Object>> list = (List<Map<String, Object>>) body
							.get("data");
					responseObject.addAll(list);
				}
			}
		} catch (Exception e) {
			LOGGER.error(Logger.EVENT_FAILURE,
					"Exception occured while calling "
							+ connectHealthCheckEndPoint, e);

			Map<String, Object> envDetails = new HashMap<>();
			envDetails.put("targetType", "Connect API");
			envDetails.put("target", "Connect API health check");
			envDetails.put(
					"statusMessage",
					"Exception occured while calling "
							+ connectHealthCheckEndPoint
							+ UtilityService
									.convertExceptionStackTraceToString(e));
			envDetails.put("status", "Failed");
		}

	}

	private void connectListingHealthCheck(
			List<Map<String, Object>> responseObject) {
		Map<String, Object> counterpartyListingStatus = new HashMap<>();
		counterpartyListingStatus.put("targetType", "Connect Listing");
		try {
			counterpartyListingStatus.put("target",
					"Counterparty Maintenance Listing");
			connectRestTemplate.getCounterpartyData(null,
					restTemplateUtil.getCommonHttpHeaders());
			counterpartyListingStatus.put("status", "Success");
		} catch (HttpClientErrorException he) {
			LOGGER.error(Logger.EVENT_FAILURE,
					"Failed to retrieve Counterparty Maintenance listing", he);
			counterpartyListingStatus.put("status", "Fail");
			counterpartyListingStatus.put(
					"statusMessage",
					he.getResponseBodyAsString()
							+ "; "
							+ UtilityService
									.convertExceptionStackTraceToString(he));
		} catch (Exception e) {
			LOGGER.error(Logger.EVENT_FAILURE,
					"Failed to retrieve Counterparty Maintenance listing", e);
			counterpartyListingStatus.put("status", "Fail");
			counterpartyListingStatus.put("statusMessage",
					UtilityService.convertExceptionStackTraceToString(e));
		}
		responseObject.add(counterpartyListingStatus);
		Map<String, Object> limitListingStatus = new HashMap<>();

		limitListingStatus.put("targetType", "Connect Listing");
		try {
			limitListingStatus.put("target", "Limit Maintenance Listing");
			connectRestTemplate.getLimitMaintenanceData(null,
					restTemplateUtil.getCommonHttpHeaders());
			limitListingStatus.put("status", "Success");

		} catch (HttpClientErrorException he) {
			limitListingStatus.put("status", "Fail");
			limitListingStatus.put(
					"statusMessage",
					he.getResponseBodyAsString()
							+ "; "
							+ UtilityService
									.convertExceptionStackTraceToString(he));
		} catch (Exception e) {
			LOGGER.error(Logger.EVENT_FAILURE,
					"Failed to retrieve Limit Maintenance listing", e);
			limitListingStatus.put("status", "Fail");
			limitListingStatus.put("statusMessage",
					UtilityService.convertExceptionStackTraceToString(e));
		}
		responseObject.add(limitListingStatus);

	}

	private void platformCollectionsHealthCheck(
			List<Map<String, Object>> responseObject) {

		Map<String, Object> exposureCollectionStatus = new HashMap<>();
		exposureCollectionStatus.put("targetType", "Platform Collection");
		HttpHeaders httpHeaders = restTemplateUtil.getCommonHttpHeaders();
		Map<String, Object> appProperties = requestContextHolder
				.getCurrentContext().getAppProperties();
		JSONObject filterObject = new JSONObject();
		filterObject.put("start", 0);
		filterObject.put("limit", 1);
		List<String> exposureCollectionNames = (ArrayList<String>) appProperties
				.get(PropertyConstants.COLLECTION_NAME_EXPOSURE_DATA);
		exposureCollectionStatus.put("target",
				String.join(",", exposureCollectionNames));
		try {
			platformDataService.getCounterPartyExposures(filterObject,
					httpHeaders, appProperties);

			exposureCollectionStatus.put("status", "Success");

		} catch (PlatformException e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", exposureCollectionNames), e);
			exposureCollectionStatus.put("status", "Fail");
			exposureCollectionStatus.put(
					"statusMessage",
					e.getMessage()
							+ " \n "
							+ UtilityService
									.convertExceptionStackTraceToString(e
											.getCause()));
		} catch (Exception e) {

			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", exposureCollectionNames), e);
			exposureCollectionStatus.put("status", "Fail");
			exposureCollectionStatus.put("statusMessage",
					UtilityService.convertExceptionStackTraceToString(e));
		}

		responseObject.add(exposureCollectionStatus);

		Map<String, Object> derivedPaymentTermsStatus = new HashMap<>();
		derivedPaymentTermsStatus.put("targetType", "Platform Collection");

		List<String> derivePaymentTerms = (ArrayList<String>) appProperties
				.get(PropertyConstants.COLLECTION_NAME_DERIVED_PAYMENT_TERMS);
		derivedPaymentTermsStatus.put("target",
				String.join(",", derivePaymentTerms));
		try {
			filterObject.put("start", 0);
			filterObject.put("limit", 1);
			platformDataService.getDerivedPaymentTerms(filterObject,
					httpHeaders, appProperties);

			derivedPaymentTermsStatus.put("status", "Success");

		} catch (PlatformException e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", derivePaymentTerms), e);
			derivedPaymentTermsStatus.put("status", "Fail");
			derivedPaymentTermsStatus.put(
					"statusMessage",
					e.getMessage()
							+ " \n "
							+ UtilityService
									.convertExceptionStackTraceToString(e
											.getCause()));
		} catch (Exception e) {

			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", derivePaymentTerms), e);
			derivedPaymentTermsStatus.put("status", "Fail");
			derivedPaymentTermsStatus.put("statusMessage",
					UtilityService.convertExceptionStackTraceToString(e));
		}

		responseObject.add(derivedPaymentTermsStatus);

		Map<String, Object> fxRatesStatus = new HashMap<>();
		fxRatesStatus.put("targetType", "Platform Collection");

		List<String> fxRates = (ArrayList<String>) appProperties
				.get(PropertyConstants.COLLECTION_NAME_FX_RATES);
		fxRatesStatus.put("target", String.join(",", fxRates));
		try {
			filterObject.put("start", 0);
			filterObject.put("limit", 1);
			platformDataService.getFxRates(httpHeaders, appProperties);

			fxRatesStatus.put("status", "Success");

		} catch (PlatformException e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", fxRates), e);

			fxRatesStatus.put("status", "Fail");
			fxRatesStatus.put(
					"statusMessage",
					e.getMessage()
							+ " \n "
							+ UtilityService
									.convertExceptionStackTraceToString(e
											.getCause()));
		} catch (Exception e) {

			LOGGER.error(
					Logger.EVENT_FAILURE,
					"Health check failed for collections "
							+ String.join(",", fxRates), e);
			fxRatesStatus.put("status", "Fail");
			fxRatesStatus.put("statusMessage",
					UtilityService.convertExceptionStackTraceToString(e));
		}

		responseObject.add(fxRatesStatus);
	}

}
