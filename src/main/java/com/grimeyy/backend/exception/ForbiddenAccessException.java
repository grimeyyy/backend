package com.grimeyy.backend.exception;

public class ForbiddenAccessException extends RuntimeException {

	private static final long serialVersionUID = 2496126315461539157L;

	public ForbiddenAccessException(String message) {
        super(message);
    }
}