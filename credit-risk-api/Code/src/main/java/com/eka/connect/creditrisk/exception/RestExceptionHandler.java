package com.eka.connect.creditrisk.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.eka.connect.creditrisk.dataobject.ApiError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	// other exception handlers
	
	private static final Logger logger = LoggerFactory
			.getLogger(RestExceptionHandler.class);

	@ExceptionHandler(TokenValidationException.class)
	protected ResponseEntity<Object> tokenValidationExcepiton(
			TokenValidationException ex) {

		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(HeaderValidationException.class)
	protected ResponseEntity<Object> headerValidationException(
			HeaderValidationException ex) {
		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(PropertyNotFoundException.class)
	protected ResponseEntity<Object> handlePropertyNotFoundException(
			HeaderValidationException ex) {
		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(PlatformException.class)
	protected ResponseEntity<Object> handlePlatformException(
			PlatformException pe) {
		Map<String, String> map = new HashMap<>();
		map.put("message", pe.getMessage());
		return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		List<String> errors = new ArrayList<String>();
		 List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		 for (FieldError fieldError : fieldErrors) {
			 errors.add(fieldError.getDefaultMessage());
		}
		 
		 for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
	            errors.add( objectError.getDefaultMessage());
	        }
		ApiError apiError = new ApiError();
		apiError.setMessage(errors);
		logger.debug("MethodArgumentNotValidException details... [{}]", errors);
		return new ResponseEntity<Object>(apiError, status);
		}

	@ExceptionHandler(RuntimeException.class)
	protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
		logger.error("RuntimeException ...........", ex);
		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<Object> handleException(Exception ex) {
		logger.error("Exception ...........", ex);
		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(HttpClientErrorException.class)
	protected ResponseEntity<Object> handleRuntimeException(
			HttpClientErrorException ex) {
		logger.error(ex.getResponseBodyAsString(),
				ex);ex.getResponseBodyAsString();
		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, ex.getStatusCode());
	}
	
	@ExceptionHandler(CompletionException.class)
	protected ResponseEntity<Object> handleCompletionException(
			CompletionException ex) {
		logger.error(ex.getMessage(), ex);
		Throwable t = ex.getCause();
		if (t != null && t instanceof HttpClientErrorException) {
			HttpClientErrorException he = (HttpClientErrorException) ex
					.getCause();
			logger.error(he.getResponseBodyAsString(), he);

			Map<String, String> map = new HashMap<>();
			map.put("message", he.getResponseBodyAsString());
			return new ResponseEntity<>(map, he.getStatusCode());
		}

		Map<String, String> map = new HashMap<>();
		map.put("message", ex.getMessage());
		return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
