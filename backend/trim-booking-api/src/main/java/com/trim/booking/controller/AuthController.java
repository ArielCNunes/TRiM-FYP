package com.trim.booking.controller;

import com.trim.booking.dto.auth.*;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.service.auth.AuthTokenExchangeService;
import com.trim.booking.service.user.PasswordResetService;
import com.trim.booking.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trim.booking.config.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BarberRepository barberRepository;
    private final BusinessRepository businessRepository;
    private final PasswordResetService passwordResetService;
    private final AuthTokenExchangeService authTokenExchangeService;

    public AuthController(UserService userService, JwtUtil jwtUtil, BarberRepository barberRepository,
                          BusinessRepository businessRepository, PasswordResetService passwordResetService,
                          AuthTokenExchangeService authTokenExchangeService) {
        this.barberRepository = barberRepository;
        this.businessRepository = businessRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
        this.authTokenExchangeService = authTokenExchangeService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Register a new admin with a new business.
     * Returns an exchange token for secure cross-subdomain redirect.
     * The exchange token can be traded for actual JWT credentials.
     */
    @PostMapping("/register-admin")
    public ResponseEntity<Map<String, String>> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        User user = userService.registerAdmin(request);

        // Get business slug from user's business
        Business business = businessRepository.findById(user.getBusiness().getId())
                .orElseThrow(() -> new RuntimeException("Business not found"));
        String businessSlug = business.getSlug();

        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                user.getBusiness().getId()
        );

        // Create user data JSON
        String userData = String.format(
                "{\"id\":%d,\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\",\"barberId\":null}",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );

        // Create exchange token (NOT the JWT)
        String exchangeToken = authTokenExchangeService.createExchangeToken(
                jwtToken,
                userData,
                businessSlug
        );

        // Return exchange token and business slug for redirect
        Map<String, String> response = new HashMap<>();
        response.put("exchangeToken", exchangeToken);
        response.put("businessSlug", businessSlug);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Exchange a temporary token for actual JWT credentials.
     * This endpoint is public (no tenant context required).
     */
    @PostMapping("/exchange-token")
    public ResponseEntity<Map<String, String>> exchangeToken(@RequestBody Map<String, String> request) {
        String exchangeToken = request.get("exchangeToken");
        if (exchangeToken == null || exchangeToken.isEmpty()) {
            throw new BadRequestException("exchangeToken is required");
        }

        Map<String, String> credentials = authTokenExchangeService.exchangeToken(exchangeToken);
        return ResponseEntity.ok(credentials);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate user
        User user = userService.login(request.getEmail(), request.getPassword());

        // Get business slug from user's business
        Business business = businessRepository.findById(user.getBusiness().getId())
                .orElseThrow(() -> new RuntimeException("Business not found"));
        String businessSlug = business.getSlug();

        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                user.getBusiness().getId()
        );

        // Get barberId if user is a barber
        Long barberId = null;
        if (user.getRole().name().equals("BARBER")) {
            barberId = barberRepository.findByBusinessIdAndUserId(business.getId(), user.getId())
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
                barberId,
                businessSlug
        );

        return ResponseEntity.ok(response);
    }



    /**
     * Initiate password reset process.
     * Sends an email with reset link if account exists.
     * Requires business context to be set via subdomain.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // TenantFilter should have set the business context from the subdomain
        // PasswordResetService will validate that context exists
        String message = passwordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(new PasswordResetResponse(message));
    }

    /**
     * Validate reset token.
     * Used to check if token is valid before showing reset form.
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<PasswordResetResponse> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);

        if (isValid) {
            return ResponseEntity.ok(new PasswordResetResponse("Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponse("Invalid or expired token"));
        }
    }

    /**
     * Reset password using reset token.
     * Updates user's password and clears the reset token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new PasswordResetResponse(message));
    }
}