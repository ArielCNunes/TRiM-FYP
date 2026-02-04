package com.trim.booking.controller;

import com.trim.booking.dto.barber.CreateBarberBreakRequest;
import com.trim.booking.dto.barber.UpdateBarberBreakRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberBreakRepository;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.service.barber.BarberBreakService;
import com.trim.booking.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/barber-breaks")
public class BarberBreakController {
    private final BarberBreakService barberBreakService;
    private final BarberRepository barberRepository;
    private final BarberBreakRepository barberBreakRepository;

    public BarberBreakController(BarberBreakService barberBreakService,
                                  BarberRepository barberRepository,
                                  BarberBreakRepository barberBreakRepository) {
        this.barberBreakService = barberBreakService;
        this.barberRepository = barberRepository;
        this.barberBreakRepository = barberBreakRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Get all breaks for a specific barber.
     *
     * @param barberId The ID of the barber
     * @return List of all breaks for the barber
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<?> getBreaksByBarberId(@PathVariable Long barberId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber is requesting their own breaks
        if (!isAdmin) {
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty() || !barberOpt.get().getId().equals(barberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only view your own breaks");
            }
        }

        List<BarberBreak> breaks = barberBreakService.getBreaksByBarberId(barberId);
        return ResponseEntity.ok(breaks);
    }

    /**
     * Create a new break for a barber.
     *
     * @param request The create break request containing barberId, startTime, endTime, and optional label
     * @return The created break with 201 status
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @PostMapping
    public ResponseEntity<?> createBreak(@Valid @RequestBody CreateBarberBreakRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber is creating their own break
        if (!isAdmin) {
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!barberOpt.get().getId().equals(request.getBarberId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only create breaks for yourself");
            }
        }

        BarberBreak barberBreak = barberBreakService.createBreak(
                request.getBarberId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLabel()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(barberBreak);
    }

    /**
     * Update an existing break.
     *
     * @param id The ID of the break to update
     * @param request The update break request containing startTime, endTime, and optional label
     * @return The updated break
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBreak(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBarberBreakRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber owns this break
        if (!isAdmin) {
            BarberBreak existingBreak = barberBreakRepository.findByIdAndBusinessId(id, getBusinessId())
                    .orElseThrow(() -> new ResourceNotFoundException("Break not found with id: " + id));

            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!existingBreak.getBarber().getId().equals(barberOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update your own breaks");
            }
        }

        BarberBreak barberBreak = barberBreakService.updateBreak(
                id,
                request.getStartTime(),
                request.getEndTime(),
                request.getLabel()
        );
        return ResponseEntity.ok(barberBreak);
    }

    /**
     * Delete a break.
     *
     * @param id The ID of the break to delete
     * @return 204 No Content
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBreak(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // If not admin, verify barber owns this break
        if (!isAdmin) {
            BarberBreak existingBreak = barberBreakRepository.findByIdAndBusinessId(id, getBusinessId())
                    .orElseThrow(() -> new ResourceNotFoundException("Break not found with id: " + id));

            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(
                    getBusinessId(), authenticatedUserId);

            if (barberOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not a barber in this business");
            }

            if (!existingBreak.getBarber().getId().equals(barberOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own breaks");
            }
        }

        barberBreakService.deleteBreak(id);
        return ResponseEntity.noContent().build();
    }
}

