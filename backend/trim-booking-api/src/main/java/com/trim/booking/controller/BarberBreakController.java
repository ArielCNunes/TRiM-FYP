package com.trim.booking.controller;

import com.trim.booking.dto.barber.CreateBarberBreakRequest;
import com.trim.booking.dto.barber.UpdateBarberBreakRequest;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.service.barber.BarberBreakService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barber-breaks")
public class BarberBreakController {
    private final BarberBreakService barberBreakService;

    public BarberBreakController(BarberBreakService barberBreakService) {
        this.barberBreakService = barberBreakService;
    }

    /**
     * Get all breaks for a specific barber.
     *
     * @param barberId The ID of the barber
     * @return List of all breaks for the barber
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBER')")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<BarberBreak>> getBreaksByBarberId(@PathVariable Long barberId) {
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
    public ResponseEntity<BarberBreak> createBreak(@Valid @RequestBody CreateBarberBreakRequest request) {
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
    public ResponseEntity<BarberBreak> updateBreak(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBarberBreakRequest request) {
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
    public ResponseEntity<Void> deleteBreak(@PathVariable Long id) {
        barberBreakService.deleteBreak(id);
        return ResponseEntity.noContent().build();
    }
}

