package com.trim.booking.service.reminder;

import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import com.trim.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for sending automated reminder notifications.
 * Runs scheduled jobs to remind customers about upcoming appointments.
 */
@Service
public class ReminderService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    public ReminderService(BookingRepository bookingRepository, EmailService emailService, SmsService smsService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Send reminders for all bookings happening tomorrow.
     * Runs every day at 10:00 AM.
     * Sets tenant context for each booking to ensure proper isolation.
     * <p>
     * Cron format: second minute hour day month weekday
     * "0 0 10 * * ?" = At 10:00 AM every day
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        logger.info("Running daily reminder job for bookings on: {}", tomorrow);

        // Get all bookings for tomorrow (ordered by business_id)
        List<Booking> tomorrowBookings = bookingRepository.findByBookingDate(tomorrow);

        int remindersSent = 0;

        for (Booking booking : tomorrowBookings) {
            // Only send reminders for confirmed/pending bookings
            if (booking.getStatus() == Booking.BookingStatus.PENDING ||
                    booking.getStatus() == Booking.BookingStatus.CONFIRMED) {

                try {
                    // Set tenant context for this booking's business
                    TenantContext.setCurrentBusiness(
                            booking.getBusiness().getId(),
                            booking.getBusiness().getSlug()
                    );

                    sendReminder(booking);
                    remindersSent++;

                } finally {
                    TenantContext.clear();
                }
            }
        }

        // Log summary without sensitive data
        logger.info("Daily reminder job completed: sent {} reminders for {}", remindersSent, tomorrow);
    }

    /**
     * Send reminder email and SMS to customer.
     */
    private void sendReminder(Booking booking) {
        try {
            // Send reminder email
            emailService.sendReminderEmail(booking);

            // Send reminder SMS
            smsService.sendReminderSms(booking);

            // Log without sensitive customer data (no email/phone)
            logger.debug("Reminder sent for booking: id={}, businessId={}",
                    booking.getId(), booking.getBusiness().getId());

        } catch (Exception e) {
            // Log error without exposing customer PII
            logger.error("Failed to send reminder for booking: id={}, businessId={}, error={}",
                    booking.getId(), booking.getBusiness().getId(), e.getMessage());
        }
    }
}