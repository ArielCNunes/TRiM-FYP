package com.trim.booking.service.booking;

import com.trim.booking.dto.booking.UpdateBookingRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.entity.User;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.exception.ForbiddenException;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service responsible for booking create/update operations
 * Single responsibility: creating and modifying bookings.
 */
@Service
public class BookingCommandService {
    private final BookingRepository bookingRepository;
    private final BookingValidationService validationService;
    private final BookingConflictDetectionService conflictDetectionService;
    private final BusinessRepository businessRepository;

    public BookingCommandService(BookingRepository bookingRepository,
                                 BookingValidationService validationService,
                                 BookingConflictDetectionService conflictDetectionService,
                                 BusinessRepository businessRepository) {
        this.bookingRepository = bookingRepository;
        this.validationService = validationService;
        this.conflictDetectionService = conflictDetectionService;
        this.businessRepository = businessRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Create a new booking with race condition protection.
     * Uses pessimistic locking to prevent two customers from booking the same slot simultaneously.
     *
     * @param customerId    Customer ID
     * @param barberId      Barber ID
     * @param serviceId     Service ID
     * @param bookingDate   Booking date
     * @param startTime     Start time
     * @param paymentMethod Payment method
     * @return Created booking
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(Long customerId, Long barberId, Long serviceId,
                                 LocalDate bookingDate, LocalTime startTime,
                                 String paymentMethod) {
        // Step 1: Validate entities exist
        User customer = validationService.validateAndGetCustomer(customerId);
        Barber barber = validationService.validateAndGetBarber(barberId);
        ServiceOffered service = validationService.validateAndGetService(serviceId);

        // Step 1.1: Validate all entities belong to the same business
        validationService.validateEntitiesBelongToSameBusiness(customer, barber, service);

        // Step 1.5: Check if customer is blacklisted
        if (Boolean.TRUE.equals(customer.getBlacklisted())) {
            String reason = customer.getBlacklistReason() != null
                    ? customer.getBlacklistReason()
                    : "No reason provided";
            throw new ForbiddenException("Customer is blacklisted: " + reason);
        }

        // Step 2: Validate business rules
        validationService.validateBookingTimeInFuture(bookingDate, startTime);

        // Step 3: Calculate end time
        LocalTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // Step 4: Check for conflicts with pessimistic locking
        conflictDetectionService.validateTimeSlotAvailable(
                barberId, bookingDate, startTime, endTime);

        // Step 5: Create and save booking
        Booking booking = buildBooking(customer, barber, service,
                bookingDate, startTime, endTime);

        try {
            return bookingRepository.save(booking);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create booking - slot may have been taken");
        }
    }


    /**
     * Build a booking entity with default values.
     *
     * @param customer    Customer
     * @param barber      Barber
     * @param service     Service
     * @param bookingDate Booking date
     * @param startTime   Start time
     * @param endTime     End time
     * @return Booking entity (not yet persisted)
     */
    private Booking buildBooking(User customer, Barber barber,
                                 ServiceOffered service,
                                 LocalDate bookingDate, LocalTime startTime,
                                 LocalTime endTime) {
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);

        // Set expiry: 10 minutes from now for pending bookings
        booking.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

        // Deposit amounts will be set by PaymentService
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setOutstandingBalance(service.getPrice());

        // Set business association
        Long businessId = getBusinessId();
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        booking.setBusiness(business);

        return booking;
    }

    /**
     * Update an existing booking's date and time.
     * Only allows changing date/time to avoid payment/refund complexity.
     * Barber and service remain the same.
     *
     * @param bookingId Booking ID to update
     * @param request   Update request with new date/time
     * @return Updated booking
     * @throws ResourceNotFoundException if booking not found
     * @throws BadRequestException       if booking cannot be updated
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking updateBooking(Long bookingId, UpdateBookingRequest request) {
        // Step 1: Fetch existing booking with tenant validation
        Booking booking = bookingRepository.findByIdAndBusinessId(bookingId, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Step 2: Validate booking status - can't update completed/cancelled bookings
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot update a completed booking");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled booking");
        }

        // Step 3: Validate new date/time is in the future
        validationService.validateBookingTimeInFuture(request.getBookingDate(), request.getStartTime());

        // Step 4: Calculate new end time based on service duration
        ServiceOffered service = booking.getService();
        LocalTime newEndTime = request.getStartTime().plusMinutes(service.getDurationMinutes());

        // Step 5: Check for conflicts (excluding current booking)
        conflictDetectionService.validateTimeSlotAvailableForUpdate(
                bookingId,
                booking.getBarber().getId(),
                request.getBookingDate(),
                request.getStartTime(),
                newEndTime
        );

        // Step 6: Update booking fields
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(newEndTime);

        // Step 7: Reset expiry if it was set (give them another 10 minutes for new slot)
        if (booking.getExpiresAt() != null && booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
        }

        // Step 8: Save and return
        return bookingRepository.save(booking);
    }
}

