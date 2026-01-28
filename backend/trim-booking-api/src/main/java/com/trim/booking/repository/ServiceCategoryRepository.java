package com.trim.booking.repository;

import com.trim.booking.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    // Business-filtered methods for multi-tenancy
    List<ServiceCategory> findByBusinessId(Long businessId);

    Optional<ServiceCategory> findByIdAndBusinessId(Long id, Long businessId);
}