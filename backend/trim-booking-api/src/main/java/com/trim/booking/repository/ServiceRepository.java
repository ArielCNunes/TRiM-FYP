package com.trim.booking.repository;

import com.trim.booking.entity.ServiceOffered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffered, Long> {
    // Business-filtered methods for multi-tenancy
    List<ServiceOffered> findByBusinessId(Long businessId);

    List<ServiceOffered> findByBusinessIdAndActiveTrue(Long businessId);
}