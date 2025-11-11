package com.trim.booking.service.booking;

import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled service to clean up expired pending bookings.
 * Runs every 5 minutes to mark expired bookings as CANCELLED.
 */
@Service
public class BookingCleanupService {
    private final BookingRepository bookingRepository;

    public BookingCleanupService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Auto-cancel expired pending bookings.
     * Runs every 5 minutes (300,000 milliseconds).
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cancelExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings();

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
            bookingRepository.save(booking);

            System.out.println("Auto-cancelled expired booking: " + booking.getId());
        }

        if (!expiredBookings.isEmpty()) {
            System.out.println("Cleaned up " + expiredBookings.size() + " expired bookings");
        }
    }
}

