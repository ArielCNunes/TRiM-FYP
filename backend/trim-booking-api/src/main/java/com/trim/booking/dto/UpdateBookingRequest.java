package com.trim.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for updating an existing booking.
 * Only allows changing date and time to avoid payment/refund complexity.
 */
public class UpdateBookingRequest {
    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    // Constructors
    public UpdateBookingRequest() {
    }

    public UpdateBookingRequest(LocalDate bookingDate, LocalTime startTime) {
        this.bookingDate = bookingDate;
        this.startTime = startTime;
    }

    // Getters and Setters
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
}

