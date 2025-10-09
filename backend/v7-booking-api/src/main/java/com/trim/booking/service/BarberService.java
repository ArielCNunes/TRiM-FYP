package com.trim.booking.service;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BarberService {
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public BarberService(BarberRepository barberRepository, UserRepository userRepository) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // All or nothing transaction to ensure both User and Barber are created
    @Transactional
    public Barber createBarber(String firstName, String lastName, String email,
                               String phone, String password, String bio, String profileImageUrl) {

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Create User account with BARBER role
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.Role.BARBER);

        User savedUser = userRepository.save(user);

        // Create Barber profile
        Barber barber = new Barber();
        barber.setUser(savedUser);
        barber.setBio(bio);
        barber.setProfileImageUrl(profileImageUrl);
        barber.setActive(true);

        return barberRepository.save(barber);
    }

    public List<Barber> getAllBarbers() {
        return barberRepository.findAll();
    }

    public List<Barber> getActiveBarbers() {
        return barberRepository.findByActiveTrue();
    }

    public Optional<Barber> getBarberById(Long id) {
        return barberRepository.findById(id);
    }

    public Barber updateBarber(Long id, String bio, String profileImageUrl) {
        return barberRepository.findById(id)
                .map(barber -> {
                    if (bio != null) barber.setBio(bio);
                    if (profileImageUrl != null) barber.setProfileImageUrl(profileImageUrl);
                    return barberRepository.save(barber);
                })
                .orElseThrow(() -> new RuntimeException("Barber not found with id: " + id));
    }

    public void deactivateBarber(Long id) {
        barberRepository.findById(id)
                .map(barber -> {
                    barber.setActive(false);
                    return barberRepository.save(barber);
                })
                .orElseThrow(() -> new RuntimeException("Barber not found with id: " + id));
    }

    public void deleteBarber(Long id) {
        barberRepository.deleteById(id);
    }
}