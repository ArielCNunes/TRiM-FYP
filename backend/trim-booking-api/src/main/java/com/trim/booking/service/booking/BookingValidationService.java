package com.trim.booking.service.booking;

import com.trim.booking.entity.Barber;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.entity.User;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.ServiceRepository;
import com.trim.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service responsible for validating booking entities and business rules.
 * Single responsibility: validation logic.
 */
@Service
public class BookingValidationService {
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;

    public BookingValidationService(UserRepository userRepository,
                                    BarberRepository barberRepository,
                                    ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Validate and retrieve customer by ID.
     *
     * @param customerId Customer ID
     * @return User entity
     * @throws ResourceNotFoundException if customer not found
     */
    public User validateAndGetCustomer(Long customerId) {
        return userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId));
    }

    /**
     * Validate and retrieve barber by ID.
     *
     * @param barberId Barber ID
     * @return Barber entity
     * @throws ResourceNotFoundException if barber not found
     */
    public Barber validateAndGetBarber(Long barberId) {
        return barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber not found with ID: " + barberId));
    }

    /**
     * Validate and retrieve service by ID.
     *
     * @param serviceId Service ID
     * @return ServiceOffered entity
     * @throws ResourceNotFoundException if service not found
     */
    public ServiceOffered validateAndGetService(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service not found with ID: " + serviceId));
    }

    /**
     * Validate booking time is in the future.
     *
     * @param bookingDate Booking date
     * @param startTime   Start time
     * @throws IllegalArgumentException if booking is in the past
     */
    public void validateBookingTimeInFuture(LocalDate bookingDate, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (bookingDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot book in the past");
        }

        if (bookingDate.isEqual(today) && startTime.isBefore(now)) {
            throw new IllegalArgumentException("Cannot book in the past");
        }
    }
}

