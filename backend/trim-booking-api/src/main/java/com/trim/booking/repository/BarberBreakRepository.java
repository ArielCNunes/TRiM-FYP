package com.trim.booking.repository;

import com.trim.booking.entity.BarberBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberBreakRepository extends JpaRepository<BarberBreak, Long> {
    // Business-filtered methods for multi-tenancy
    List<BarberBreak> findByBusinessIdAndBarberId(Long businessId, Long barberId);
}

