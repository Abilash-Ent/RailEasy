package com.apexon.railEasy.exception;

/**
 * Thrown when a request is semantically invalid (e.g. not enough seats).
 * Maps to HTTP 400.
 */
public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}

