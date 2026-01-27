package com.trim.booking.repository;

import com.trim.booking.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    // Business-filtered methods for multi-tenancy
    List<Barber> findByBusinessId(Long businessId);

    List<Barber> findByBusinessIdAndActiveTrue(Long businessId);

    @Query("SELECT COUNT(b) FROM Barber b WHERE b.business.id = :businessId AND b.active = true")
    Long countByBusinessIdAndActiveTrue(@Param("businessId") Long businessId);

    Optional<Barber> findByBusinessIdAndUserId(Long businessId, Long userId);
}