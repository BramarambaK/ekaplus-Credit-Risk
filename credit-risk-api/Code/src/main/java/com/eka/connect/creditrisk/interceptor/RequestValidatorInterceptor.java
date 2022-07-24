package com.eka.connect.creditrisk.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.PropertyConstants;
import com.eka.connect.creditrisk.dataobject.RequestContext;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;
import com.eka.connect.creditrisk.exception.HeaderValidationException;
import com.eka.connect.creditrisk.http.HttpProperties;
import com.eka.connect.creditrisk.service.ConnectRestTemplate;

@Component
@Order(2)
public class RequestValidatorInterceptor implements AsyncHandlerInterceptor {
	
	@Value("${eka_connect_host}")
	private String ekaConnectHost;
 
	@Autowired
	private RequestContextHolder requestContextHolder;

	@Autowired
	private RequestContext requestContext;	
	
	@Autowired
	ConnectRestTemplate connectRestTemplate;
	
	@Autowired
	HttpProperties httpProperties;
	

	private static final Logger LOGGER = ESAPI
			.getLogger(RequestValidatorInterceptor.class);

	public static final String REGEX_DOT = "\\.";

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		setTenantNameAndRequestIdToLogAndContext(request); 

		String requestURI = request.getRequestURI();
		String requestMethod = request.getMethod();
		
		RequestResponseLogger.logRequest(request);
		
		LOGGER.info(Logger.EVENT_SUCCESS, "********* Credit Risk api PreHandle Started......"+"Request Details: " + requestMethod + " " + requestURI);
		
		
		validateHeaders(request);
		requestContextHolder.setCurrentContext(requestContext);
		requestContextHolder.getCurrentContext().setRequest(request);
		requestContext.setContextMap(MDC.getCopyOfContextMap());
		setAppProperties();
		//validateToken(request);
		//setPlatformUrlToContext();
		requestContext.setPlatformUrl((String)requestContext.getAppProperties().get(PropertyConstants.PLATRORM_URL));
		
		LOGGER.info(Logger.EVENT_SUCCESS, "********* Credit Risk api PreHandle completed......"+"Request Details: " + requestMethod + " " + requestURI);

		
		return true;
	}

	private void setAppProperties() {
		Map<String,Object> appProperties  = connectRestTemplate
		.getAppProperties();
		requestContext.setAppProperties(appProperties);
		
	}

	private void validateHeaders(HttpServletRequest request) {

		if (StringUtils.isEmpty(request.getHeader(CreditRiskConstants.AUTHORIZATION))) {
			throw new HeaderValidationException(
					"Authorization Header is missing");
		}
		Map<String, String> headers = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, request.getHeader(headerName));
			
		}
		requestContext.setRequestHeaders(headers);
		requestContext.setTenantShortName(headers.get("x-tenantid"));
		if (null == requestContext.getTenantShortName()) {
			requestContext.setTenantShortName(request.getServerName().split(
					"\\.")[0]);
		}

	}

	public void adduserToRequest(HttpServletRequest request) {
		if (requestContext != null) {
		}
	}
	
	@Override
	public  void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		RequestResponseLogger.logResponseHeaders(response);
 
	}
	
	@Override
	public  void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		String requestURI = request.getRequestURI();
		String requestMethod = request.getMethod();
		response.addHeader(CreditRiskConstants.REQUEST_ID, requestContextHolder
				.getCurrentContext().getRequestId());
		if (ex != null) {
			RequestResponseLogger.logResponseHeaderDetails(response);
		}

		LOGGER.info(Logger.EVENT_SUCCESS,
				"********* Credit Risk api User Request completed......"
						+ "Request Details: " + requestMethod + " "
						+ requestURI);

		requestContextHolder.getCurrentContext().getContextMap().clear();
		MDC.clear();
		requestContextHolder.remove();

	}
	
	private void setTenantNameAndRequestIdToLogAndContext(HttpServletRequest request) {
		String requestId = null;
		String tenantName = null;
		if (null != request.getHeader(CreditRiskConstants.REQUEST_ID)) {
			requestId = request.getHeader(CreditRiskConstants.REQUEST_ID);
		} else {
			requestId = UUID.randomUUID().toString().replace("-", "")+"-GEN";

		}

		if (null == request.getHeader(CreditRiskConstants.X_TENANT_ID)) {
			tenantName = request.getServerName();
			tenantName = tenantName.split(REGEX_DOT)[0];
		}
		else{
		tenantName = request.getHeader(CreditRiskConstants.X_TENANT_ID);
		}

		MDC.put(CreditRiskConstants.REQUEST_ID, requestId);
		requestContext.setRequestId(requestId);
		
		MDC.put(CreditRiskConstants.TENANT_NAME, tenantName);

	}
}
