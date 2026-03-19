package com.trim.booking.dto.barber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateBarberBreakRequest {
    @NotNull(message = "Barber ID is required")
    private Long barberId;

    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End time is required")
    private String endTime;

    private String label;

    private String dayOfWeek;

    // Constructors
    public CreateBarberBreakRequest() {
    }

    public CreateBarberBreakRequest(Long barberId, String startTime, String endTime, String label, String dayOfWeek) {
        this.barberId = barberId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public Long getBarberId() {
        return barberId;
    }

    public void setBarberId(Long barberId) {
        this.barberId = barberId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}

