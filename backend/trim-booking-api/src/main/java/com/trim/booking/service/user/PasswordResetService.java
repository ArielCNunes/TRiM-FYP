package com.trim.booking.service.user;

import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.service.notification.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for handling password reset operations.
 */
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    // Token expiry time: 1 hour
    private static final int TOKEN_EXPIRY_HOURS = 1;

    public PasswordResetService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Initiate password reset process.
     * Generates a secure token, saves it with expiry, and sends reset email.
     *
     * @param email User's email address
     * @return Generic success message (don't reveal if email exists for security)
     */
    @Transactional
    public String initiatePasswordReset(String email) {
        // Find user by email (if exists)
        User user = userRepository.findByEmail(email).orElse(null);

        // If user exists, generate token and send email
        if (user != null && user.getPasswordHash() != null) { // Only for accounts with passwords (not guest accounts)
            // Generate secure random token
            String resetToken = UUID.randomUUID().toString();

            // Set token and expiry (1 hour from now)
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));

            // Save to database
            userRepository.save(user);

            // Send email with reset link
            emailService.sendPasswordResetEmail(user, resetToken);
        }

        // Always return generic success message (don't reveal if email exists)
        return "If an account exists with this email, a password reset link has been sent.";
    }

    /**
     * Validate if a reset token is valid and not expired.
     *
     * @param token Reset token
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean validateResetToken(String token) {
        User user = userRepository.findByResetToken(token).orElse(null);

        if (user == null) {
            return false;
        }

        // Check if token has expired
        return user.getResetTokenExpiry() != null &&
                user.getResetTokenExpiry().isAfter(LocalDateTime.now());
    }

    /**
     * Reset user's password using the reset token.
     *
     * @param token       Reset token
     * @param newPassword New password (plain text, will be hashed)
     * @return Success message
     * @throws BadRequestException if token is invalid or expired
     */
    @Transactional
    public String resetPassword(String token, String newPassword) {
        // Find user by token
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        // Check if token has expired
        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        // Hash new password
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update password
        user.setPasswordHash(hashedPassword);

        // Clear reset token and expiry (one-time use)
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // Save user
        userRepository.save(user);

        return "Password has been successfully reset";
    }
}

