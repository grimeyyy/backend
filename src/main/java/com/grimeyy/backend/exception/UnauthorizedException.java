package com.grimeyy.backend.exception;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = -2545314623990048216L;
	
	public UnauthorizedException(String message) {
		super(message);
	}

}
