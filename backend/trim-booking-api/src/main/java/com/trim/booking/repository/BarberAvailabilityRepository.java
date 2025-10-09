package com.trim.booking.repository;

import com.trim.booking.entity.BarberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberAvailabilityRepository extends JpaRepository<BarberAvailability, Long> {
    List<BarberAvailability> findByBarberId(Long barberId);

    Optional<BarberAvailability> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);
}
