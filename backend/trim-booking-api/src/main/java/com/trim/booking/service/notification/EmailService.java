package com.trim.booking.service.notification;

import com.trim.booking.entity.Booking;
import com.trim.booking.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    /**
     * Send password reset email with reset token link.
     * Runs asynchronously so API response isn't delayed.
     * Uses HTML email with a professional button.
     *
     * @param user       The user requesting password reset
     * @param resetToken The generated reset token
     */
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Recipient
            helper.setTo(user.getEmail());

            // Subject
            helper.setSubject("Reset Your TRiM Account Password");

            // Email body (HTML)
            String emailBody = buildPasswordResetEmailHtml(user, resetToken);
            helper.setText(emailBody, true); // true = HTML content

            // Sender
            helper.setFrom("noreply.trim.bookings@gmail.com");

            // Send email
            mailSender.send(mimeMessage);

            System.out.println("Password reset email sent to: " + user.getEmail());

        } catch (MessagingException e) {
            // Don't fail the request if email fails
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Build the password reset email body as HTML.
     * Uses inline CSS for compatibility with email clients.
     */
    private String buildPasswordResetEmailHtml(User user, String resetToken) {
        // Path parameter format: /reset-password/{token} NOT query parameter (?token=)
        String resetUrl = "http://localhost:3000/reset-password/" + resetToken;

        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td align="center" style="padding: 40px 0;">
                                        <table role="presentation" style="width: 600px; max-width: 100%%; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                            <!-- Header -->
                                            <tr>
                                                <td style="padding: 40px 40px 20px 40px; text-align: center; background-color: #1e40af; border-radius: 8px 8px 0 0;">
                                                    <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">TRiM</h1>
                                                </td>
                                            </tr>
                        
                                            <!-- Content -->
                                            <tr>
                                                <td style="padding: 40px;">
                                                    <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px;">Hi %s,</h2>
                        
                                                    <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        We received a request to reset your password.
                                                    </p>
                        
                                                    <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        Click the button below to reset your password:
                                                    </p>
                        
                                                    <!-- Button -->
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                        <tr>
                                                            <td align="center" style="padding: 0 0 30px 0;">
                                                                <a href="%s" 
                                                                   style="display: inline-block; padding: 16px 40px; background-color: #1e40af; color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: bold; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                                                    Reset Password
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </table>
                        
                                                    <p style="margin: 0 0 15px 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                        <strong>This link will expire in 1 hour</strong> for security reasons.
                                                    </p>
                        
                                                    <p style="margin: 0 0 20px 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                        If the button doesn't work, copy and paste this link into your browser:
                                                    </p>
                        
                                                    <p style="margin: 0 0 30px 0; padding: 15px; background-color: #f8f9fa; border-radius: 4px; word-break: break-all; color: #666666; font-size: 13px;">
                                                        %s
                                                    </p>
                        
                                                    <p style="margin: 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                        If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
                                                    </p>
                                                </td>
                                            </tr>
                        
                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center;">
                                                    <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                                        Best regards,<br>
                                                        <strong>TRiM</strong>
                                                    </p>
                                                    <p style="margin: 0; color: #999999; font-size: 12px;">
                                                        This is an automated message, please do not reply to this email.
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """,
                user.getFirstName(),
                resetUrl,
                resetUrl
        );
    }
}