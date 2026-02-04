package com.trim.booking.controller;

import com.trim.booking.entity.BarberAvailability;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Business;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberAvailabilityRepository;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/barber-availability")
public class BarberAvailabilityController {
    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final BarberRepository barberRepository;
    private final BusinessRepository businessRepository;

    public BarberAvailabilityController(BarberAvailabilityRepository barberAvailabilityRepository,
                                        BarberRepository barberRepository,
                                        BusinessRepository businessRepository) {
        this.barberAvailabilityRepository = barberAvailabilityRepository;
        this.barberRepository = barberRepository;
        this.businessRepository = businessRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Set availability for a barber on a specific day of the week.
     * <p>
     * Example: POST /api/barber-availability
     * Body: { "barberId": 1, "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00", "isAvailable": true }
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @PostMapping
    public ResponseEntity<?> setAvailability(@RequestBody Map<String, Object> request) {
        Long businessId = getBusinessId();
        Long barberId = Long.valueOf(request.get("barberId").toString());

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber is setting their own availability
        if (!isAdmin) {
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(businessId, authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!barberOpt.get().getId().equals(barberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only set your own availability");
            }
        }

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.get("dayOfWeek").toString());
        LocalTime startTime = LocalTime.parse(request.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(request.get("endTime").toString());
        Boolean isAvailable = Boolean.valueOf(request.get("isAvailable").toString());

        // Get barber with tenant isolation
        Barber barber = barberRepository.findByIdAndBusinessId(barberId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + barberId));

        // Get business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        // Create availability
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(barber);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setIsAvailable(isAvailable);
        availability.setBusiness(business);

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
    public ResponseEntity<?> updateAvailability(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BarberAvailability availability = barberAvailabilityRepository.findByIdAndBusinessId(id, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber owns this availability record
        if (!isAdmin) {
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!availability.getBarber().getId().equals(barberOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update your own availability");
            }
        }

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
        List<BarberAvailability> availability = barberAvailabilityRepository.findByBusinessIdAndBarberId(getBusinessId(), barberId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Get all availability records.
     */
    @GetMapping
    public ResponseEntity<List<BarberAvailability>> getAllAvailability() {
        return ResponseEntity.ok(barberAvailabilityRepository.findByBarberBusinessId(getBusinessId()));
    }

    /**
     * Delete availability record.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        BarberAvailability availability = barberAvailabilityRepository.findByIdAndBusinessId(id, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber owns this availability record
        if (!isAdmin) {
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!availability.getBarber().getId().equals(barberOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own availability");
            }
        }

        barberAvailabilityRepository.delete(availability);
        return ResponseEntity.noContent().build();
    }
}