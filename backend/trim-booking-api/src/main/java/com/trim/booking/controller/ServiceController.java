package com.trim.booking.controller;

import com.trim.booking.dto.service.ServiceRequest;
import com.trim.booking.dto.service.ServiceResponse;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.service.barber.ServicesOfferedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServicesOfferedService servicesOfferedService;

    public ServiceController(ServicesOfferedService servicesOfferedService) {
        this.servicesOfferedService = servicesOfferedService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse created = new ServiceResponse(servicesOfferedService.createService(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = servicesOfferedService.getAllServices()
                .stream()
                .map(ServiceResponse::new)
                .toList();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ServiceResponse>> getActiveServices() {
        List<ServiceResponse> services = servicesOfferedService.getActiveServices()
                .stream()
                .map(ServiceResponse::new)
                .toList();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return servicesOfferedService.getServiceById(id)
                .map(service -> ResponseEntity.ok(new ServiceResponse(service)))
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse updated = new ServiceResponse(servicesOfferedService.updateService(id, request));
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        servicesOfferedService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        servicesOfferedService.deactivateService(id);
        return ResponseEntity.noContent().build();
    }
}