package com.eka.connect.creditrisk.exception;

public class TokenValidationException extends RuntimeException {

	private static final long serialVersionUID = 4183321990683568936L;

	public TokenValidationException() {
		super();
	}

	public TokenValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenValidationException(String message) {
		super(message);
	}

	public TokenValidationException(Throwable cause) {
		super(cause);
	}

}
