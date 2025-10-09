package com.trim.booking.repository;

import com.trim.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBarberIdAndBookingDate(Long barberId, LocalDate bookingDate);

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByBarberId(Long barberId);
}