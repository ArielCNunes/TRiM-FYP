package com.trim.booking.controller;

import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.BarberRepository;

import com.trim.booking.dto.booking.CreateBookingRequest;
import com.trim.booking.dto.booking.UpdateBookingRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Booking;
import com.trim.booking.service.booking.BookingService;
import com.trim.booking.tenant.TenantContext;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final BarberRepository barberRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository, BarberRepository barberRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.barberRepository = barberRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Create a new booking.
     * <p>
     * POST /api/bookings
     * Body: { "barberId": 1, "serviceId": 1, "bookingDate": "2025-10-13", "startTime": "10:00" }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        // Get the authenticated user's ID from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        Booking booking = bookingService.createBooking(
                authenticatedUserId,
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
    @PreAuthorize("hasAnyRole('BARBER', 'ADMIN')")
    public ResponseEntity<?> getBarberBookings(@PathVariable Long barberId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user is admin (admins can view any barber's bookings in their business)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // For barbers: verify they are requesting their own bookings
            Long businessId = getBusinessId();
            Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(businessId, authenticatedUserId);

            if (barberOpt.isEmpty() || !barberOpt.get().getId().equals(barberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only view your own bookings");
            }
        }

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
        Long businessId = getBusinessId();

        if (date != null && status != null) {
            // Filter by both date and status
            bookings = bookingRepository.findByBusinessIdAndBookingDateAndStatus(businessId, date, Booking.BookingStatus.valueOf(status.toUpperCase()));
        } else if (date != null) {
            // Filter by date only
            bookings = bookingRepository.findByBusinessIdAndBookingDate(businessId, date);
        } else if (status != null) {
            // Filter by status only
            bookings = bookingRepository.findByBusinessIdAndStatus(businessId, Booking.BookingStatus.valueOf(status.toUpperCase()));
        } else {
            // No filters - return today's bookings for the business
            bookings = bookingRepository.findByBusinessIdAndBookingDate(businessId, LocalDate.now());
        }

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get a specific booking by ID.
     * <p>
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        // Get authenticated user's ID and role from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Check if user has ADMIN role
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Fetch the booking
        Booking booking = bookingService.getBookingById(id);

        // Admin can access any booking in their business
        if (isAdmin) {
            return ResponseEntity.ok(booking);
        }

        // Check if user is the customer who made the booking
        boolean isBookingCustomer = booking.getCustomer().getId().equals(authenticatedUserId);

        // Check if user is the barber assigned to the booking
        boolean isBookingBarber = false;
        Long businessId = getBusinessId();
        Optional<Barber> barberOpt = barberRepository.findByBusinessIdAndUserId(businessId, authenticatedUserId);
        if (barberOpt.isPresent()) {
            isBookingBarber = booking.getBarber().getId().equals(barberOpt.get().getId());
        }

        // If user is neither the customer nor the barber, deny access
        if (!isBookingCustomer && !isBookingBarber) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permission to view this booking");
        }

        return ResponseEntity.ok(booking);
    }

    /**
     * Cancel a booking.
     * <p>
     * PATCH /api/bookings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = (Long) auth.getDetails();

        // Fetch booking first
        Booking booking = bookingService.getBookingById(id);

        // Check if user is admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Only allow cancellation by booking owner or admin
        if (!isAdmin && !booking.getCustomer().getId().equals(authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only cancel your own bookings");
        }

        bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingService.getBookingById(id));
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