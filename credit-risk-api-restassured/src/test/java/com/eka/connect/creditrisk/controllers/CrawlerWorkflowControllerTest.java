package com.eka.connect.creditrisk.controllers;

import static io.restassured.RestAssured.given;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CrawlerWorkflowControllerTest {

	String token = null;
	String tenant = null;
	String userName = null;
	String password = null;
	String eka_connect_host = null;
	String creditrisk_api_host = null;
	Map<String, Object> requestPayload = new HashMap<String, Object>();

	private static final String tokenGenerationApiPath = "/api/authenticate";

	@BeforeTest
	public void setUp() throws Exception {

		Properties prop = new Properties();
		prop.load(new FileInputStream(ResourceUtils
				.getFile("classpath:RestAssuredTest.properties")));
		tenant = prop.getProperty("tenant");
		userName = prop.getProperty("userName");
		password = prop.getProperty("password");
		eka_connect_host = (String) prop.getProperty("eka_connect_host");
		URL url = new URL(eka_connect_host);
		RestAssured.baseURI = "http://" + url.getHost();
		RestAssured.port = url.getPort();
		token = authenticateUser(userName, password);
	}

	@Test(enabled = true)
	public void testCrawlerWorkflow() throws Exception {
		// read the json
		FileInputStream fileInputStream = new FileInputStream(
				ResourceUtils.getFile("classpath:crawlerworkflow.json"));
		Map<String, Object> jsonMap = new JSONObject(new JSONTokener(
				fileInputStream)).toMap();
		executeAPIandAssert(jsonMap, "/workflow");
	}

	private String authenticateUser(String username, String password)
			throws UnsupportedEncodingException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("userName", username);
		body.put("password", password);
		String base64encodedUsernamePassword = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes("utf-8"));
		Response response = given()
				.header("Content-Type", "application/json")
				.header("Authorization",
						"Basic " + base64encodedUsernamePassword)
				.header("X-TenantID", tenant).body(body).when()
				.post(tokenGenerationApiPath);
		JsonPath jsonPath = new JsonPath(response.asString());
		return jsonPath.getString("auth2AccessToken.access_token");
	}

	private void executeAPIandAssert(Map<String, Object> jsonMap, String path) {
		Response appMetaResponse = callAPI(Method.POST, path, jsonMap);
		verify200OKResponse(appMetaResponse);

	}

	private void verify200OKResponse(Response response) {
		System.out.println(response.body().asString());
		Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
	}

	private Response callAPI(Method httpMethod, String path,
			Map<String, Object> payload) {
		switch (httpMethod) {
		case GET:
			return given().log().all().header("Authorization", token)
					.header("X-TenantID", tenant)
					.header("Content-Type", "application/json").when()
					.request(httpMethod.name(), path);
		case POST:
		case PUT:
		case DELETE:
			return given().log().all().header("Authorization", token)
					.header("X-TenantID", tenant)
					.header("Content-Type", "application/json").with()
					.body(payload).when().request(httpMethod.name(), path);

		default:
			return null;
		}
	}

}
