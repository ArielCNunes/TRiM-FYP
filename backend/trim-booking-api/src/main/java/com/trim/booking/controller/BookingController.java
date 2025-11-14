package com.trim.booking.controller;

import com.trim.booking.repository.BookingRepository;

import com.trim.booking.dto.booking.CreateBookingRequest;
import com.trim.booking.dto.booking.UpdateBookingRequest;
import com.trim.booking.entity.Booking;
import com.trim.booking.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a new booking.
     * <p>
     * POST /api/bookings
     * Body: { "customerId": 1, "barberId": 1, "serviceId": 1, "bookingDate": "2025-10-13", "startTime": "10:00" }
     */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(
                request.getCustomerId(),
                request.getBarberId(),
                request.getServiceId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "pay_online"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * Get all bookings for a customer.
     * <p>
     * GET /api/bookings/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerBookings(@PathVariable Long customerId) {
        // Get authenticated user's ID from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is trying to access their own bookings
        if (!authenticatedUserId.equals(customerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own bookings");
        }

        return ResponseEntity.ok(bookingService.getCustomerBookings(customerId));
    }

    /**
     * Get all bookings for a barber.
     * <p>
     * GET /api/bookings/barber/{barberId}
     */
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<Booking>> getBarberBookings(@PathVariable Long barberId) {
        return ResponseEntity.ok(bookingService.getBarberBookings(barberId));
    }

    /**
     * Get all bookings (admin only) with optional filters.
     * <p>
     * GET /api/bookings/all?status=PENDING&date=2025-10-15
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Booking> bookings;

        if (date != null && status != null) {
            // Filter by both date and status
            bookings = bookingRepository.findByBookingDateAndStatus(date, Booking.BookingStatus.valueOf(status.toUpperCase()));
        } else if (date != null) {
            // Filter by date only
            bookings = bookingRepository.findByBookingDate(date);
        } else if (status != null) {
            // Filter by status only
            bookings = bookingRepository.findByStatus(Booking.BookingStatus.valueOf(status.toUpperCase()));
        } else {
            // No filters - return all bookings
            bookings = bookingRepository.findAll();
        }

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get a specific booking by ID.
     * <p>
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Cancel a booking.
     * <p>
     * PATCH /api/bookings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);

        // Fetch the updated booking to return it
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Update a booking's date and time.
     * Only allows changing date/time
     * <p>
     * PUT /api/bookings/{id}
     * Body: { "bookingDate": "2025-11-20", "startTime": "14:00" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Fetch booking to check ownership
        Booking booking = bookingService.getBookingById(id);

        // Authorization check: only booking owner can update their booking
        if (!booking.getCustomer().getId().equals(authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only update your own bookings");
        }

        // Perform update
        Booking updated = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get barber's schedule for a specific date.
     * <p>
     * GET /api/bookings/barber/{barberId}/schedule?date=2025-10-20
     */
    @GetMapping("/barber/{barberId}/schedule")
    @PreAuthorize("hasAnyRole('BARBER', 'ADMIN')")
    public ResponseEntity<List<Booking>> getBarberSchedule(@PathVariable Long barberId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Booking> schedule = bookingService.getBarberScheduleForDate(barberId, date);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Mark booking as completed.
     * <p>
     * PUT /api/bookings/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('BARBER', 'ADMIN')")
    public ResponseEntity<Booking> markAsCompleted(@PathVariable Long id) {
        Booking booking = bookingService.markAsCompleted(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Mark booking as fully paid.
     * Used when customer pays the outstanding balance in the shop.
     * <p>
     * PUT /api/bookings/{id}/mark-paid
     */
    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('BARBER', 'ADMIN')")
    public ResponseEntity<Booking> markAsPaid(@PathVariable Long id) {
        Booking booking = bookingService.markAsPaid(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Mark booking as no-show.
     * <p>
     * PUT /api/bookings/{id}/no-show
     */
    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('BARBER', 'ADMIN')")
    public ResponseEntity<Booking> markAsNoShow(@PathVariable Long id) {
        Booking booking = bookingService.markAsNoShow(id);
        return ResponseEntity.ok(booking);
    }
}