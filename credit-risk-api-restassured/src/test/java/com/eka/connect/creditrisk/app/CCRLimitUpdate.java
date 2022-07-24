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

public class CCRLimitUpdate {

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
	public void testLimitUpdatePageAPIs() {
		
		//workflow call
		String workflowCallPayloadString = "{\"task\":\"editlimitlist\",\"output\":{\"editlimitlist\":{\"fromPeriod\":\"2020-02-03\",\"toPeriod\":\"2020-02-04\",\"amount\":34,\"limitStatus\":\"limitStatus-002\",\"limitStatusDisplayName\":\"Inactive\",\"referenceNo\":\"rfdgd\",\"coverPercentage\":\"45\",\"limitRefNo\":\"LM-81-REF\",\"counterpartyGroupName\":\"PHD-M0-105835\",\"creditLimitSource\":\"creditLimitSource-002\",\"creditLimitType\":\"creditLimitType-008\"}},\"id\":\"5e55085fc9e77c000190d78e\",\"appId\":\"5539617b-5075-4482-8bcc-26f76849eb89\",\"appName\":\"creditrisk\",\"objectDetails\":{\"_id\":\"object_limitmaintenance\",\"additionalProperties\":{},\"createdBy\":\"\",\"createdOn\":\"\",\"fields\":{\"currency\":{\"currency\":\"Currency\",\"labelKey\":\"currency\",\"type\":\"dropdown\",\"isRequired\":false,\"dataType\":\"String\",\"serviceKey\":\"currency\",\"parent\":[\"counterpartyGroupName\"]},\"currencyDisplayName\":{\"currencyDisplayName\":\"Currency\",\"labelKey\":\"currencyDisplayName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"counterpartyName\":{\"counterpartyName\":\"Counterparty Group/Name\",\"labelKey\":\"counterpartyName\",\"type\":\"textbox\",\"isRequired\":false,\"dataType\":\"String\"},\"counterpartyGroupName\":{\"counterpartyGroupName\":\"Counterparty Group/Name\",\"labelKey\":\"counterpartyGroupName\",\"type\":\"dropdown\",\"isRequired\":true,\"dataType\":\"String\",\"serviceKey\":\"businessPartnerCombo\",\"dropdownValue\":\"counterpartyGroupNameDisplayName\",\"dependsOn\":[\"Third_Party\"],\"children\":[\"creditLimitSource\",\"currency\"]},\"limitRefNo\":{\"limitRefNo\":\"Limit Ref No\",\"labelKey\":\"limitRefNo\",\"serialNumber\":{\"startsWith\":\"0\",\"prefix\":\"LM-\",\"suffix\":\"-REF\"}},\"counterpartyGroupNameDisplayName\":{\"counterpartyGroupNameDisplayName\":\"Counterparty Group/Name\",\"labelKey\":\"counterpartyGroupNameDisplayName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"creditLimitSource\":{\"creditLimitSource\":\"Credit Limit Source\",\"labelKey\":\"creditLimitSource\",\"type\":\"dropdown\",\"isRequired\":true,\"dataType\":\"String\",\"serviceKey\":\"creditLimitSource\",\"dropdownValue\":\"creditLimitSourceDisplayName\",\"parent\":[\"counterpartyGroupName\"],\"children\":[\"creditLimitType\"]},\"creditLimitSourceDisplayName\":{\"creditLimitSourceDisplayName\":\"Credit Limit Source\",\"labelKey\":\"creditLimitSourceDisplayName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"Period\":{\"Period\":\"Period\",\"labelKey\":\"Period\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"contactName\":{\"contactName\":\"Name of Contact Person\",\"labelKey\":\"contactName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"creditLimitType\":{\"creditLimitType\":\"Credit Limit Type\",\"labelKey\":\"creditLimitType\",\"type\":\"dropdown\",\"isRequired\":true,\"dataType\":\"String\",\"serviceKey\":\"creditLimitType\",\"dropdownValue\":\"creditLimitTypeDisplayName\",\"parent\":[\"creditLimitSource\"]},\"creditLimitTypeDisplayName\":{\"creditLimitTypeDisplayName\":\"Credit Limit Type\",\"labelKey\":\"creditLimitTypeDisplayName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"fromPeriod\":{\"fromPeriod\":\"From Period\",\"labelKey\":\"fromPeriod\",\"type\":\"datepicker\",\"isRequired\":false,\"dataType\":\"date\",\"format\":\"yyyy-MM-dd\",\"comparison\":[{\"compareTo\":\"toPeriod\",\"operator\":\"<=\"}]},\"toPeriod\":{\"toPeriod\":\"To Period\",\"labelKey\":\"toPeriod\",\"type\":\"datepicker\",\"isRequired\":false,\"format\":\"yyyy-MM-dd\",\"dataType\":\"date\"},\"amount\":{\"amount\":\"Amount\",\"labelKey\":\"amount\",\"type\":\"textbox\",\"isRequired\":true,\"dataType\":\"number\"},\"amount2\":{\"amount2\":\"Amount\",\"labelKey\":\"amount2\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"number\"},\"limitStatus\":{\"limitStatus\":\"Limit Status\",\"labelKey\":\"limitStatus\",\"type\":\"dropdown\",\"isRequired\":true,\"dataType\":\"String\",\"serviceKey\":\"limitStatus\",\"dropdownValue\":\"limitStatusDisplayName\"},\"limitStatusDisplayName\":{\"limitStatusDisplayName\":\"Limit Status\",\"labelKey\":\"limitStatusDisplayName\",\"type\":\"hidden\",\"isRequired\":false,\"dataType\":\"String\"},\"referenceNo\":{\"referenceNo\":\"Decision Ref. No.\",\"labelKey\":\"referenceNo\",\"type\":\"textbox\",\"dataType\":\"String\",\"isRequired\":false,\"length\":\"1000\"},\"coverPercentage\":{\"coverPercentage\":\"Cover Percentage\",\"labelKey\":\"coverPercentage\",\"type\":\"textbox\",\"dataType\":\"String\",\"isRequired\":false,\"length\":\"2\"},\"maxPaymentTerm\":{\"maxPaymentTerm\":\"Max Payment Term\",\"labelKey\":\"maxPaymentTerm\",\"type\":\"textbox\",\"dataType\":\"String\",\"isRequired\":false,\"length\":\"1000\"},\"remarks\":{\"remarks\":\"Remarks\",\"labelKey\":\"remarks\",\"type\":\"textbox\",\"dataType\":\"String\",\"isRequired\":false,\"length\":\"1000\"},\"contractRefno\":{\"contractRefno\":\"Contract/Draft Ref No\",\"labelKey\":\"contractRefno\",\"type\":\"textbox\",\"dataType\":\"String\",\"isRequired\":false}},\"label\":\"Limit Maintainance\",\"lastModifiedBy\":\"\",\"lastModifiedOn\":\"\",\"name\":\"limitmaintenance\",\"sys__UUID\":\"26e782e8-89e5-40ea-a3be-63b320260b7d\",\"sys__createdBy\":\"admin\",\"sys__createdOn\":\"Thu Feb 07 19:21:17 UTC 2019\",\"tenantID\":\"\",\"type\":\"object\",\"uniqueFields\":[\"limitRefNo\",\"counterpartyGroupName\",\"creditLimitSource\",\"creditLimitType\"],\"version\":\"1\",\"versionHistory\":[{\"version\":\"\",\"fields\":\"[{}]\",\"additionalProperties\":{}}]},\"workflowTaskName\":\"editlimitlist\"}";
		Response workflowResponse = callAPI(Method.POST, "/workflow",
				generatePayloadFromString(workflowCallPayloadString));
		verify200OKResponse(workflowResponse);
		workflowResponse.then().assertThat().body("message", containsString("Limit got replaced successfully"));


		
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