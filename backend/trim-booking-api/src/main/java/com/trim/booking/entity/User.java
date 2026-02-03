package com.trim.booking.entity;

import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.InvalidPhoneNumberException;
import com.trim.booking.util.PhoneNumberUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    @Column
    private Long resetTokenBusinessId;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean blacklisted = false;

    @Column(length = 500)
    private String blacklistReason;

    @Column
    private LocalDateTime blacklistedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnore
    private Business business;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        normalizePhoneBeforePersist();
    }

    @PreUpdate
    protected void onUpdate() {
        normalizePhoneBeforeUpdate();
    }

    private void normalizePhoneBeforePersist() {
        if (phone != null && !phone.trim().isEmpty()) {
            try {
                phone = PhoneNumberUtil.normalizePhoneNumber(phone, "353");
            } catch (InvalidPhoneNumberException e) {
                throw new BadRequestException("Invalid phone number format - must be valid international number: " + e.getMessage());
            }
        }
    }

    private void normalizePhoneBeforeUpdate() {
        if (phone != null && !phone.trim().isEmpty()) {
            try {
                phone = PhoneNumberUtil.normalizePhoneNumber(phone, "353");
            } catch (InvalidPhoneNumberException e) {
                throw new BadRequestException("Invalid phone number format - must be valid international number: " + e.getMessage());
            }
        }
    }

    // Constructors
    public User() {
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

    @JsonIgnore // never include passwordHash in JSON responses
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public Long getResetTokenBusinessId() {
        return resetTokenBusinessId;
    }

    public void setResetTokenBusinessId(Long resetTokenBusinessId) {
        this.resetTokenBusinessId = resetTokenBusinessId;
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

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    // Role Enum
    public enum Role {
        CUSTOMER,
        BARBER,
        ADMIN
    }
}