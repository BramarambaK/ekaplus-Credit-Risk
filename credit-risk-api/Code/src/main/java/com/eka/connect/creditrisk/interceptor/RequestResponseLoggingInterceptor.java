package com.eka.connect.creditrisk.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Order(1)
public class RequestResponseLoggingInterceptor implements
		AsyncHandlerInterceptor {

	private static final Logger LOGGER = ESAPI
			.getLogger(RequestResponseLoggingInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Request api path :: " + request.getRequestURI()
								+ " Initiated..."));
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		LOGGER.info(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Request api path :: " + request.getRequestURI()
								+ " completed..."));

	}

}
