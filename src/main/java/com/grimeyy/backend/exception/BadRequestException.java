package com.grimeyy.backend.exception;

public class BadRequestException extends RuntimeException {
	
	private static final long serialVersionUID = -6651988010563024131L;

	public BadRequestException(String message) {
		super(message);
	}

}
