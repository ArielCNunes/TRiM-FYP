package com.trim.booking.service.barber;

import com.trim.booking.dto.service.CategoryRequest;
import com.trim.booking.dto.service.CategoryWithServicesResponse;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.ServiceCategory;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.repository.ServiceCategoryRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;

    public ServiceCategoryService(ServiceCategoryRepository categoryRepository,
                                   BusinessRepository businessRepository) {
        this.categoryRepository = categoryRepository;
        this.businessRepository = businessRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findByBusinessId(getBusinessId());
    }

    @Transactional(readOnly = true)
    public List<CategoryWithServicesResponse> getAllCategoriesWithServices() {
        return categoryRepository.findByBusinessId(getBusinessId()).stream()
                .map(CategoryWithServicesResponse::new)
                .collect(Collectors.toList());
    }

    public ServiceCategory getCategoryById(Long id) {
        return categoryRepository.findByIdAndBusinessId(id, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public ServiceCategory createCategory(String name) {
        ServiceCategory category = new ServiceCategory(name);
        Business business = businessRepository.findById(getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        category.setBusiness(business);
        return categoryRepository.save(category);
    }

    public ServiceCategory updateCategory(Long id, CategoryRequest request) {
        ServiceCategory category = getCategoryById(id);
        category.setName(request.getName());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return categoryRepository.save(category);
    }

    public ServiceCategory deactivateCategory(Long id) {
        ServiceCategory category = getCategoryById(id);

        // Check if any services in this category are still active
        boolean hasActiveServices = category.getServices().stream()
                .anyMatch(service -> service.getActive());

        if (hasActiveServices) {
            throw new BadRequestException("Cannot deactivate category with active services. Please deactivate all services first.");
        }

        category.setActive(false);
        return categoryRepository.save(category);
    }
}