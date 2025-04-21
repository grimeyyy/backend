package com.grimeyy.backend.exception;

public class DuplicateEmailException extends RuntimeException {

	private static final long serialVersionUID = 3687654120805151040L;

	public DuplicateEmailException(String message) {
        super(message);
    }
}
