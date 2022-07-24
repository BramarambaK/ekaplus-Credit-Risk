package com.eka.connect.creditrisk.dataobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestContextHolder {

	private static Logger logger = LoggerFactory
			.getLogger(RequestContextHolder.class.getName());

	private ThreadLocal<RequestContext> currentContext = new ThreadLocal<>();

	public void setCurrentContext(RequestContext contextInfo) {
		currentContext.set(contextInfo);
	}

	public RequestContext getCurrentContext() {
		return currentContext.get();
	}

	public void remove() {
		currentContext.remove();
	}

}
