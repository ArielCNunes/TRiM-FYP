package com.trim.booking.service.booking;

import com.trim.booking.dto.booking.UpdateBookingRequest;
import com.trim.booking.entity.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Facade service for booking operations.
 * Delegates to specialized services following Single Responsibility Principle.
 * <p>
 * This service maintains backward compatibility with existing controllers
 * while internally delegating to focused, single-responsibility services:
 * - BookingCommandService: Create/Update operations
 * - BookingQueryService: Read operations
 * - BookingStatusService: Status transitions
 * - BookingConflictDetectionService: Conflict detection
 * - BookingValidationService: Entity validation
 */
@Service
@Transactional
public class BookingService {
    private final BookingCommandService commandService;
    private final BookingQueryService queryService;
    private final BookingStatusService statusService;

    public BookingService(BookingCommandService commandService,
                          BookingQueryService queryService,
                          BookingStatusService statusService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.statusService = statusService;
    }

    /**
     * Create a new booking with race condition protection.
     * <p>
     * Uses pessimistic locking to prevent two customers from booking the same slot simultaneously.
     * Transaction isolation ensures atomicity.
     */
    public Booking createBooking(Long customerId, Long barberId, Long serviceId, LocalDate bookingDate, LocalTime startTime, String paymentMethod) {
        return commandService.createBooking(customerId, barberId, serviceId, bookingDate, startTime, paymentMethod);
    }


    /**
     * Get all bookings for a customer.
     */
    public List<Booking> getCustomerBookings(Long customerId) {
        return queryService.getCustomerBookings(customerId);
    }

    /**
     * Get all bookings for a barber.
     */
    public List<Booking> getBarberBookings(Long barberId) {
        return queryService.getBarberBookings(barberId);
    }

    /**
     * Get a specific booking by ID.
     */
    public Booking getBookingById(Long bookingId) {
        return queryService.getBookingById(bookingId);
    }

    /**
     * Get barber's bookings for a specific date.
     */
    public List<Booking> getBarberScheduleForDate(Long barberId, LocalDate date) {
        return queryService.getBarberScheduleForDate(barberId, date);
    }

    /**
     * Mark booking as completed.
     * Only the assigned barber or admin can do this.
     */
    public Booking markAsCompleted(Long bookingId) {
        return statusService.markAsCompleted(bookingId);
    }

    /**
     * Mark booking as fully paid.
     * Used when customer pays the outstanding balance in the shop.
     * Only admin or barber can do this.
     */
    public Booking markAsPaid(Long bookingId) {
        return statusService.markAsPaid(bookingId);
    }

    /**
     * Mark booking as no-show.
     * Only the assigned barber or admin can do this.
     */
    public Booking markAsNoShow(Long bookingId) {
        return statusService.markAsNoShow(bookingId);
    }

    /**
     * Cancel a booking.
     */
    public void cancelBooking(Long bookingId) {
        statusService.cancelBooking(bookingId);
    }

    /**
     * Update an existing booking's date and time.
     * Only allows changing date/time
     *
     * @param bookingId Booking ID to update
     * @param request   Update request with new date/time
     * @return Updated booking
     */
    public Booking updateBooking(Long bookingId, UpdateBookingRequest request) {
        return commandService.updateBooking(bookingId, request);
    }
}