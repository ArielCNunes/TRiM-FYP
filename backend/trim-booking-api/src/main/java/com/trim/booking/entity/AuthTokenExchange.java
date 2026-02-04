package com.trim.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Temporary token for secure cross-subdomain authentication.
 * These tokens are single-use and expire quickly (60 seconds).
 */
@Entity
@Table(name = "auth_token_exchanges")
public class AuthTokenExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short-lived random token (UUID) used in URL redirect.
     * This is NOT the JWT - it's exchanged for the JWT.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String exchangeToken;

    /**
     * The actual JWT token to be retrieved after exchange.
     */
    @Column(nullable = false, length = 2048)
    private String jwtToken;

    /**
     * User data as JSON string.
     */
    @Column(nullable = false, length = 1024)
    private String userData;

    /**
     * Business slug for the redirect target.
     */
    @Column(nullable = false, length = 100)
    private String businessSlug;

    /**
     * When this exchange token expires (60 seconds from creation).
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been used (single-use).
     */
    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExchangeToken() { return exchangeToken; }
    public void setExchangeToken(String exchangeToken) { this.exchangeToken = exchangeToken; }

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }

    public String getUserData() { return userData; }
    public void setUserData(String userData) { this.userData = userData; }

    public String getBusinessSlug() { return businessSlug; }
    public void setBusinessSlug(String businessSlug) { this.businessSlug = businessSlug; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
