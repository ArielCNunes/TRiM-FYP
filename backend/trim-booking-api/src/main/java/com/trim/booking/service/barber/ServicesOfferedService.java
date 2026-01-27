package com.trim.booking.service.barber;

import com.trim.booking.dto.service.ServiceRequest;
import com.trim.booking.entity.ServiceCategory;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.repository.ServiceCategoryRepository;
import com.trim.booking.repository.ServiceRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicesOfferedService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;

    public ServicesOfferedService(ServiceRepository serviceRepository,
                                   ServiceCategoryRepository categoryRepository,
                                   BusinessRepository businessRepository) {
        this.serviceRepository = serviceRepository;
        this.categoryRepository = categoryRepository;
        this.businessRepository = businessRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    public ServiceOffered createService(ServiceRequest request) {
        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        ServiceOffered service = new ServiceOffered();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        service.setDepositPercentage(request.getDepositPercentage());
        service.setActive(request.getActive());
        service.setCategory(category);
        service.setBusiness(businessRepository.findById(getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found")));

        return serviceRepository.save(service);
    }

    public List<ServiceOffered> getAllServices() {
        return serviceRepository.findByBusinessId(getBusinessId());
    }

    public List<ServiceOffered> getActiveServices() {
        return serviceRepository.findByBusinessIdAndActiveTrue(getBusinessId());
    }

    public Optional<ServiceOffered> getServiceById(Long id) {
        return serviceRepository.findByIdAndBusinessId(id, getBusinessId());
    }

    public ServiceOffered updateService(Long id, ServiceRequest request) {
        ServiceOffered service = serviceRepository.findByIdAndBusinessId(id, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        service.setDepositPercentage(request.getDepositPercentage());
        service.setActive(request.getActive());
        service.setCategory(category);

        return serviceRepository.save(service);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public void deactivateService(Long id) {
        serviceRepository.findByIdAndBusinessId(id, getBusinessId())
                .map(service -> {
                    service.setActive(false);
                    return serviceRepository.save(service);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }
}