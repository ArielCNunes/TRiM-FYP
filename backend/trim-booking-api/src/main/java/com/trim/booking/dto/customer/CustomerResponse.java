package com.trim.booking.dto.customer;

import java.time.LocalDateTime;

/**
 * Response DTO for customer details.
 */
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private Boolean blacklisted;
    private String blacklistReason;
    private LocalDateTime blacklistedAt;
    private Long noShowCount;

    // Constructors
    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String firstName, String lastName, String email, String phone,
                            LocalDateTime createdAt, Boolean blacklisted, String blacklistReason,
                            LocalDateTime blacklistedAt, Long noShowCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.blacklisted = blacklisted;
        this.blacklistReason = blacklistReason;
        this.blacklistedAt = blacklistedAt;
        this.noShowCount = noShowCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(Boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public String getBlacklistReason() {
        return blacklistReason;
    }

    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }

    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(LocalDateTime blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public Long getNoShowCount() {
        return noShowCount;
    }

    public void setNoShowCount(Long noShowCount) {
        this.noShowCount = noShowCount;
    }
}

