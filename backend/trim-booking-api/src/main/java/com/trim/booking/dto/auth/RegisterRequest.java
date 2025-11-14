package com.trim.booking.dto.auth;

import com.trim.booking.util.PhoneNumberUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

    // Constructors
    public RegisterRequest() {
    }

    // Custom validation method for phone number
    @AssertTrue(message = "Phone must be in valid international format")
    public boolean isPhoneValid() {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty
        }
        try {
            // If already looks like E.164, validate directly
            if (phone.trim().startsWith("+")) {
                return PhoneNumberUtil.validateE164Format(phone.trim());
            }
            // Otherwise, attempt normalization to see if it's valid
            // Don't store the result - just check if it can be normalized
            PhoneNumberUtil.normalizePhoneNumber(phone, "353");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}