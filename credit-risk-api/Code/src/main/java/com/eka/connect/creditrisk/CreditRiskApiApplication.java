package com.eka.connect.creditrisk;

import javax.annotation.PostConstruct;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import com.eka.connect.creditrisk.http.HttpProperties;

@SpringBootApplication
//@EnableScheduling
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
public class CreditRiskApiApplication {
	private static final Logger LOGGER = ESAPI
			.getLogger(CreditRiskApiApplication.class);

	@Autowired
	private HttpProperties httpProperties;
	
	public static void main(String[] args) {
		LOGGER.info(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Credit Risk Application Main class initiation started..................."));

		SpringApplication.run(CreditRiskApiApplication.class, args);
	}

	@PostConstruct
	public void postConstruct() {
		LOGGER.info(Logger.EVENT_SUCCESS,"Credit Risk Application Main class initiated successfully..................");
	}
	
	@Bean
	public RestTemplate createRestTemplateBean(){
		RestTemplate  restTemplate  =  new RestTemplate();
		restTemplate.setRequestFactory(getClientHttpRequestFactory(httpProperties.getHttpConnectionTimeOut(),
				httpProperties.getHttpReadTimeOut()));
		return restTemplate;
	}


	// https://stackoverflow.com/questions/45713767/spring-rest-template-readtimeout
	public static ClientHttpRequestFactory getClientHttpRequestFactory(
			int httpConnectionTimeOut, int httpReadTimeOut) {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(httpConnectionTimeOut);

		// Read timeout
		clientHttpRequestFactory.setReadTimeout(httpReadTimeOut);
		return clientHttpRequestFactory;
	}
}
