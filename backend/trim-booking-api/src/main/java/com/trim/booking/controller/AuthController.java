package com.trim.booking.controller;

import com.trim.booking.dto.RegisterRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trim.booking.dto.LoginRequest;
import com.trim.booking.dto.LoginResponse;
import com.trim.booking.config.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BarberRepository barberRepository;

    public AuthController(UserService userService, JwtUtil jwtUtil, BarberRepository barberRepository) {
        this.barberRepository = barberRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
}