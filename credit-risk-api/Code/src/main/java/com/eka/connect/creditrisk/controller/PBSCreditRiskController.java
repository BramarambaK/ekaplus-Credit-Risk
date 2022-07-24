package com.eka.connect.creditrisk.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eka.connect.creditrisk.dataobject.ApiError;
import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.dataobject.PBSCreditCheckRequest;
import com.eka.connect.creditrisk.service.PBSCreditCheckService;


@RestController
@RequestMapping(value = "/pbs")
@Api(value = "Plan Bulk Shipment Credit Risk Controller", description = "REST APIs related to Credit Risk!!!!")

public class PBSCreditRiskController {
	

	
	@Autowired
	@Qualifier("pbsCreditCheckService")
	PBSCreditCheckService pbsCreditCheckService;
	
	private static final Logger LOGGER = ESAPI
			.getLogger(PBSCreditRiskController.class);

	@PostMapping(value = "/creditcheck")
	@ApiOperation(value = "Does Credit Check")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ItemResponse.class),
			 @ApiResponse(code = 400, message = "Bad Request",response =ApiError.class) })
	
	public ItemResponse creditCheck(
			HttpServletRequest request,
			@Valid @RequestBody PBSCreditCheckRequest pbsCreditCheckRequest)
			throws Exception {
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("creditCheck initiated with request \n "+pbsCreditCheckRequest));

		// Step1 :populate CreditCheckRequest from ContractCreditCheckRequest
		// object
		ItemResponse creditCheckResponse = pbsCreditCheckService
				.doCreditCheck(pbsCreditCheckRequest);
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("creditCheck completed with Response \n "+creditCheckResponse));
		return creditCheckResponse;
	}
	


}
