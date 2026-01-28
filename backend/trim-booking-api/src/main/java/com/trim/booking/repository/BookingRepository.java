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
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Business-filtered methods for multi-tenancy
    List<Booking> findByBusinessIdAndBarberIdAndBookingDate(Long businessId, Long barberId, LocalDate bookingDate);

    List<Booking> findByBusinessIdAndCustomerId(Long businessId, Long customerId);

    List<Booking> findByBusinessIdAndBarberId(Long businessId, Long barberId);

    List<Booking> findByBusinessIdAndBookingDate(Long businessId, LocalDate bookingDate);

    List<Booking> findByBusinessIdAndStatus(Long businessId, Booking.BookingStatus status);

    List<Booking> findByBusinessIdAndBookingDateAndStatus(Long businessId, LocalDate bookingDate, Booking.BookingStatus status);

    Optional<Booking> findByIdAndBusinessId(Long id, Long businessId);

    // This query locks the rows for the specified barber and date to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.business.id = :businessId AND b.barber.id = :barberId AND b.bookingDate = :bookingDate")
    List<Booking> findByBusinessIdAndBarberIdAndBookingDateWithLock(@Param("businessId") Long businessId,
                                                                     @Param("barberId") Long barberId,
                                                                     @Param("bookingDate") LocalDate bookingDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.business.id = :businessId AND b.bookingDate = CURRENT_DATE")
    Long countTodaysBookingsByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.business.id = :businessId AND b.bookingDate >= CURRENT_DATE AND b.status != 'CANCELLED'")
    Long countUpcomingBookingsByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT SUM(b.service.price) FROM Booking b WHERE b.business.id = :businessId AND b.paymentStatus = 'FULLY_PAID'")
    BigDecimal calculateTotalRevenueByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT SUM(b.service.price) FROM Booking b WHERE b.business.id = :businessId AND b.paymentStatus = 'FULLY_PAID' AND MONTH(b.bookingDate) = MONTH(CURRENT_DATE) AND YEAR(b.bookingDate) = YEAR(CURRENT_DATE)")
    BigDecimal calculateThisMonthRevenueByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT b.service.name as serviceName, COUNT(b) as count FROM Booking b WHERE b.business.id = :businessId AND b.status != 'CANCELLED' GROUP BY b.service.name ORDER BY count DESC")
    List<Object[]> findPopularServicesByBusinessId(@Param("businessId") Long businessId);


    // Global method for scheduled cleanup task - runs across all businesses
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
            "AND b.expiresAt IS NOT NULL " +
            "AND b.expiresAt < CURRENT_TIMESTAMP")
    List<Booking> findExpiredPendingBookings();

    // Global method for scheduled reminder task - runs across all businesses
    List<Booking> findByBookingDate(LocalDate bookingDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.business.id = :businessId AND b.customer.id = :customerId AND b.status = :status")
    Long countByBusinessIdAndCustomerIdAndStatus(@Param("businessId") Long businessId, @Param("customerId") Long customerId, @Param("status") Booking.BookingStatus status);

    // Get recent bookings ordered by creation date (for dashboard)
    @Query("SELECT b FROM Booking b WHERE b.business.id = :businessId ORDER BY b.createdAt DESC")
    List<Booking> findRecentByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId);
}