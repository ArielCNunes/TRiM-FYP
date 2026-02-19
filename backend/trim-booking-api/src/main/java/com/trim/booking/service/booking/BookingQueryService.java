package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service responsible for booking read operations
 * Single responsibility: querying bookings.
 */
@Service
@Transactional(readOnly = true)
public class BookingQueryService {
    private final BookingRepository bookingRepository;

    public BookingQueryService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Get booking by ID.
     *
     * @param bookingId Booking ID
     * @return Booking entity
     * @throws ResourceNotFoundException if booking not found
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findByIdAndBusinessId(bookingId, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));
    }

    /**
     * Get all bookings for a customer.
     *
     * @param customerId Customer ID
     * @return List of bookings
     */
    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingRepository.findByBusinessIdAndCustomerId(getBusinessId(), customerId);
    }

    /**
     * Get all bookings for a barber.
     *
     * @param barberId Barber ID
     * @return List of bookings
     */
    public List<Booking> getBarberBookings(Long barberId) {
        return bookingRepository.findByBusinessIdAndBarberId(getBusinessId(), barberId);
    }

    /**
     * Get barber's schedule for a specific date.
     *
     * @param barberId Barber ID
     * @param date     Booking date
     * @return List of bookings
     */
    public List<Booking> getBarberScheduleForDate(Long barberId, LocalDate date) {
        return bookingRepository.findByBusinessIdAndBarberIdAndBookingDate(getBusinessId(), barberId, date);
    }

    /**
     * Get upcoming bookings for a customer.
     *
     * @param customerId Customer ID
     * @return List of upcoming bookings
     */
    public List<Booking> getUpcomingBookingsForCustomer(Long customerId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByBusinessIdAndCustomerId(getBusinessId(), customerId).stream()
                .filter(b -> b.getBookingDate().isAfter(today) ||
                        b.getBookingDate().isEqual(today))
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .toList();
    }

    /**
     * Get past bookings for a customer.
     *
     * @param customerId Customer ID
     * @return List of past bookings
     */
    public List<Booking> getPastBookingsForCustomer(Long customerId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByBusinessIdAndCustomerId(getBusinessId(), customerId).stream()
                .filter(b -> b.getBookingDate().isBefore(today))
                .toList();
    }

    /**
     * Get all bookings with filters.
     *
     * @param status Optional status filter
     * @param date   Optional date filter
     * @return List of bookings
     */
    public List<Booking> getAllBookings(Booking.BookingStatus status, LocalDate date) {
        Long businessId = getBusinessId();
        if (date != null && status != null) {
            return bookingRepository.findByBusinessIdAndBookingDateAndStatus(businessId, date, status);
        } else if (date != null) {
            return bookingRepository.findByBusinessIdAndBookingDate(businessId, date);
        } else if (status != null) {
            return bookingRepository.findByBusinessIdAndStatus(businessId, status);
        } else {
            return bookingRepository.findByBusinessIdAndBookingDate(businessId, LocalDate.now());
        }
    }
}

