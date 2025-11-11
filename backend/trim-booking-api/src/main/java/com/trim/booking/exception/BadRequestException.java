package com.trim.booking.exception;

/**
 * Thrown when a request contains invalid data or violates business rules.
 * Maps to HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

