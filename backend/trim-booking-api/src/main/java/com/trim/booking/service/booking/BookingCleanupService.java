package com.trim.booking.service.booking;

import com.trim.booking.config.RlsBypass;
import com.trim.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled service to clean up expired pending bookings.
 * Runs every 5 minutes to mark expired bookings as CANCELLED.
 */
@Service
public class BookingCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);

    private final BookingRepository bookingRepository;
    private final RlsBypass rlsBypass;

    public BookingCleanupService(BookingRepository bookingRepository, RlsBypass rlsBypass) {
        this.bookingRepository = bookingRepository;
        this.rlsBypass = rlsBypass;
    }

    /**
     * Auto-cancel expired pending bookings.
     * Runs every 5 minutes (300,000 milliseconds).
     * Uses a single bulk UPDATE instead of loading and saving each booking individually.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cancelExpiredBookings() {
        int cancelledCount = rlsBypass.executeWithoutRls(
                bookingRepository::cancelAllExpiredPendingBookings);

        if (cancelledCount > 0) {
            logger.info("Booking cleanup completed: cancelled {} expired bookings", cancelledCount);
        }
    }
}
