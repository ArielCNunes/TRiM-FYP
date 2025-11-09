package com.trim.booking.controller;

import com.trim.booking.entity.BarberAvailability;
import com.trim.booking.entity.Barber;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberAvailabilityRepository;
import com.trim.booking.repository.BarberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barber-availability")
public class BarberAvailabilityController {
    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final BarberRepository barberRepository;

    public BarberAvailabilityController(BarberAvailabilityRepository barberAvailabilityRepository,
                                        BarberRepository barberRepository) {
        this.barberAvailabilityRepository = barberAvailabilityRepository;
        this.barberRepository = barberRepository;
    }

    /**
     * Set availability for a barber on a specific day of the week.
     * <p>
     * Example: POST /api/barber-availability
     * Body: { "barberId": 1, "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00", "isAvailable": true }
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @PostMapping
    public ResponseEntity<BarberAvailability> setAvailability(@RequestBody Map<String, Object> request) {
        Long barberId = Long.valueOf(request.get("barberId").toString());
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.get("dayOfWeek").toString());
        LocalTime startTime = LocalTime.parse(request.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(request.get("endTime").toString());
        Boolean isAvailable = Boolean.valueOf(request.get("isAvailable").toString());

        // Get barber
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + barberId));

        // Create availability
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(barber);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setIsAvailable(isAvailable);

        BarberAvailability saved = barberAvailabilityRepository.save(availability);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update barber availability.
     * <p>
     * PUT /api/barber-availability/{id}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @PutMapping("/{id}")
    public ResponseEntity<BarberAvailability> updateAvailability(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BarberAvailability availability = barberAvailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Update fields if provided
        if (request.containsKey("dayOfWeek")) {
            availability.setDayOfWeek(DayOfWeek.valueOf(request.get("dayOfWeek").toString()));
        }
        if (request.containsKey("startTime")) {
            availability.setStartTime(LocalTime.parse(request.get("startTime").toString()));
        }
        if (request.containsKey("endTime")) {
            availability.setEndTime(LocalTime.parse(request.get("endTime").toString()));
        }
        if (request.containsKey("isAvailable")) {
            availability.setIsAvailable(Boolean.valueOf(request.get("isAvailable").toString()));
        }

        BarberAvailability updated = barberAvailabilityRepository.save(availability);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get all availability for a barber.
     */
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<BarberAvailability>> getBarberAvailability(@PathVariable Long barberId) {
        List<BarberAvailability> availability = barberAvailabilityRepository.findByBarberId(barberId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Get all availability records.
     */
    @GetMapping
    public ResponseEntity<List<BarberAvailability>> getAllAvailability() {
        return ResponseEntity.ok(barberAvailabilityRepository.findAll());
    }

    /**
     * Delete availability record.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        barberAvailabilityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}