package com.trim.booking.service.notification;

import com.trim.booking.config.TwilioConfig;
import com.trim.booking.entity.Booking;
import com.trim.booking.util.PhoneNumberUtil;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending SMS notifications via Twilio.
 * Uses @Async to send SMS in background without blocking API responses.
 */
@Service
public class SmsService {
    private final TwilioConfig twilioConfig;

    public SmsService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    /**
     * Send booking confirmation SMS to customer.
     * Runs asynchronously so API response isn't delayed.
     *
     * @param booking The booking details
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            String toPhone = booking.getCustomer().getPhone();

            // Validate and normalize phone number as safety check
            if (!PhoneNumberUtil.validateE164Format(toPhone)) {
                System.err.println("Warning: Phone number not in E.164 format, attempting to normalize: " + toPhone);
                try {
                    toPhone = PhoneNumberUtil.normalizePhoneNumber(toPhone, "353");
                    System.out.println("Successfully normalized phone to: " + toPhone);
                } catch (Exception normalizationError) {
                    System.err.println("Failed to normalize phone for booking ID: " + booking.getId() +
                                     ", customer email: " + booking.getCustomer().getEmail() +
                                     ", phone: " + toPhone +
                                     ", error: " + normalizationError.getMessage());
                    // Skip sending SMS with invalid number
                    return;
                }
            }

            String messageBody = buildConfirmationSms(booking);

            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            System.out.println("SMS sent to: " + toPhone +
                    " (SID: " + message.getSid() + ")");

        } catch (Exception e) {
            // Don't fail the booking if SMS fails
            System.err.println("Failed to send SMS for booking ID: " +
                             (booking != null ? booking.getId() : "unknown") +
                             ", error: " + e.getMessage());
        }
    }

    /**
     * Build the SMS body with booking details.
     */
    private String buildConfirmationSms(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format(
                "TRiM Booking confirmed!\n%s with %s\n%s at %s\nSee you soon!",
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter)
        );
    }

    /**
     * Send appointment reminder SMS (24 hours before).
     */
    @Async
    public void sendReminderSms(Booking booking) {
        try {
            String toPhone = booking.getCustomer().getPhone();

            // Validate and normalize phone number as safety check
            if (!PhoneNumberUtil.validateE164Format(toPhone)) {
                System.err.println("Warning: Phone number not in E.164 format, attempting to normalize: " + toPhone);
                try {
                    toPhone = PhoneNumberUtil.normalizePhoneNumber(toPhone, "353");
                    System.out.println("Successfully normalized phone to: " + toPhone);
                } catch (Exception normalizationError) {
                    System.err.println("Failed to normalize phone for booking ID: " + booking.getId() +
                                     ", customer email: " + booking.getCustomer().getEmail() +
                                     ", phone: " + toPhone +
                                     ", error: " + normalizationError.getMessage());
                    // Skip sending SMS with invalid number
                    return;
                }
            }

            String messageBody = buildReminderSms(booking);

            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            System.out.println("SMS reminder sent to: " + toPhone +
                    " (SID: " + message.getSid() + ")");

        } catch (Exception e) {
            System.err.println("Failed to send reminder SMS for booking ID: " +
                             (booking != null ? booking.getId() : "unknown") +
                             ", error: " + e.getMessage());
        }
    }

    /**
     * Build reminder SMS body.
     */
    private String buildReminderSms(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format(
                "Reminder: Your appointment is tomorrow!\n%s with %s\n%s at %s\nPlease arrive 5 min early.",
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter)
        );
    }
}