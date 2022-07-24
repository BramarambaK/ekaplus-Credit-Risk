package com.eka.connect.creditrisk.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;

@Service
public class RestTemplateUtil {
	@Autowired
	private RequestContextHolder requestContextHolder;

	public HttpHeaders getCommonHttpHeaders() {

		HttpHeaders headers = new HttpHeaders();
		headers.add(CreditRiskConstants.AUTHORIZATION,
				requestContextHolder.getCurrentContext().getRequestHeaders().get("authorization"));
		headers.add(CreditRiskConstants.X_TENANT_ID, requestContextHolder.getCurrentContext().getTenantShortName());
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		headers.add(CreditRiskConstants.REQUEST_ID,requestContextHolder.getCurrentContext().getRequestId());


		return headers;
	}

}
