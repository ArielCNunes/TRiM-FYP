package com.trim.booking.controller;

import com.trim.booking.dto.auth.*;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.service.user.PasswordResetService;
import com.trim.booking.service.user.UserService;
import com.trim.booking.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trim.booking.config.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BarberRepository barberRepository;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, JwtUtil jwtUtil, BarberRepository barberRepository,
                          PasswordResetService passwordResetService) {
        this.barberRepository = barberRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<User> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        User user = userService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
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
            barberId = barberRepository.findByBusinessIdAndUserId(getBusinessId(), user.getId())
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
    }


    /**
     * Initiate password reset process.
     * Sends an email with reset link if account exists.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
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