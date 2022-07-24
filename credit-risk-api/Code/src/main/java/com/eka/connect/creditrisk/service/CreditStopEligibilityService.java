package com.eka.connect.creditrisk.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.RelativeUrlConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CreditStopEligibility;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.dataobject.WorkflowData;
import com.eka.connect.creditrisk.dataobject.WorkflowPojo;
import com.eka.connect.creditrisk.util.CreditStopEligibilityExecutor;
import com.eka.connect.creditrisk.util.RestTemplateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CreditStopEligibilityService {

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static final Logger LOGGER = ESAPI
			.getLogger(CreditStopEligibilityService.class);
	@Autowired
	ConnectRestTemplate connectRestTemplate;

	@Autowired
	CreditCheckService creditCheckService;

	@Autowired
	RestTemplateUtil templateUtil;

	@Autowired
	private RequestContextHolder requestContextHolder;

	@Scheduled(cron = "${cron.expression}")
	public void scheduleFixedDelayTask() {
		System.out.println("Fixed delay task - " + System.currentTimeMillis()
				/ 1000);
	}

	public void execute() {
		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Credit Stop Eligibility schedule job initiated..."));
		List<CreditStopEligibility> eligibilityData = getCreditStopEligibilityData(templateUtil
				.getCommonHttpHeaders());
		// this data has to come via workflow api.
		WorkflowData counterPartyFilterData = new WorkflowData();
		counterPartyFilterData
				.setAppId(CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID);
		counterPartyFilterData.setWorkFlowTask("counterpartylist");

		counterPartyFilterData.setFilterData(null);
		List<CounterPartyDetails> counterpartyList = connectRestTemplate
				.getCounterpartyDataByWorkFlow(counterPartyFilterData,
						templateUtil.getCommonHttpHeaders());
		if (!CollectionUtils.isEmpty(eligibilityData)
				&& !CollectionUtils.isEmpty(counterpartyList)) {

			new CreditStopEligibilityExecutor(eligibilityData,
					counterpartyList,
					CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID).execute();
			updateCounterPartyStatus(counterpartyList,
					templateUtil.getCommonHttpHeaders());
		}

		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Credit Stop Eligibility schedule job completed...."));

	}

	private void updateCounterPartyStatus(
			List<CounterPartyDetails> counterpartyData, HttpHeaders httpHeaders) {
		Date systemDate = new Date();
		String dateString = new SimpleDateFormat(DATE_FORMAT)
				.format(systemDate);
		/*
		 * PropertyData propertyData = connectRestTemplate
		 * .getPropertyData("eka_mdm_host");
		 */
		String url = (String) requestContextHolder.getCurrentContext()
				.getAppProperties().get("eka_mdm_host")
				+ RelativeUrlConstants.MDM_DATA_URL;
		String mdmDataString = connectRestTemplate.getMdmData(url,
				"creditRiskStatus");
		JSONObject mdmData = new JSONObject(mdmDataString);
		JSONArray jsonArray = mdmData.getJSONArray("creditRiskStatus");
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject o = jsonArray.getJSONObject(i);
			map.put(o.getString("value"), o.getString("key"));
		}
		List<CounterPartyDetails> toBeUpdatedList = counterpartyData.stream()
				.filter(e -> e.getCreditStopStatus() != null)
				.collect(Collectors.toList());
		for (CounterPartyDetails e : toBeUpdatedList) {
			e.setCreditRiskStatus(map.get(e.getCreditStopStatus()));
			e.setCreditRiskStatusDisplayName(e.getCreditStopStatus());
			e.setId(e.getConnectId());
			if(e.getReference()==null || e.getReference().length()==0)
			e.setReference("Credit Stop eligibility scheduler update");
		}
		prepareWorkflowObjectAndSaveCounterparty(toBeUpdatedList, httpHeaders);

	}

	private void prepareWorkflowObjectAndSaveCounterparty(
			List<CounterPartyDetails> e, HttpHeaders httpHeaders) {

		List<List<CounterPartyDetails>> finalList = new ArrayList<>();
		int count = 0;
		int chunkSize = 1000;
		for (CounterPartyDetails c : e) {
			if (count % chunkSize == 0) {
				finalList.add(new ArrayList<>());
			}
			count++;
			finalList.get(finalList.size() - 1).add(c);
		}
		for (List<CounterPartyDetails> list : finalList) {

			WorkflowPojo workflowObj = new WorkflowPojo();
			workflowObj
					.setAppId(CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID);
			workflowObj.setTask("editcounterparty");
			workflowObj.setWorkflowTaskName(workflowObj.getTask());
			workflowObj.getOutput()
					.put(workflowObj.getWorkflowTaskName(), list);

			connectRestTemplate.saveWorkflowObject(workflowObj, httpHeaders);
			try{
				LOGGER.info(
						Logger.EVENT_SUCCESS,
						ESAPI.encoder().encodeForHTML(
								new ObjectMapper()
										.writerWithDefaultPrettyPrinter()
										.writeValueAsString(workflowObj)));
			}catch(Exception ex){
				
			}finally{
				
			}
		}

	}

	private List<CreditStopEligibility> getCreditStopEligibilityData(
			HttpHeaders httpHeaders) {

		Map<String, Object> queryMap = new HashMap<>();

		queryMap.put("eligible", true);
		
	/*	FilterData creditStopFilterData = new FilterData();
		creditStopFilterData.setFilter(new ArrayList<MongoOperations>());
		MongoOperations operation = new MongoOperations();
		operation.setOperator("eq");
		operation.setFieldName("eligible");
		operation.setValue(true);
		creditStopFilterData.getFilter().add(operation);*/

		return connectRestTemplate.getCreditStopDataByWorkflow(null,
				httpHeaders);

	}

}
