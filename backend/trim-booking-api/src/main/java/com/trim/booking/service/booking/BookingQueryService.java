package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service responsible for booking read operations
 * Single responsibility: querying bookings.
 */
@Service
public class BookingQueryService {
    private final BookingRepository bookingRepository;

    public BookingQueryService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Get booking by ID.
     *
     * @param bookingId Booking ID
     * @return Booking entity
     * @throws ResourceNotFoundException if booking not found
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
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
        return bookingRepository.findByCustomerId(customerId);
    }

    /**
     * Get all bookings for a barber.
     *
     * @param barberId Barber ID
     * @return List of bookings
     */
    public List<Booking> getBarberBookings(Long barberId) {
        return bookingRepository.findByBarberId(barberId);
    }

    /**
     * Get barber's schedule for a specific date.
     *
     * @param barberId Barber ID
     * @param date     Booking date
     * @return List of bookings
     */
    public List<Booking> getBarberScheduleForDate(Long barberId, LocalDate date) {
        return bookingRepository.findByBarberIdAndBookingDate(barberId, date);
    }

    /**
     * Get upcoming bookings for a customer.
     *
     * @param customerId Customer ID
     * @return List of upcoming bookings
     */
    public List<Booking> getUpcomingBookingsForCustomer(Long customerId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByCustomerId(customerId).stream()
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
        return bookingRepository.findByCustomerId(customerId).stream()
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
        if (date != null && status != null) {
            return bookingRepository.findByBookingDateAndStatus(date, status);
        } else if (date != null) {
            return bookingRepository.findByBookingDate(date);
        } else if (status != null) {
            return bookingRepository.findByStatus(status);
        } else {
            return bookingRepository.findAll();
        }
    }
}

