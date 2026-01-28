package com.trim.booking.dto.auth;

public class LoginResponse {
    private Long id;
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long barberId;
    private String businessSlug;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(Long id, String token, String email, String firstName, String lastName, String role, Long barberId, String businessSlug) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.barberId = barberId;
        this.businessSlug = businessSlug;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public String getBusinessSlug() {
        return businessSlug;
    }

    public void setBusinessSlug(String businessSlug) {
        this.businessSlug = businessSlug;
    }
}