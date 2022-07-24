package com.eka.connect.creditrisk.app;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class CCRLimitcreate {

	String token = null;
	String tenant = null;
	String userName = null;
	String password = null;
	Map<String, Object> requestPayload = new HashMap<String, Object>();

	private static final String tokenGenerationApiPath = "/api/authenticate";
	private static final String validateNewPassWordApiPath = "/authenticate/validateNewPassword";

	@BeforeTest
	public void setUp() throws Exception {

		Properties prop = new Properties();
		prop.load(new FileInputStream(ResourceUtils.getFile("classpath:RestAssuredTest.properties")));
		tenant = prop.getProperty("tenant");
		URL url = new URL((String) prop.getProperty("eka_connect_host"));
		RestAssured.baseURI = "http://" + url.getHost();
		RestAssured.port = url.getPort();
		userName = prop.getProperty("userName");
		password = prop.getProperty("password");
		token = authenticateUser(userName, password);
	}

	@Test(enabled = true)
	public void testLimitCreatePageAPIs() {
		// call layout--
		String layoutCallPayloadString = "{\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"workFlowTask\":\"createmaintenance\",\"payLoadData\":\"\"}";
		Response layoutResponse = callAPI(Method.POST, "/workflow/layout",
				generatePayloadFromString(layoutCallPayloadString));
		verify200OKResponse(layoutResponse);
		layoutResponse.then().assertThat().body("appId", is("5539617b-5075-4482-8bcc-26f76849eb89"));
		//mdm call
		String mdmCallPayloadString = "{\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"data\":[{\"serviceKey\":\"businessPartnerCombo\",\"dependsOn\":[\"Third_Party\"]},{\"serviceKey\":\"limitStatus\"}],\"workFlowTask\":\"createmaintenance\",\"params\":{},\"payLoadData\":{}}";
		Response mdmResponse = callAPI(Method.POST, "/workflow/mdm",
				generatePayloadFromString(mdmCallPayloadString));
		verify200OKResponse(mdmResponse);
		mdmResponse.then().assertThat().body("limitStatus.size()", greaterThan(0));
		//2nd
		String mdmCall2PayloadString = "{\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"data\":[{\"serviceKey\":\"creditLimitSource\",\"dependsOn\":[\"PHD-M0-105835\"]},{\"serviceKey\":\"currency\",\"dependsOn\":[\"PHD-M0-105835\"]}],\"workFlowTask\":\"createmaintenance\",\"params\":{},\"payLoadData\":{\"currency\":\"\",\"currencyDisplayName\":\"\",\"counterpartyName\":\"\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"limitRefNo\":\"\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"creditLimitSource\":\"\",\"creditLimitSourceDisplayName\":\"\",\"Period\":\"\",\"contactName\":\"\",\"creditLimitType\":\"\",\"creditLimitTypeDisplayName\":\"\",\"fromPeriod\":\"\",\"toPeriod\":\"\",\"amount\":\"\",\"amount2\":\"\",\"limitStatus\":\"\",\"limitStatusDisplayName\":\"\",\"referenceNo\":\"\",\"coverPercentage\":\"\",\"maxPaymentTerm\":\"\",\"remarks\":\"\",\"contractRefno\":\"\",\"createmaintenance\":{\"currency\":\"\",\"currencyDisplayName\":\"\",\"counterpartyName\":\"\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"limitRefNo\":\"\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"creditLimitSource\":\"\",\"creditLimitSourceDisplayName\":\"\",\"Period\":\"\",\"contactName\":\"\",\"creditLimitType\":\"\",\"creditLimitTypeDisplayName\":\"\",\"fromPeriod\":\"\",\"toPeriod\":\"\",\"amount\":\"\",\"amount2\":\"\",\"limitStatus\":\"\",\"limitStatusDisplayName\":\"\",\"referenceNo\":\"\",\"coverPercentage\":\"\",\"maxPaymentTerm\":\"\",\"remarks\":\"\",\"contractRefno\":\"\"}}}";
				Response mdmResponse2 = callAPI(Method.POST, "/workflow/mdm",
						generatePayloadFromString(mdmCall2PayloadString));
			
		verify200OKResponse(mdmResponse2);
		mdmResponse2.then().assertThat().body("creditLimitSource.size()", greaterThan(0));
		//3rd
		String mdmCall3PayloadString = "{\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"data\":[{\"serviceKey\":\"creditLimitType\",\"dependsOn\":[\"creditLimitSource-002\"]}],\"workFlowTask\":\"createmaintenance\",\"params\":{},\"payLoadData\":{\"currency\":\"USD\",\"currencyDisplayName\":\"\",\"counterpartyName\":\"\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"limitRefNo\":\"\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"creditLimitSource\":\"creditLimitSource-002\",\"creditLimitSourceDisplayName\":\"Nexus\",\"Period\":\"\",\"contactName\":\"\",\"creditLimitType\":\"\",\"creditLimitTypeDisplayName\":\"\",\"fromPeriod\":\"\",\"toPeriod\":\"\",\"amount\":\"\",\"amount2\":\"\",\"limitStatus\":\"\",\"limitStatusDisplayName\":\"\",\"referenceNo\":\"\",\"coverPercentage\":\"\",\"maxPaymentTerm\":\"\",\"remarks\":\"\",\"contractRefno\":\"\",\"createmaintenance\":{\"currency\":\"USD\",\"currencyDisplayName\":\"\",\"counterpartyName\":\"\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"limitRefNo\":\"\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"creditLimitSource\":\"creditLimitSource-002\",\"creditLimitSourceDisplayName\":\"Nexus\",\"Period\":\"\",\"contactName\":\"\",\"creditLimitType\":\"\",\"creditLimitTypeDisplayName\":\"\",\"fromPeriod\":\"\",\"toPeriod\":\"\",\"amount\":\"\",\"amount2\":\"\",\"limitStatus\":\"\",\"limitStatusDisplayName\":\"\",\"referenceNo\":\"\",\"coverPercentage\":\"\",\"maxPaymentTerm\":\"\",\"remarks\":\"\",\"contractRefno\":\"\"}}}";
		Response mdmResponse3 = callAPI(Method.POST, "/workflow/mdm",
				generatePayloadFromString(mdmCall3PayloadString));
		verify200OKResponse(mdmResponse3);
		mdmResponse3.then().assertThat().body("creditLimitType.size()", greaterThan(0));
		
		//workflow call
		String workflowCallPayloadString = "{\"workflowTaskName\":\"createmaintenance\",\"task\":\"createmaintenance\",\"appName\":\"creditrisk\",\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"output\":{\"createmaintenance\":{\"currency\":\"USD\",\"sys__state\":{\"currency\":{\"show\":true,\"disable\":false},\"currencyDisplayName\":{\"show\":true,\"disable\":false},\"counterpartyName\":{\"show\":true,\"disable\":false},\"counterpartyGroupName\":{\"show\":true,\"disable\":false},\"limitRefNo\":{\"show\":true,\"disable\":false},\"counterpartyGroupNameDisplayName\":{\"show\":true,\"disable\":false},\"creditLimitSource\":{\"show\":true,\"disable\":false},\"creditLimitSourceDisplayName\":{\"show\":true,\"disable\":false},\"Period\":{\"show\":true,\"disable\":false},\"contactName\":{\"show\":true,\"disable\":false},\"creditLimitType\":{\"show\":true,\"disable\":false},\"creditLimitTypeDisplayName\":{\"show\":true,\"disable\":false},\"fromPeriod\":{\"show\":true,\"disable\":false},\"toPeriod\":{\"show\":true,\"disable\":false},\"amount\":{\"show\":true,\"disable\":false},\"amount2\":{\"show\":true,\"disable\":false},\"limitStatus\":{\"show\":true,\"disable\":false},\"limitStatusDisplayName\":{\"show\":true,\"disable\":false},\"referenceNo\":{\"show\":true,\"disable\":false},\"coverPercentage\":{\"show\":true,\"disable\":false},\"maxPaymentTerm\":{\"show\":true,\"disable\":false},\"remarks\":{\"show\":true,\"disable\":false},\"contractRefno\":{\"show\":true,\"disable\":false}},\"counterpartyGroupName\":\"PHD-M0-105835\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"creditLimitSource\":\"creditLimitSource-002\",\"creditLimitSourceDisplayName\":\"Nexus\",\"creditLimitType\":\"creditLimitType-008\",\"creditLimitTypeDisplayName\":\"Top Up Credit Limit\",\"fromPeriod\":\"2020-02-03\",\"toPeriod\":\"2020-02-04\",\"amount\":\"34\",\"limitStatus\":\"limitStatus-002\",\"limitStatusDisplayName\":\"Inactive\"}},\"id\":\"\"}";
		Response workflowResponse = callAPI(Method.POST, "/workflow",
				generatePayloadFromString(workflowCallPayloadString));
		verify200OKResponse(workflowResponse);
		workflowResponse.then().assertThat().body("message", containsString("Limit Maintenance got created successfully"));


		
	}

	

	private Response callAPI(Method httpMethod, String path, Map<String, Object> payload) {
		switch (httpMethod) {
		case GET:
			return given().log().all().header("Authorization", token).header("X-TenantID", tenant)
					.header("Content-Type", "application/json").when().request(httpMethod.name(), path);
		case POST:
		case PUT:
		case DELETE:
			return given().log().all().header("Authorization", token).header("X-TenantID", tenant)
					.header("Content-Type", "application/json").with().body(payload).when()
					.request(httpMethod.name(), path);
		}
		return null;
	}

	private Map<String, Object> generatePayloadFromString(String payload) {
		return new JSONObject(payload).toMap();
	}

	private void verify200OKResponse(Response response) {
		Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
	}

	private String authenticateUser(String username, String password) throws UnsupportedEncodingException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("userName", username);
		body.put("password", password);
		String base64encodedUsernamePassword = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes("utf-8"));
		Response response = given().header("Content-Type", "application/json")
				.header("Authorization", "Basic " + base64encodedUsernamePassword).header("X-TenantID", tenant)
				.body(body).when().post(tokenGenerationApiPath);
		JsonPath jsonPath = new JsonPath(response.asString());
		return jsonPath.getString("auth2AccessToken.access_token");
	}
}