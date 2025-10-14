package com.trim.booking.service;

import com.trim.booking.entity.Booking;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications.
 * Uses @Async to send emails in background without blocking API responses.
 */
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send booking confirmation email to customer.
     * Runs asynchronously so API response isn't delayed.
     *
     * @param booking The booking details
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // Recipient
            message.setTo(booking.getCustomer().getEmail());

            // Subject
            message.setSubject("Booking Confirmation - " + booking.getService().getName());

            // Email body
            String emailBody = buildConfirmationEmail(booking);
            message.setText(emailBody);

            // Sender
            message.setFrom("noreply.trim.bookings@gmail.com");

            // Send email
            mailSender.send(message);

            System.out.println("Confirmation email sent to: " + booking.getCustomer().getEmail());

        } catch (Exception e) {
            // Don't fail the booking if email fails
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Build the email body with booking details.
     */
    private String buildConfirmationEmail(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
                        Hi %s,
                        
                        Your booking has been confirmed!
                        
                        Booking Details:
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        Service: %s
                        Barber: %s %s
                        Date: %s
                        Time: %s - %s
                        Price: €%.2f
                        Status: %s
                        
                        We look forward to seeing you!
                        
                        Best regards.
                        """,
                booking.getCustomer().getFirstName(),
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBarber().getUser().getLastName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter),
                booking.getEndTime().format(timeFormatter),
                booking.getService().getPrice(),
                booking.getStatus()
        );
    }

    /**
     * Send appointment reminder email (24 hours before).
     */
    @Async
    public void sendReminderEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(booking.getCustomer().getEmail());
            message.setSubject("Reminder: Tomorrow's Appointment - " + booking.getService().getName());

            String emailBody = buildReminderEmail(booking);
            message.setText(emailBody);
            message.setFrom("noreply.trim.bookings@gmail.com");

            mailSender.send(message);

            // Log success
            System.out.println("Reminder email sent to: " + booking.getCustomer().getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
        }
    }

    /**
     * Build reminder email body.
     */
    private String buildReminderEmail(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
                        Hi %s,
                        
                        This is a friendly reminder about your appointment tomorrow!
                        
                        Appointment Details:
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        Service: %s
                        Barber: %s %s
                        Date: %s
                        Time: %s - %s
                        Price: €%.2f
                        
                        Please arrive 5 minutes early.
                        
                        If you need to cancel, please do so at least 24 hours in advance.
                        
                        See you tomorrow!
                        
                        Best regards!
                        """,
                booking.getCustomer().getFirstName(),
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBarber().getUser().getLastName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter),
                booking.getEndTime().format(timeFormatter),
                booking.getService().getPrice()
        );
    }
}