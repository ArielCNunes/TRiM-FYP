package com.trim.booking.repository;

import com.trim.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBarberIdAndBookingDate(Long barberId, LocalDate bookingDate);

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByBarberId(Long barberId);

    List<Booking> findByBookingDate(LocalDate bookingDate);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByBookingDateAndStatus(LocalDate bookingDate, Booking.BookingStatus status);

    // This query locks the rows for the specified barber and date to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.barber.id = :barberId AND b.bookingDate = :bookingDate")
    List<Booking> findByBarberIdAndBookingDateWithLock(@Param("barberId") Long barberId,
                                                       @Param("bookingDate") LocalDate bookingDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = CURRENT_DATE")
    Long countTodaysBookings();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate >= CURRENT_DATE AND b.status != 'CANCELLED'")
    Long countUpcomingBookings();

    @Query("SELECT SUM(b.service.price) FROM Booking b WHERE b.paymentStatus = 'FULLY_PAID'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(b.service.price) FROM Booking b WHERE b.paymentStatus = 'FULLY_PAID' AND MONTH(b.bookingDate) = MONTH(CURRENT_DATE) AND YEAR(b.bookingDate) = YEAR(CURRENT_DATE)")
    BigDecimal calculateThisMonthRevenue();

    @Query("SELECT b.service.name as serviceName, COUNT(b) as count FROM Booking b WHERE b.status != 'CANCELLED' GROUP BY b.service.name ORDER BY count DESC")
    List<Object[]> findPopularServices();
}