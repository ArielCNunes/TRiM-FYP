package com.trim.booking.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for forgot password request.
 */
public class ForgotPasswordRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    // Optional - can be provided explicitly or resolved from subdomain
    private String businessSlug;

    // Constructors
    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String email, String businessSlug) {
        this.email = email;
        this.businessSlug = businessSlug;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBusinessSlug() {
        return businessSlug;
    }

    public void setBusinessSlug(String businessSlug) {
        this.businessSlug = businessSlug;
    }
}
