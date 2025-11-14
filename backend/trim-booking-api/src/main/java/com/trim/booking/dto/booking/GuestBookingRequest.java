package com.trim.booking.dto.booking;

import com.trim.booking.util.PhoneNumberUtil;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class GuestBookingRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Barber ID is required")
    private Long barberId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private String paymentMethod;

    private String notes;

    // Constructors
    public GuestBookingRequest() {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

