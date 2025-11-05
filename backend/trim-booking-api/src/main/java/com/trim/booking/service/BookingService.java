package com.trim.booking.service;

import com.trim.booking.entity.*;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    // Unused but kept for future use (email/SMS notifications, payment processing)
    // private final AvailabilityService availabilityService;
    // private final EmailService emailService;
    // private final SmsService smsService;
    // private final PaymentService paymentService;
    // private final PaymentRepository paymentRepository;
    private final GuestUserService guestUserService;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          BarberRepository barberRepository,
                          ServiceRepository serviceRepository,
                          GuestUserService guestUserService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.guestUserService = guestUserService;
    }

    /**
     * Create a new booking with race condition protection.
     * <p>
     * Uses pessimistic locking to prevent two customers from booking the same slot simultaneously.
     * Transaction isolation ensures atomicity.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(Long customerId, Long barberId, Long serviceId, LocalDate bookingDate, LocalTime startTime, String paymentMethod) {

        // Step 1: Validate entities exist
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barber not found with ID: " + barberId));

        ServiceOffered service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));

        // Step 2: Calculate end time
        LocalTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // Step 3: Get existing bookings with pessimistic lock
        // This prevents other transactions from reading these bookings until we're done
        List<Booking> existingBookings = bookingRepository
                .findByBarberIdAndBookingDateWithLock(barberId, bookingDate);

        // Step 4: Check for conflicts manually
        for (Booking existing : existingBookings) {
            // Skip cancelled bookings
            if (existing.getStatus() == Booking.BookingStatus.CANCELLED) {
                continue;
            }

            // Check for time overlap
            boolean hasOverlap = startTime.isBefore(existing.getEndTime()) &&
                    endTime.isAfter(existing.getStartTime());

            if (hasOverlap) {
                throw new ConflictException("This time slot is no longer available");
            }
        }

        // Step 5: Create the booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);

        // Deposit amounts will be set by PaymentService
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setOutstandingBalance(service.getPrice());

        try {
            return bookingRepository.save(booking);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create booking - slot may have been taken");
        }
    }

    /**
     * Create a booking for a guest user.
     * This method creates a guest user account first, then creates the booking.
     *
     * @param firstName Customer's first name
     * @param lastName Customer's last name
     * @param email Customer's email
     * @param phone Customer's phone
     * @param barberId Barber ID
     * @param serviceId Service ID
     * @param bookingDate Booking date
     * @param startTime Start time
     * @param paymentMethod Payment method (pay_online or pay_in_shop)
     * @return Booking object
     * @throws ConflictException if email already exists or time slot unavailable
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createGuestBooking(String firstName, String lastName, String email, String phone,
                                       Long barberId, Long serviceId, LocalDate bookingDate,
                                       LocalTime startTime, String paymentMethod) {

        // Step 1: Create guest user via service
        User guestCustomer = guestUserService.createGuestUser(firstName, lastName, email, phone);

        // Step 2: Create booking using the guest user
        return createBooking(guestCustomer.getId(), barberId, serviceId, bookingDate, startTime, paymentMethod);
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
     * Get barber's bookings for a specific date.
     */
    public List<Booking> getBarberScheduleForDate(Long barberId, LocalDate date) {
        return bookingRepository.findByBarberIdAndBookingDate(barberId, date);
    }

    /**
     * Mark booking as completed.
     * Only the assigned barber or admin can do this.
     */
    @Transactional
    public Booking markAsCompleted(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    /**
     * Mark booking as no-show.
     * Only the assigned barber or admin can do this.
     */
    @Transactional
    public Booking markAsNoShow(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(Booking.BookingStatus.NO_SHOW);
        return bookingRepository.save(booking);
    }

    /**
     * Cancel a booking.
     */
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if booking is already cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        // Update booking status to CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        System.out.println("Booking cancelled: " + bookingId);
    }
}