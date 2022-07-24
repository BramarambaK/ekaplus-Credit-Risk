package com.eka.connect.creditrisk.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.eka.connect.creditrisk.service.CreditStopEligibilityService;

/**
 * 
 * @author rajeshks
 *
 */
@RestController
@RequestMapping("/creditstop")
public class CreditStopEligibilityController {
	@Autowired
	CreditStopEligibilityService creditStopEligibilityService;

	private static final Logger LOGGER = ESAPI
			.getLogger(CreditStopEligibilityController.class);

	@PostMapping("/execute")
	public void execute() {
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Credit Stop Eligibility Service API started.."));

		creditStopEligibilityService.execute();
		
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Credit Stop Eligibility Service API completed.."));
	}
	
	@RequestMapping(method = {RequestMethod.HEAD,RequestMethod.GET},value="/execute")
	public String executeDummy(HttpServletRequest request, HttpServletResponse response) {

		JSONObject j = new JSONObject();
		j.put("test", "test");
		JSONArray jsonarray = new JSONArray();
		jsonarray.put(j);
		JSONObject j1 = new JSONObject();
		j1.put("data", jsonarray);
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Credit Stop Eligibility Service API started.."));

		response.setHeader("status", "success");
		
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Credit Stop Eligibility Service API completed.."));
		return j1.toString();
	}

}
