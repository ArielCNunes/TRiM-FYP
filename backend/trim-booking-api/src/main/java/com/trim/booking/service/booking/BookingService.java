package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import org.springframework.stereotype.Service;

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
     * Create a booking for a guest user.
     * This method creates a guest user account first, then creates the booking.
     *
     * @param firstName     Customer's first name
     * @param lastName      Customer's last name
     * @param email         Customer's email
     * @param phone         Customer's phone
     * @param barberId      Barber ID
     * @param serviceId     Service ID
     * @param bookingDate   Booking date
     * @param startTime     Start time
     * @param paymentMethod Payment method (pay_online or pay_in_shop)
     * @return Booking object
     * @throws com.trim.booking.exception.ConflictException if email already exists or time slot unavailable
     */
    public Booking createGuestBooking(String firstName, String lastName, String email, String phone,
                                      Long barberId, Long serviceId, LocalDate bookingDate,
                                      LocalTime startTime, String paymentMethod) {
        return commandService.createGuestBooking(firstName, lastName, email, phone,
                barberId, serviceId, bookingDate, startTime, paymentMethod);
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
}