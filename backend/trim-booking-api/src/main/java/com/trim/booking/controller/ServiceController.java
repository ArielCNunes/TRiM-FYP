package com.trim.booking.controller;

import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.service.ServicesOfferedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServicesOfferedService servicesOfferedService;

    public ServiceController(ServicesOfferedService servicesOfferedService) {
        this.servicesOfferedService = servicesOfferedService;
    }

    @PostMapping
    public ResponseEntity<ServiceOffered> createService(@Valid @RequestBody ServiceOffered service) {
        ServiceOffered created = servicesOfferedService.createService(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ServiceOffered>> getAllServices() {
        return ResponseEntity.ok(servicesOfferedService.getAllServices());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ServiceOffered>> getActiveServices() {
        return ResponseEntity.ok(servicesOfferedService.getActiveServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOffered> getServiceById(@PathVariable Long id) {
        return servicesOfferedService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOffered> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceOffered service) {
        try {
            ServiceOffered updated = servicesOfferedService.updateService(id, service);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        servicesOfferedService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        try {
            servicesOfferedService.deactivateService(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}