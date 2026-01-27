package com.trim.booking.repository;

import com.trim.booking.entity.BarberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberAvailabilityRepository extends JpaRepository<BarberAvailability, Long> {
    // Business-filtered methods for multi-tenancy
    List<BarberAvailability> findByBusinessIdAndBarberId(Long businessId, Long barberId);

    Optional<BarberAvailability> findByBusinessIdAndBarberIdAndDayOfWeek(Long businessId, Long barberId, DayOfWeek dayOfWeek);

    List<BarberAvailability> findByBarberBusinessId(Long businessId);

    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.id = :id AND ba.barber.business.id = :businessId")
    Optional<BarberAvailability> findByIdAndBusinessId(@Param("id") Long id, @Param("businessId") Long businessId);
}
