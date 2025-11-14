package com.trim.booking.dto.auth;

/**
 * Generic response DTO for password reset operations.
 */
public class PasswordResetResponse {

    private String message;

    // Constructors
    public PasswordResetResponse() {
    }

    public PasswordResetResponse(String message) {
        this.message = message;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

