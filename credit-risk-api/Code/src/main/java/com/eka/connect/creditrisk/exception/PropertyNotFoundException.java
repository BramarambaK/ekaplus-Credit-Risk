package com.eka.connect.creditrisk.exception;


public class PropertyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PropertyNotFoundException() {
		super();
	}

	public PropertyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyNotFoundException(String message) {
		super(message);
	}

	public PropertyNotFoundException(Throwable cause) {
		super(cause);
	}
}
