package com.trim.booking.service.reminder;

import com.trim.booking.entity.Booking;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for sending automated reminder notifications.
 * Runs scheduled jobs to remind customers about upcoming appointments.
 */
@Service
public class ReminderService {
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
     * <p>
     * Cron format: second minute hour day month weekday
     * "0 0 10 * * ?" = At 10:00 AM every day
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        System.out.println("Running daily reminder job for bookings on: " + tomorrow);

        // Get all bookings for tomorrow
        List<Booking> tomorrowBookings = bookingRepository.findByBookingDate(tomorrow);

        int remindersSent = 0;

        for (Booking booking : tomorrowBookings) {
            // Only send reminders for confirmed/pending bookings
            if (booking.getStatus() == Booking.BookingStatus.PENDING ||
                    booking.getStatus() == Booking.BookingStatus.CONFIRMED) {

                sendReminder(booking);
                remindersSent++;
            }
        }

        // Log summary
        System.out.println("Sent " + remindersSent + " reminders for " + tomorrow);
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

            System.out.println("Reminder sent to: " + booking.getCustomer().getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send reminder for booking " + booking.getId() + ": " + e.getMessage());
        }
    }
}