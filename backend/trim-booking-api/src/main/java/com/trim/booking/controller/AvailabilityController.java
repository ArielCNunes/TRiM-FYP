package com.trim.booking.controller;

import com.trim.booking.service.barber.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Get available time slots for a barber on a specific date for a given service.
     * <p>
     * Example: GET /api/availability?barberId=1&date=2025-10-15&serviceId=1
     *
     * @param barberId  The ID of the barber
     * @param date      The date to check
     * @param serviceId The ID of the service
     * @return List of available time slots
     */
    @GetMapping
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {
        List<String> slots = availabilityService.getAvailableSlots(barberId, date, serviceId);
        return ResponseEntity.ok(slots);
    }
}