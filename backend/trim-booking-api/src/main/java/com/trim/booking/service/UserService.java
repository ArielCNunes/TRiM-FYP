package com.trim.booking.service;

import com.trim.booking.dto.RegisterRequest;
import com.trim.booking.entity.User;
import com.trim.booking.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final GuestUserService guestUserService;

    public UserService(UserRepository userRepository, GuestUserService guestUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.guestUserService = guestUserService;
    }

    /**
     * Authenticate user and return user details.
     *
     * @param email    User's email
     * @param password Plain text password
     * @return User if credentials valid
     * @throws RuntimeException if credentials invalid
     */
    public User login(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    /**
     * Register a new customer.
     *
     * @param request Registration request containing user details
     * @return Registered User
     * @throws RuntimeException if email already exists
     */
    public User registerCustomer(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(User.Role.CUSTOMER);

        // Save to database
        return userRepository.save(user);
    }

    /**
     * Save a guest account by setting password.
     * Converts guest user to registered user.
     *
     * @param userId User ID
     * @param password Plain text password
     * @return Updated user
     */
    public User saveGuestAccount(Long userId, String password) {
        return guestUserService.saveGuestAccount(userId, password);
    }
}