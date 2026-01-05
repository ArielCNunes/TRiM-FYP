package com.trim.booking.service.user;

import com.trim.booking.dto.auth.AdminRegisterRequest;
import com.trim.booking.dto.auth.RegisterRequest;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.InvalidPhoneNumberException;
import com.trim.booking.exception.UnauthorizedException;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.util.PhoneNumberUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final GuestUserService guestUserService;
    private final BusinessRepository businessRepository;

    public UserService(UserRepository userRepository, GuestUserService guestUserService, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.guestUserService = guestUserService;
        this.businessRepository = businessRepository;
    }

    /**
     * Authenticate user and return user details.
     *
     * @param email    User's email
     * @param password Plain text password
     * @return User if credentials valid
     * @throws UnauthorizedException if credentials invalid
     */
    public User login(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
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

        // Normalize phone number (defensive programming - entity will also normalize)
        String normalizedPhone;
        try {
            normalizedPhone = PhoneNumberUtil.normalizePhoneNumber(request.getPhone(), "353");
        } catch (InvalidPhoneNumberException e) {
            throw new BadRequestException("Invalid phone number: " + e.getMessage());
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(normalizedPhone);
        user.setRole(User.Role.CUSTOMER);

        // Save to database
        return userRepository.save(user);
    }

    /**
     * Register a new admin with associated business.
     *
     * @param request Admin registration request containing user and business details
     * @return Registered User
     * @throws RuntimeException if email already exists
     */
    public User registerAdmin(AdminRegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Normalize phone number
        String normalizedPhone;
        try {
            normalizedPhone = PhoneNumberUtil.normalizePhoneNumber(request.getPhone(), "353");
        } catch (InvalidPhoneNumberException e) {
            throw new BadRequestException("Invalid phone number: " + e.getMessage());
        }

        // Create new user with ADMIN role
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(normalizedPhone);
        user.setRole(User.Role.ADMIN);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Create business linked to admin user
        Business business = new Business();
        business.setName(request.getBusinessName());
        business.setAdminUser(savedUser);

        // Save business to database
        businessRepository.save(business);

        return savedUser;
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