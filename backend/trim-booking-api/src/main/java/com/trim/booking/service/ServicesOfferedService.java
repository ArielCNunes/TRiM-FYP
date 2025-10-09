package com.trim.booking.service;

import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicesOfferedService {
    // Service layer for managing services offered
    private final ServiceRepository serviceRepository;

    public ServicesOfferedService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ServiceOffered createService(ServiceOffered service) {
        return serviceRepository.save(service);
    }

    public List<ServiceOffered> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<ServiceOffered> getActiveServices() {
        return serviceRepository.findByActiveTrue();
    }

    public Optional<ServiceOffered> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public ServiceOffered updateService(Long id, ServiceOffered updatedService) {
        // Update only allowed fields
        return serviceRepository.findById(id)
                .map(service -> {
                    service.setName(updatedService.getName());
                    service.setDescription(updatedService.getDescription());
                    service.setDurationMinutes(updatedService.getDurationMinutes());
                    service.setPrice(updatedService.getPrice());
                    service.setActive(updatedService.getActive());
                    return serviceRepository.save(service);
                })
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public void deactivateService(Long id) {
        // Soft delete by setting active to false
        serviceRepository.findById(id)
                .map(service -> {
                    service.setActive(false);
                    return serviceRepository.save(service);
                })
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }
}