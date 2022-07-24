package com.eka.connect.creditrisk.exception;

public class HeaderValidationException extends RuntimeException {
	 
	private static final long serialVersionUID = 4183321990683568936L;

	public HeaderValidationException() {
		super();
	}

	public HeaderValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public HeaderValidationException(String message) {
		super(message);
	}

	public HeaderValidationException(Throwable cause) {
		super(cause);
	}

}

