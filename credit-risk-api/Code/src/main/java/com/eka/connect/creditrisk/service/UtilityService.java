package com.eka.connect.creditrisk.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UtilityService {

	@Value("${currency_locale:en}")
	private String currencyFormat;

	private Locale currencylocale = null;

	private static final Logger LOGGER = ESAPI
			.getLogger(UtilityService.class);

	@PostConstruct
	public void initLocale() {
		if (currencylocale == null) {
			LOGGER.info(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Initializing Currency Locale.."));
			currencylocale = new Locale(currencyFormat);
			LOGGER.info(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Completed Currency Locale initialization to "
							+ currencylocale.toString())); 
		}

	}

	public Locale getCurrencyLocale() {
		return this.currencylocale;
	}
	
	public static String convertExceptionStackTraceToString(Throwable e) {

		if (e != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString(); // stack trace as a string
			return sStackTrace.length()>1000?sStackTrace.substring(0, 1000):sStackTrace;
		}
		return null;
	}
	

}
