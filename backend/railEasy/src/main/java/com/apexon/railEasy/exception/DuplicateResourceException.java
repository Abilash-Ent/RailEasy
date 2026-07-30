package com.apexon.railEasy.exception;

/**
 * Thrown when a request conflicts with the current state (e.g. duplicate email).
 * Maps to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

