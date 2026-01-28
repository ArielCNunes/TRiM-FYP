package com.trim.booking.service.barber;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.InvalidPhoneNumberException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.repository.UserRepository;
import com.trim.booking.tenant.TenantContext;
import com.trim.booking.util.PhoneNumberUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BarberService {
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public BarberService(BarberRepository barberRepository, UserRepository userRepository, BusinessRepository businessRepository) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    // All or nothing transaction to ensure both User and Barber are created
    @Transactional
    public Barber createBarber(String firstName, String lastName, String email,
                               String phone, String password, String bio, String profileImageUrl) {

        Long businessId = getBusinessId();

        // Check if email already exists within this business
        if (userRepository.existsByBusinessIdAndEmail(businessId, email)) {
            throw new RuntimeException("Email already registered");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        // Normalize phone number
        String normalizedPhone;
        try {
            normalizedPhone = PhoneNumberUtil.normalizePhoneNumber(phone, "353");
        } catch (InvalidPhoneNumberException e) {
            throw new BadRequestException("Invalid phone number: " + e.getMessage());
        }

        // Create User account with BARBER role
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.Role.BARBER);
        user.setBusiness(business);

        User savedUser = userRepository.save(user);

        // Create Barber profile
        Barber barber = new Barber();
        barber.setUser(savedUser);
        barber.setBio(bio);
        barber.setProfileImageUrl(profileImageUrl);
        barber.setActive(true);
        barber.setBusiness(business);

        return barberRepository.save(barber);
    }

    public List<Barber> getAllBarbers() {
        return barberRepository.findByBusinessId(getBusinessId());
    }

    public List<Barber> getActiveBarbers() {
        return barberRepository.findByBusinessIdAndActiveTrue(getBusinessId());
    }

    public Optional<Barber> getBarberById(Long id) {
        return barberRepository.findByIdAndBusinessId(id, getBusinessId());
    }

    public Barber updateBarber(Long id, String firstName, String lastName, String email, String phone, String bio, String profileImageUrl) {
        return barberRepository.findByIdAndBusinessId(id, getBusinessId())
                .map(barber -> {
                    if (firstName != null) barber.getUser().setFirstName(firstName);
                    if (lastName != null) barber.getUser().setLastName(lastName);
                    if (email != null) barber.getUser().setEmail(email);
                    if (phone != null) barber.getUser().setPhone(phone);
                    if (bio != null) barber.setBio(bio);
                    if (profileImageUrl != null) barber.setProfileImageUrl(profileImageUrl);
                    return barberRepository.save(barber);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + id));
    }

    public void deactivateBarber(Long id) {
        barberRepository.findByIdAndBusinessId(id, getBusinessId())
                .map(barber -> {
                    barber.setActive(false);
                    return barberRepository.save(barber);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + id));
    }

    public void reactivateBarber(Long id) {
        barberRepository.findByIdAndBusinessId(id, getBusinessId())
                .map(barber -> {
                    barber.setActive(true);
                    return barberRepository.save(barber);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + id));
    }

    public void deleteBarber(Long id) {
        Barber barber = barberRepository.findByIdAndBusinessId(id, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + id));
        barberRepository.delete(barber);
    }
}