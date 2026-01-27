package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.exception.ResourceNotFoundException;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service responsible for handling booking status transitions.
 * Single responsibility: status management logic.
 */
@Service
public class BookingStatusService {
    private final BookingRepository bookingRepository;

    public BookingStatusService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
    }

    /**
     * Mark booking as completed.
     * Only the assigned barber or admin can do this.
     *
     * @param bookingId Booking ID
     * @return Updated booking
     */
    @Transactional
    public Booking markAsCompleted(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        validateCanComplete(booking);

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
        booking.setDepositAmount(booking.getService().getPrice());
        booking.setOutstandingBalance(BigDecimal.ZERO);

        return bookingRepository.save(booking);
    }

    /**
     * Mark booking as fully paid.
     * Used when customer pays the outstanding balance in the shop.
     * Only admin or barber can do this.
     *
     * @param bookingId Booking ID
     * @return Updated booking
     */
    @Transactional
    public Booking markAsPaid(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        validateCanMarkPaid(booking);

        booking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
        booking.setDepositAmount(booking.getService().getPrice());
        booking.setOutstandingBalance(BigDecimal.ZERO);

        return bookingRepository.save(booking);
    }

    /**
     * Mark booking as no-show.
     * Only the assigned barber or admin can do this.
     *
     * @param bookingId Booking ID
     * @return Updated booking
     */
    @Transactional
    public Booking markAsNoShow(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        validateCanMarkNoShow(booking);

        booking.setStatus(Booking.BookingStatus.NO_SHOW);

        return bookingRepository.save(booking);
    }

    /**
     * Cancel a booking.
     *
     * @param bookingId Booking ID
     * @return Updated booking
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        validateCanCancel(booking);

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        System.out.println("Booking cancelled: " + bookingId);

        return bookingRepository.save(booking);
    }

    /**
     * Confirm a booking (after deposit payment).
     *
     * @param bookingId Booking ID
     * @return Updated booking
     */
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);

        return bookingRepository.save(booking);
    }

    // Validation methods

    private void validateCanComplete(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled booking");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking is already completed");
        }
    }

    private void validateCanMarkPaid(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot mark cancelled booking as paid");
        }
        if (booking.getPaymentStatus() == Booking.PaymentStatus.FULLY_PAID) {
            throw new IllegalStateException("Booking is already fully paid");
        }
    }

    private void validateCanMarkNoShow(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot mark cancelled booking as no-show");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot mark completed booking as no-show");
        }
    }

    private void validateCanCancel(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed booking");
        }
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findByIdAndBusinessId(bookingId, getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));
    }
}

