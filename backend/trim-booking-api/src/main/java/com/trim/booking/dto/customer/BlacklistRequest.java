package com.trim.booking.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for blacklisting a customer.
 */
public class BlacklistRequest {
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    // Constructors
    public BlacklistRequest() {
    }

    public BlacklistRequest(String reason) {
        this.reason = reason;
    }

    // Getters and Setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

