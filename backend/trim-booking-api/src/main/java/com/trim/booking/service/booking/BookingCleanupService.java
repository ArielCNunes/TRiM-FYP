package com.trim.booking.service.booking;

import com.trim.booking.config.RlsBypass;
import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * Sets tenant context for each booking to ensure proper isolation.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cancelExpiredBookings() {
        List<Booking> expiredBookings = rlsBypass.executeWithoutRls(
                bookingRepository::findExpiredPendingBookings);

        int cancelledCount = 0;

        for (Booking booking : expiredBookings) {
            try {
                // Set tenant context for this booking's business
                TenantContext.setCurrentBusiness(
                        booking.getBusiness().getId(),
                        booking.getBusiness().getSlug()
                );

                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
                bookingRepository.save(booking);

                // Log without sensitive customer data
                logger.info("Auto-cancelled expired booking: id={}, businessId={}",
                        booking.getId(), booking.getBusiness().getId());
                cancelledCount++;

            } catch (Exception e) {
                logger.error("Failed to cancel expired booking: id={}, error={}",
                        booking.getId(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        if (cancelledCount > 0) {
            logger.info("Booking cleanup completed: cancelled {} expired bookings", cancelledCount);
        }
    }
}

