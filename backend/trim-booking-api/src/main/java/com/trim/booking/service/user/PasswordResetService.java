package com.trim.booking.service.user;

import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

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
     * Requires tenant context to be set for security.
     *
     * @param email User's email address
     * @return Generic success message (don't reveal if email exists for security)
     * @throws BadRequestException if tenant context is not set
     */
    @Transactional
    public String initiatePasswordReset(String email) {
        // Require tenant context for password reset
        Long businessId = TenantContext.getCurrentBusinessId();
        if (businessId == null) {
            logger.warn("Password reset attempted without business context for email: {}", email);
            throw new BadRequestException("Business context is required for password reset");
        }

        // Find user by email within the specific business
        User user = userRepository.findByBusinessIdAndEmail(businessId, email).orElse(null);

        // If user exists in this business, generate token and send email
        if (user != null) {
            // Generate secure random token
            String resetToken = UUID.randomUUID().toString();

            // Set token, expiry, and business ID (1 hour from now)
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
            user.setResetTokenBusinessId(businessId);

            // Save to database
            userRepository.save(user);

            // Send email with reset link
            emailService.sendPasswordResetEmail(user, resetToken);

            logger.info("Password reset initiated for user {} in business {}", email, businessId);
        } else {
            logger.info("Password reset requested for non-existent email {} in business {}", email, businessId);
        }

        // Always return generic success message (don't reveal if email exists)
        return "If an account exists with this email, a password reset link has been sent.";
    }

    /**
     * Validate if a reset token is valid and not expired.
     * Also validates that the token's business context matches current tenant.
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
        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Validate business context matches token's business
        Long currentBusinessId = TenantContext.getCurrentBusinessId();
        if (currentBusinessId == null) {
            logger.warn("Reset token validation attempted without business context");
            return false;
        }
        if (user.getResetTokenBusinessId() == null) {
            logger.warn("Reset token has no associated business");
            return false;
        }
        if (!currentBusinessId.equals(user.getResetTokenBusinessId())) {
            logger.warn("Reset token business mismatch. Token business: {}, Current business: {}",
                    user.getResetTokenBusinessId(), currentBusinessId);
            return false;
        }

        return true;
    }

    /**
     * Reset user's password using the reset token.
     * Validates that the token's business context matches the current tenant.
     *
     * @param token       Reset token
     * @param newPassword New password (plain text, will be hashed)
     * @return Success message
     * @throws BadRequestException if token is invalid, expired, or business mismatch
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

        // Validate business context matches token's business
        Long currentBusinessId = TenantContext.getCurrentBusinessId();
        if (currentBusinessId == null) {
            logger.warn("Password reset attempted without business context");
            throw new BadRequestException("Business context is required for password reset");
        }
        if (user.getResetTokenBusinessId() == null) {
            logger.warn("Reset token has no associated business");
            throw new BadRequestException("Invalid or expired reset token");
        }
        if (!currentBusinessId.equals(user.getResetTokenBusinessId())) {
            logger.warn("Password reset attempt with business mismatch. Token business: {}, Current business: {}",
                    user.getResetTokenBusinessId(), currentBusinessId);
            throw new BadRequestException("Invalid or expired reset token");
        }

        // Hash new password
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update password
        user.setPasswordHash(hashedPassword);

        // Clear reset token, expiry, and business ID (one-time use)
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setResetTokenBusinessId(null);

        // Save user
        userRepository.save(user);

        logger.info("Password successfully reset for user in business {}", user.getBusiness().getId());

        return "Password has been successfully reset";
    }
}

