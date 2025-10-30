package com.trim.booking.exception;

/**
 * Thrown when a business logic conflict occurs (e.g., booking slot already taken).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

