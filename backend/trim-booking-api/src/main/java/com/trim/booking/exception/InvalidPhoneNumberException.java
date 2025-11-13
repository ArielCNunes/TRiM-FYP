package com.trim.booking.exception;

/**
 * Exception thrown when a phone number fails validation or normalization.
 */
public class InvalidPhoneNumberException extends RuntimeException {

    public InvalidPhoneNumberException(String message) {
        super(message);
    }

    public InvalidPhoneNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}

