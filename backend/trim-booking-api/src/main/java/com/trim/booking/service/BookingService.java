package com.trim.booking.service;

import com.trim.booking.entity.*;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.exception.ConflictException;
import com.trim.booking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityService availabilityService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          BarberRepository barberRepository,
                          ServiceRepository serviceRepository,
                          AvailabilityService availabilityService, EmailService emailService, SmsService smsService, PaymentService paymentService, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityService = availabilityService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
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
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // Set payment status based on payment method
        if ("pay_in_shop".equalsIgnoreCase(paymentMethod)) {
            booking.setPaymentStatus(Booking.PaymentStatus.PAY_IN_SHOP);
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        } else {
            // Online payment - will be handled by payment service
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
            booking.setStatus(Booking.BookingStatus.PENDING);
        }

        // Save to database - unique constraint will catch any duplicates that slipped through
        try {
            Booking savedBooking = bookingRepository.save(booking);

            // Send confirmation email asynchronously
            emailService.sendBookingConfirmation(savedBooking);

            // Send SMS notification asynchronously
            smsService.sendBookingConfirmation(savedBooking);
            return savedBooking;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create booking - slot may have been taken");
        }
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
        List<Booking> allBookings = bookingRepository.findByBarberIdAndBookingDate(barberId, date);
        return allBookings;
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

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // If booking was paid online (via Stripe), process refund
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            try {
                // Check if payment exists (only for online payments)
                Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);
                if (paymentOpt.isPresent()) {
                    // Online payment - process Stripe refund
                    paymentService.processRefund(bookingId);
                    System.out.println("Refund processed for booking: " + bookingId);
                } else {
                    // Pay-in-shop marked as paid - just update status, no refund
                    bookingRepository.save(booking);
                    System.out.println("Booking cancelled (pay-in-shop, no refund): " + bookingId);
                }
            } catch (Exception e) {
                System.err.println("Failed to process refund: " + e.getMessage());
                throw new RuntimeException("Failed to process refund: " + e.getMessage());
            }
        } else {
            // No refund needed for unpaid bookings
            bookingRepository.save(booking);
        }

        System.out.println("Booking cancelled: " + bookingId);
    }
}