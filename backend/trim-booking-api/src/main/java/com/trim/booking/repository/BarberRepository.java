package com.trim.booking.repository;

import com.trim.booking.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    List<Barber> findByActiveTrue();

    @Query("SELECT COUNT(b) FROM Barber b WHERE b.active = true")
    Long countActiveBarbers();

    Optional<Barber> findByUserId(Long userId);
}