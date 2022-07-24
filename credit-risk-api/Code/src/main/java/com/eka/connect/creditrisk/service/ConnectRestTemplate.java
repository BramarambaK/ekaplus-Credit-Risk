package com.eka.connect.creditrisk.service;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.RelativeUrlConstants;
import com.eka.connect.creditrisk.dataobject.ConnectData;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CreditStopEligibility;
import com.eka.connect.creditrisk.dataobject.FilterData;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;
import com.eka.connect.creditrisk.dataobject.PropertyData;
import com.eka.connect.creditrisk.dataobject.WorkflowData;
import com.eka.connect.creditrisk.dataobject.WorkflowPojo;
import com.eka.connect.creditrisk.util.RestTemplateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConnectRestTemplate {

	private static final Logger LOGGER = ESAPI
			.getLogger(ConnectRestTemplate.class);

	@Value("${eka_connect_host}")
	private String ekaConnectHost;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	public List<CounterPartyDetails> getCounterpartyData(
			FilterData filterData, HttpHeaders httpHeaders) {
		WorkflowData counterPartyFilterData = getConnectWorkflowFilterObject(filterData,"counterpartylist");

		List<CounterPartyDetails> connectData = getCounterpartyDataByWorkFlow(
				
				counterPartyFilterData, httpHeaders);
		return connectData;

	}

	private WorkflowData getConnectWorkflowFilterObject(FilterData filterData,
			String workflowTaskName) {
		WorkflowData counterPartyFilterData = new WorkflowData();
		counterPartyFilterData
				.setAppId(CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID);
		counterPartyFilterData.setWorkFlowTask(workflowTaskName);

		counterPartyFilterData.setFilterData(filterData);

		return counterPartyFilterData;
	}

	public List<CreditStopEligibility> getCreditStopDataByWorkflow(
			FilterData filterData, HttpHeaders httpHeaders) {
		ParameterizedTypeReference<ConnectData> parameterizedTypeReference = new ParameterizedTypeReference<ConnectData>() {
		};
		WorkflowData workflowFilterData  = getConnectWorkflowFilterObject(filterData,"creditstoplist");
		ConnectData connectWorkflowData = getConnectDataObject(
				ekaConnectHost
						+ RelativeUrlConstants.CONNECT_WORKFLOW_GET_DATA,
						workflowFilterData, parameterizedTypeReference, httpHeaders);
		List<CreditStopEligibility> creditStoplist = null;
		if (connectWorkflowData != null
				&& connectWorkflowData.getData() != null) {
			ObjectMapper mapper = new ObjectMapper();
			creditStoplist = mapper.convertValue(connectWorkflowData.getData(),
					new TypeReference<List<CreditStopEligibility>>() {
					});
		}

		return creditStoplist;

	}

	public List<LimitMaintenanceDetails> getLimitMaintenanceData(
			FilterData filterData, HttpHeaders httpHeaders) {
		ParameterizedTypeReference<ConnectData> parameterizedTypeReference = new ParameterizedTypeReference<ConnectData>() {
		};
		WorkflowData workflowFilterData  = getConnectWorkflowFilterObject(filterData,"limitlist");

		String dataUrl = ekaConnectHost
				+ RelativeUrlConstants.CONNECT_WORKFLOW_GET_DATA;
		ConnectData connectWorkflowData = getConnectDataObject(dataUrl,
				workflowFilterData, parameterizedTypeReference, httpHeaders);
		List<LimitMaintenanceDetails> limitList = null;
		if (connectWorkflowData != null
				&& connectWorkflowData.getData() != null) {
			ObjectMapper mapper = new ObjectMapper();
			limitList = mapper.convertValue(connectWorkflowData.getData(),
					new TypeReference<List<LimitMaintenanceDetails>>() {
					});
		}
		return limitList;

	}

	public PropertyData getPropertyData(String propertyKey,
			HttpHeaders httpHeaders) {

		ParameterizedTypeReference<PropertyData> parameterizedTypeReference = new ParameterizedTypeReference<PropertyData>() {
		};

		PropertyData propertyData = getConnectDataObject(ekaConnectHost
				+ RelativeUrlConstants.CONNECT_PROPERTY_URL + propertyKey,
				null, parameterizedTypeReference, httpHeaders);
		return propertyData;

	}

	private <T> T getConnectDataObject(String url,
			WorkflowData workFlowData,
			ParameterizedTypeReference<T> parameterizedTypeReference,
			HttpHeaders headers) {

		if (workFlowData != null && workFlowData.getFilterData() != null) {
			workFlowData.convertFilterToElasticFilter();
			workFlowData.resetFilter();
		}
		try{
			HttpEntity<WorkflowData> httpEntity = new HttpEntity<>(workFlowData, headers);
		
			LOGGER.info(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Started calling Connect url :: " + url
									+ " with Filter Data \n "
									+ workFlowData.toString()));

		// LOGGER.debug("Headers " + headers);
		ResponseEntity<T> responseEntity = restTemplate.exchange(url,
				HttpMethod.POST, httpEntity, parameterizedTypeReference);
		
			LOGGER.info(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Completed calling Connect url :: " + url));
		return responseEntity.getBody();
		}
		catch (HttpClientErrorException he) {
			
			
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("Connect HttpClientErrorException Exception details --> "
							+ he.getRawStatusCode() + "" + he.getResponseBodyAsString()
							+ he.getResponseHeaders())); 

			throw he;

		} 

	}

	public List<CounterPartyDetails> getCounterpartyDataByWorkFlow(
			WorkflowData workFlowData, HttpHeaders httpHeaders) {
		ParameterizedTypeReference<ConnectData> parameterizedTypeReference = new ParameterizedTypeReference<ConnectData>() {
		};

		String dataUrl = ekaConnectHost
				+ RelativeUrlConstants.CONNECT_WORKFLOW_GET_DATA;
		ConnectData connectWorkflowData = getConnectDataObject(dataUrl,
				workFlowData, parameterizedTypeReference, httpHeaders);
		List<CounterPartyDetails> counterpartylist = null;
		if (connectWorkflowData != null
				&& connectWorkflowData.getData() != null) {
			ObjectMapper mapper = new ObjectMapper();
			counterpartylist = mapper.convertValue(
					connectWorkflowData.getData(),
					new TypeReference<List<CounterPartyDetails>>() {
					});
		}
		return counterpartylist;
	}

	public void saveWorkflowObject(WorkflowPojo workflowObj,
			HttpHeaders httpHeaders) {
		try {
			HttpEntity<WorkflowPojo> httpEntity = new HttpEntity<>(workflowObj,
					httpHeaders);
			String url = ekaConnectHost
					+ RelativeUrlConstants.CONNECT_WORKFLOW_SAVE;
			LOGGER.info(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"WorkflowPojo save connect url :: " + url));
 
			ResponseEntity<String> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, String.class);
			LOGGER.info(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Response status" + responseEntity.getStatusCode()));
		} catch (Exception e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder()
							.encodeForHTML(
									e.getClass().getName()
											+ "occured. unable to save workflow data.."),
					e);

			throw e;
		}

	}

	public String getMdmData(String url, String serviceKey) {
		HttpHeaders headers = restTemplateUtil.getCommonHttpHeaders();

		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("serviceKey", serviceKey);
		array.put(obj);

		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("mdm url :: " + url));

		HttpEntity<String> httpEntity = new HttpEntity<>(array.toString(),
				headers);
		LOGGER.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Headers " + headers));
		
		ResponseEntity<String> responseEntity = restTemplate.exchange(url,
				HttpMethod.POST, httpEntity, String.class);
		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Response " + responseEntity.getBody()));
		return responseEntity.getBody();

	}

	public Map<String, Object> getAppProperties() {

		final String url = ekaConnectHost
				+ RelativeUrlConstants.CONNECT_GET_ALL_PROPERTIES_BY_APP
						.replace("{refTypeId}",
								CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID);
		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("getAppProperties url :: " + url));
		HttpHeaders headers = restTemplateUtil.getCommonHttpHeaders();
		ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<Map<String, Object>>() {
		};

		// Map<String, Object> propMap = new HashMap<String, Object>();
		// propMap.put("visibility", "public");
		HttpEntity<String> httpEntity = new HttpEntity<>("{}", headers);
		ResponseEntity<Map<String, Object>> exchangeData = restTemplate
				.exchange(url, HttpMethod.POST, httpEntity,
						parameterizedTypeReference);
		LOGGER.info(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Properties data call is success.."));
		
		return exchangeData.getBody();
	}
}