package com.trim.booking.exception;

/**
 * Thrown when an action is forbidden due to business rules.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

