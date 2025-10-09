package com.trim.booking.dto;

public class UpdateBarberRequest {
    private String bio;
    private String profileImageUrl;

    // Constructors
    public UpdateBarberRequest() {
    }

    // Getters and Setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}