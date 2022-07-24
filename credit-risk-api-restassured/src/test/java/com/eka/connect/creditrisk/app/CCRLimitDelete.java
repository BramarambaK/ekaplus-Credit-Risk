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

public class CCRLimitDelete {

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
	public void testLimitDeletePageAPIs() {
		// call layout--
		String layoutCallPayloadString = "{\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"workFlowTask\":\"deletelimitlist\",\"payLoadData\":{\"_id\":\"5e55085fc9e77c000190d78e\",\"creditLimitSourceDisplayName\":\"Nexus\",\"amount\":34,\"sys__data__state\":\"Modify\",\"creditLimitTypeDisplayName\":\"Top Up Credit Limit\",\"fromPeriod\":\"2020-02-03\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"sys__version\":1,\"refType\":\"app\",\"refTypeId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"userId\":\"2534\",\"creditLimitType\":\"creditLimitType-008\",\"limitStatus\":\"limitStatus-002\",\"sys__createdBy\":\"admin@ekaplus.com\",\"toPeriod\":\"2020-02-04\",\"limitRefNo\":\"LM-81-REF\",\"currency\":\"USD\",\"creditLimitSource\":\"creditLimitSource-002\",\"sys__createdOn\":\"1582631007068\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"limitStatusDisplayName\":\"Inactive\",\"object\":\"26e782e8-89e5-40ea-a3be-63b320260b7d\",\"sys__UUID\":\"2916b951-b79c-45c4-99e6-6a2636a08b44\",\"coverPercentage\":\"45\",\"referenceNo\":\"rfdgd\",\"sys__updatedBy\":\"admin@ekaplus.com\",\"sys__updatedOn\":\"1582784796390\",\"counterpartyName\":\"4 A Sante Industries Spa\",\"sys__state\":{\"creditLimitSourceDisplayName\":{\"show\":true,\"disable\":false},\"amount\":{\"show\":true,\"disable\":false},\"sys__data__state\":{\"show\":true,\"disable\":false},\"creditLimitTypeDisplayName\":{\"show\":true,\"disable\":false},\"fromPeriod\":{\"show\":true,\"disable\":false},\"counterpartyGroupName\":{\"show\":true,\"disable\":false},\"sys__version\":{\"show\":true,\"disable\":false},\"refType\":{\"show\":true,\"disable\":false},\"refTypeId\":{\"show\":true,\"disable\":false},\"userId\":{\"show\":true,\"disable\":false},\"creditLimitType\":{\"show\":true,\"disable\":false},\"limitStatus\":{\"show\":true,\"disable\":false},\"sys__createdBy\":{\"show\":true,\"disable\":false},\"toPeriod\":{\"show\":true,\"disable\":false},\"limitRefNo\":{\"show\":true,\"disable\":false},\"currency\":{\"show\":true,\"disable\":false},\"creditLimitSource\":{\"show\":true,\"disable\":false},\"sys__createdOn\":{\"show\":true,\"disable\":false},\"counterpartyGroupNameDisplayName\":{\"show\":true,\"disable\":false},\"limitStatusDisplayName\":{\"show\":true,\"disable\":false},\"object\":{\"show\":true,\"disable\":false},\"sys__UUID\":{\"show\":true,\"disable\":false},\"coverPercentage\":{\"show\":true,\"disable\":false},\"referenceNo\":{\"show\":true,\"disable\":false},\"sys__updatedBy\":{\"show\":true,\"disable\":false},\"sys__updatedOn\":{\"show\":true,\"disable\":false},\"counterpartyName\":{\"show\":true,\"disable\":false}}}}";
		Response layoutResponse = callAPI(Method.POST, "/workflow/layout",
				generatePayloadFromString(layoutCallPayloadString));
		verify200OKResponse(layoutResponse);
		layoutResponse.then().assertThat().body("appId", is("5539617b-5075-4482-8bcc-26f76849eb89"));
		
		// message layout--
				Response messageResponse = callAPI(Method.GET, "/meta/message",null);
				verify200OKResponse(messageResponse);
				messageResponse.then().assertThat().body("type", is("message"));
				
		//workflow call
//		String workflowCallPayloadString = "{\"workflowTaskName\":\"deletelimitlist\",\"task\":\"deletelimitlist\",\"appName\":\"creditrisk\",\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"output\":{\"deletelimitlist\":{\"_id\":\"5e55085fc9e77c000190d78e\",\"creditLimitSourceDisplayName\":\"Nexus\",\"amount\":34,\"sys__data__state\":\"Modify\",\"creditLimitTypeDisplayName\":\"Top Up Credit Limit\",\"fromPeriod\":\"2020-02-03\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"sys__version\":1,\"refType\":\"app\",\"refTypeId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"userId\":\"2534\",\"creditLimitType\":\"creditLimitType-008\",\"limitStatus\":\"limitStatus-002\",\"sys__createdBy\":\"admin@ekaplus.com\",\"toPeriod\":\"2020-02-04\",\"limitRefNo\":\"LM-81-REF\",\"currency\":\"USD\",\"creditLimitSource\":\"creditLimitSource-002\",\"sys__createdOn\":\"1582631007068\",\"counterpartyGroupNameDisplayName\":\"4 A Sante Industries Spa\",\"limitStatusDisplayName\":\"Inactive\",\"object\":\"26e782e8-89e5-40ea-a3be-63b320260b7d\",\"sys__UUID\":\"2916b951-b79c-45c4-99e6-6a2636a08b44\",\"coverPercentage\":\"45\",\"referenceNo\":\"rfdgd\",\"sys__updatedBy\":\"admin@ekaplus.com\",\"sys__updatedOn\":\"1582784796390\",\"counterpartyName\":\"4 A Sante Industries Spa\",\"sys__state\":{\"creditLimitSourceDisplayName\":{\"show\":true,\"disable\":false},\"amount\":{\"show\":true,\"disable\":false},\"sys__data__state\":{\"show\":true,\"disable\":false},\"creditLimitTypeDisplayName\":{\"show\":true,\"disable\":false},\"fromPeriod\":{\"show\":true,\"disable\":false},\"counterpartyGroupName\":{\"show\":true,\"disable\":false},\"sys__version\":{\"show\":true,\"disable\":false},\"refType\":{\"show\":true,\"disable\":false},\"refTypeId\":{\"show\":true,\"disable\":false},\"userId\":{\"show\":true,\"disable\":false},\"creditLimitType\":{\"show\":true,\"disable\":false},\"limitStatus\":{\"show\":true,\"disable\":false},\"sys__createdBy\":{\"show\":true,\"disable\":false},\"toPeriod\":{\"show\":true,\"disable\":false},\"limitRefNo\":{\"show\":true,\"disable\":false},\"currency\":{\"show\":true,\"disable\":false},\"creditLimitSource\":{\"show\":true,\"disable\":false},\"sys__createdOn\":{\"show\":true,\"disable\":false},\"counterpartyGroupNameDisplayName\":{\"show\":true,\"disable\":false},\"limitStatusDisplayName\":{\"show\":true,\"disable\":false},\"object\":{\"show\":true,\"disable\":false},\"sys__UUID\":{\"show\":true,\"disable\":false},\"coverPercentage\":{\"show\":true,\"disable\":false},\"referenceNo\":{\"show\":true,\"disable\":false},\"sys__updatedBy\":{\"show\":true,\"disable\":false},\"sys__updatedOn\":{\"show\":true,\"disable\":false},\"counterpartyName\":{\"show\":true,\"disable\":false}}}},\"id\":\"5e55085fc9e77c000190d78e\"}";
//		Response workflowResponse = callAPI(Method.POST, "/workflow",
//				generatePayloadFromString(workflowCallPayloadString));
//		verify200OKResponse(workflowResponse);
//		workflowResponse.then().assertThat().body("message", containsString("Limit got deleted successfully"));
		
		


		
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