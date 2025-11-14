package com.trim.booking.controller;

import com.trim.booking.entity.Barber;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.service.barber.BarberService;
import com.trim.booking.dto.barber.CreateBarberRequest;
import com.trim.booking.dto.barber.UpdateBarberRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {
    private final BarberService barberService;

    public BarberController(BarberService barberService) {
        this.barberService = barberService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Barber> createBarber(@Valid @RequestBody CreateBarberRequest request) {
        Barber barber = barberService.createBarber(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                request.getBio(),
                request.getProfileImageUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(barber);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Barber> updateBarber(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBarberRequest request) {
        Barber updated = barberService.updateBarber(
                id,
                request.getBio(),
                request.getProfileImageUrl()
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Barber>> getAllBarbers() {
        return ResponseEntity.ok(barberService.getAllBarbers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Barber>> getActiveBarbers() {
        return ResponseEntity.ok(barberService.getActiveBarbers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barber> getBarberById(@PathVariable Long id) {
        return barberService.getBarberById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateBarber(@PathVariable Long id) {
        barberService.deactivateBarber(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBarber(@PathVariable Long id) {
        barberService.deleteBarber(id);
        return ResponseEntity.noContent().build();
    }
}