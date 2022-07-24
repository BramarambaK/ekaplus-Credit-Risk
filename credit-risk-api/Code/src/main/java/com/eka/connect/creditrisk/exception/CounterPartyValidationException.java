package com.eka.connect.creditrisk.exception;

public class CounterPartyValidationException extends RuntimeException {

	private static final long serialVersionUID = 4183321990683568936L;

	public CounterPartyValidationException() {
		super();
	}

	public CounterPartyValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CounterPartyValidationException(String message) {
		super(message);
	}

	public CounterPartyValidationException(Throwable cause) {
		super(cause);
	}

}
