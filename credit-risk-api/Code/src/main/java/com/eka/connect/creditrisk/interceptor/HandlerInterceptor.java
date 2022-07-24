package com.eka.connect.creditrisk.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Component
public class HandlerInterceptor {

	@Bean
	public MappedInterceptor getReguestValidatorInterceptor(
			RequestValidatorInterceptor requestValidatorInterceptor) {
		return new MappedInterceptor(new String[] {
				"/prepayment/creditcheck/**", "/pbs/creditcheck/**",
				"/movement/creditcheck/**", "/invoice/creditcheck/**",
				"/contract/creditcheck/**", "/contract/exposure/**",
				"/creditstop/**","/healthcheck/**" }, new String[] { "/common/getManifestInfo","/logger/**" },
				requestValidatorInterceptor);
	}

	@Bean
	@Autowired
	public MappedInterceptor getReguestResponseLoggingInterceptor(
			RequestResponseLoggingInterceptor requestResponseLoggingInterceptor) {
		return new MappedInterceptor(new String[] { "/**" },
				requestResponseLoggingInterceptor);
	}
}
