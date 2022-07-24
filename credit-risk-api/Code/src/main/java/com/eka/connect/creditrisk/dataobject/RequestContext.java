package com.eka.connect.creditrisk.dataobject;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext {

	private String tenantShortName;

	private String platformUrl;

	private Map<String, String> requestHeaders;

	private Map<String, Object> appProperties;

	private String requestId;
	
	private HttpServletRequest request;
	
	// contextMap is set when new Child() is called
    private Map<String,String> contextMap = MDC.getCopyOfContextMap();

	public RequestContext() {
		super();
	}

	public String getPlatformUrl() {
		return platformUrl;
	}

	public void setPlatformUrl(String platformUrl) {
		this.platformUrl = platformUrl;
	}

	public String getTenantShortName() {
		return tenantShortName;
	}

	public void setTenantShortName(String tenantShortName) {
		this.tenantShortName = tenantShortName;
	}

	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public Map<String, Object> getAppProperties() {
		return appProperties;
	}

	public void setAppProperties(Map<String, Object> appProperties) {
		this.appProperties = appProperties;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Map<String, String> getContextMap() {
		return contextMap;
	}

	public void setContextMap(Map<String, String> contextMap) {
		this.contextMap = contextMap;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
