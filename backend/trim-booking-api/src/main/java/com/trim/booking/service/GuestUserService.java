package com.trim.booking.service;

import com.trim.booking.entity.User;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestUserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public GuestUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Create a guest user account without password.
     * Guest accounts can be converted to full accounts later by setting a password.
     *
     * @param firstName Guest's first name
     * @param lastName Guest's last name
     * @param email Guest's email (must be unique)
     * @param phone Guest's phone number
     * @return Created guest User
     * @throws ConflictException if email already exists
     */
    @Transactional
    public User createGuestUser(String firstName, String lastName, String email, String phone) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            // Find the existing user
            User existingUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ConflictException("Email already registered: " + email));

            // If the existing user has a password (registered user), throw error
            if (existingUser.getPasswordHash() != null) {
                throw new ConflictException("Email already registered. Please login instead.");
            }

            // If it's a guest user (no password), return the existing guest user
            // This allows the same guest to make multiple bookings
            return existingUser;
        }

        // Create new guest user
        User guestUser = new User();
        guestUser.setFirstName(firstName);
        guestUser.setLastName(lastName);
        guestUser.setEmail(email);
        guestUser.setPhone(phone);
        guestUser.setRole(User.Role.CUSTOMER);
        // Do NOT set password - leave as null to indicate guest account

        // Save and return
        return userRepository.save(guestUser);
    }

    /**
     * Convert a guest account to a full account by setting a password.
     * This allows the guest to log in.
     *
     * @param userId ID of the user to upgrade
     * @param password Plain text password to hash and set
     * @return Updated User
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public User saveGuestAccount(Long userId, String password) {
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Hash and set password
        user.setPasswordHash(passwordEncoder.encode(password));

        // Save and return
        return userRepository.save(user);
    }
}

