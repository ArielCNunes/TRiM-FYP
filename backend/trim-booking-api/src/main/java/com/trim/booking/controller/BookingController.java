package com.trim.booking.controller;

import com.trim.booking.dto.CreateBookingRequest;
import com.trim.booking.entity.Booking;
import com.trim.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking.
     * <p>
     * POST /api/bookings
     * Body: { "customerId": 1, "barberId": 1, "serviceId": 1, "bookingDate": "2025-10-13", "startTime": "10:00" }
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        try {
            Booking booking = bookingService.createBooking(
                    request.getCustomerId(),
                    request.getBarberId(),
                    request.getServiceId(),
                    request.getBookingDate(),
                    request.getStartTime()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
     * Get a specific booking by ID.
     * <p>
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel a booking.
     * <p>
     * PATCH /api/bookings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.cancelBooking(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}