package com.supermart.service;

/** Raised by the service layer when user input breaks a business rule. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
