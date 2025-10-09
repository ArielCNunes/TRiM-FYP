package com.trim.booking.repository;

import com.trim.booking.entity.ServiceOffered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffered, Long> {
    List<ServiceOffered> findByActiveTrue();
}