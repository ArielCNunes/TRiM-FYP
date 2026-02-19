package com.trim.booking.service.user;

import com.trim.booking.config.RlsBypass;
import com.trim.booking.dto.auth.AdminRegisterRequest;
import com.trim.booking.dto.auth.RegisterRequest;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.InvalidPhoneNumberException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.exception.UnauthorizedException;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.tenant.TenantContext;
import com.trim.booking.util.PhoneNumberUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BusinessRepository businessRepository;
    private final RlsBypass rlsBypass;

    public UserService(UserRepository userRepository, BusinessRepository businessRepository, RlsBypass rlsBypass) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.businessRepository = businessRepository;
        this.rlsBypass = rlsBypass;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
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
        Long businessId = getBusinessId();

        // Require business context for login - prevents cross-tenant access
        if (businessId == null) {
            throw new UnauthorizedException("Business context required for login");
        }

        // Find user by email within the specific business
        User user = userRepository.findByBusinessIdAndEmail(businessId, email)
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
        Long businessId = getBusinessId();

        // Check if email already exists within this business
        if (userRepository.existsByBusinessIdAndEmail(businessId, request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

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
        user.setBusiness(business);

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
    @Transactional(rollbackFor = Exception.class)
    public User registerAdmin(AdminRegisterRequest request) {
        // Validate email format first
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid email format");
        }

        // For admin registration, check globally since business doesn't exist yet
        boolean emailExists = rlsBypass.executeWithoutRls(() ->
                userRepository.findByEmail(request.getEmail()).isPresent());
        if (emailExists) {
            throw new RuntimeException("Email already registered");
        }

        // Normalize phone number
        String normalizedPhone;
        try {
            normalizedPhone = PhoneNumberUtil.normalizePhoneNumber(request.getPhone(), "353");
        } catch (InvalidPhoneNumberException e) {
            throw new BadRequestException("Invalid phone number: " + e.getMessage());
        }

        // Create business first
        Business business = new Business();
        business.setName(request.getBusinessName());
        Business savedBusiness = businessRepository.save(business);

        // Create new user with ADMIN role
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(normalizedPhone);
        user.setRole(User.Role.ADMIN);
        user.setBusiness(savedBusiness);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Update business with admin user
        savedBusiness.setAdminUser(savedUser);
        businessRepository.save(savedBusiness);

        return savedUser;
    }
}