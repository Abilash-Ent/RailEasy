package com.apexon.railEasy.exception;

/**
 * Thrown when authentication fails (bad credentials). Maps to HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

