package com.trim.booking.dto.auth;

public class SaveAccountResponse {
    private Long userId;
    private String email;
    private String message;

    public SaveAccountResponse(Long userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}

