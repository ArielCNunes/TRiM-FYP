package com.trim.booking.controller;

import com.trim.booking.dto.*;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.User;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.service.BookingService;
import com.trim.booking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trim.booking.config.JwtUtil;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BarberRepository barberRepository;
    private final BookingService bookingService;

    public AuthController(UserService userService, JwtUtil jwtUtil, BarberRepository barberRepository, BookingService bookingService) {
        this.barberRepository = barberRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.bookingService = bookingService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Send the request to the service layer
            User user = userService.registerCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            User user = userService.login(request.getEmail(), request.getPassword());

            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name(),
                    user.getId()
            );

            // Get barberId if user is a barber
            Long barberId = null;
            if (user.getRole().name().equals("BARBER")) {
                barberId = barberRepository.findByUserId(user.getId())
                        .map(Barber::getId)
                        .orElse(null);
            }

            // Create response
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    token,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name(),
                    barberId
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Create a guest booking with customer information.
     * Automatically creates a guest user and booking.
     */
    @PostMapping("/guest-booking")
    public ResponseEntity<?> createGuestBooking(@Valid @RequestBody GuestBookingRequest request) {
        try {
            Booking booking = bookingService.createGuestBooking(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getBarberId(),
                    request.getServiceId(),
                    request.getBookingDate(),
                    request.getStartTime(),
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "pay_online"
            );

            // Calculate deposit and outstanding balance
            BigDecimal depositAmount = booking.getDepositAmount();
            BigDecimal outstandingBalance = booking.getOutstandingBalance();

            GuestBookingResponse response = new GuestBookingResponse(
                    booking.getId(),
                    booking.getCustomer().getId(),
                    depositAmount,
                    outstandingBalance,
                    booking.getCustomer().getEmail()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Save a guest account by setting a password.
     * Converts a guest user to a registered user.
     */
    @PostMapping("/save-account")
    public ResponseEntity<?> saveGuestAccount(@Valid @RequestBody SaveAccountRequest request) {
        try {
            User updatedUser = userService.saveGuestAccount(request.getUserId(), request.getPassword());

            return ResponseEntity.ok(new SaveAccountResponse(
                    updatedUser.getId(),
                    updatedUser.getEmail(),
                    "Account saved successfully"
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}