package com.trim.booking.service.notification;

import com.trim.booking.config.TwilioConfig;
import com.trim.booking.entity.Booking;
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
            System.err.println("Failed to send SMS: " + e.getMessage());
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

            String messageBody = buildReminderSms(booking);

            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            System.out.println("SMS reminder sent to: " + toPhone +
                    " (SID: " + message.getSid() + ")");

        } catch (Exception e) {
            System.err.println("Failed to send reminder SMS: " + e.getMessage());
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