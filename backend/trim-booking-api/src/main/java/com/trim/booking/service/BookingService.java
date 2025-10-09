package com.trim.booking.service;

import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityService availabilityService;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          BarberRepository barberRepository,
                          ServiceRepository serviceRepository,
                          AvailabilityService availabilityService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityService = availabilityService;
    }

    /**
     * Create a new booking.
     * <p>
     * Steps:
     * 1. Validate customer, barber, and service exist
     * 2. Calculate end time based on service duration
     * 3. Check if the time slot is still available
     * 4. Create and save the booking
     *
     * @Transactional ensures atomicity - either all operations succeed or none do
     */
    @Transactional
    public Booking createBooking(Long customerId, Long barberId, Long serviceId,
                                 LocalDate bookingDate, LocalTime startTime) {

        // Step 1: Validate entities exist
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        ServiceOffered service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Step 2: Calculate end time
        LocalTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // Step 3: Check if slot is available
        // Get all available slots for this date/service/barber
        List<LocalTime> availableSlots = availabilityService.getAvailableSlots(
                barberId, bookingDate, serviceId);

        if (!availableSlots.contains(startTime)) {
            throw new RuntimeException("Selected time slot is not available");
        }

        // Step 4: Create the booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // Save to database
        return bookingRepository.save(booking);
    }

    /**
     * Get all bookings for a customer.
     */
    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    /**
     * Get all bookings for a barber.
     */
    public List<Booking> getBarberBookings(Long barberId) {
        return bookingRepository.findByBarberId(barberId);
    }

    /**
     * Get a specific booking by ID.
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    /**
     * Cancel a booking.
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}