package com.eka.connect.creditrisk.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.RequestContextHolder;

@Aspect
@Component
public class MDCAspect {
 
	@Before("execution(* com.eka.connect.creditrisk.service.*.*Async (..))")
	public void enableMDCContext(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		HttpHeaders headers = (HttpHeaders)args[0];
		MDC.put(CreditRiskConstants.REQUEST_ID,headers.getFirst(CreditRiskConstants.REQUEST_ID));
		MDC.put(CreditRiskConstants.TENANT_NAME, headers.getFirst(CreditRiskConstants.X_TENANT_ID));

	}

	@After("execution(* com.eka.connect.creditrisk.service.*.*Async (..))")
	public void removeMDCContext(JoinPoint joinPoint) {
		MDC.remove(CreditRiskConstants.REQUEST_ID);
		MDC.remove(CreditRiskConstants.TENANT_NAME);

	}

	@AfterThrowing("execution(* com.eka.connect.creditrisk.service.*.*Async (..))")
	public void afterThrowingMDCContext(JoinPoint joinPoint) {
		MDC.remove("requestId");

	}

}
