package com.trim.booking.service.notification;

import com.trim.booking.entity.Booking;
import com.trim.booking.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications.
 * Uses @Async to send emails in background without blocking API responses.
 */
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-domain}")
    private String baseDomain;

    @Value("${app.frontend-port:3000}")
    private String frontendPort;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send booking confirmation email to customer.
     * Runs asynchronously so API response isn't delayed.
     * Uses HTML email with professional styling.
     *
     * @param booking The booking details
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Recipient
            helper.setTo(booking.getCustomer().getEmail());

            // Subject
            helper.setSubject("Booking Confirmation - " + booking.getService().getName());

            // Email body (HTML)
            String emailBody = buildConfirmationEmailHtml(booking);
            helper.setText(emailBody, true); // true = HTML content

            // Sender
            helper.setFrom("noreply.trim.bookings@gmail.com");

            // Send email
            mailSender.send(mimeMessage);

            System.out.println("Confirmation email sent to: " + booking.getCustomer().getEmail());

        } catch (MessagingException e) {
            // Don't fail the booking if email fails
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Build the booking confirmation email body as HTML.
     * Uses inline CSS for compatibility with email clients.
     */
    private String buildConfirmationEmailHtml(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

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
                                                <td style="padding: 40px 40px 20px 40px; text-align: center; background-color: #10b981; border-radius: 8px 8px 0 0;">
                                                    <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">Booking Confirmed!</h1>
                                                </td>
                                            </tr>
                        
                                            <!-- Content -->
                                            <tr>
                                                <td style="padding: 40px;">
                                                    <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px;">Hi %s,</h2>
                        
                                                    <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        Great news! Your booking has been confirmed. We're looking forward to seeing you!
                                                    </p>
                        
                                                    <!-- Booking Details Card -->
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f8f9fa; border-radius: 6px; margin-bottom: 30px;">
                                                        <tr>
                                                            <td style="padding: 25px;">
                                                                <h3 style="margin: 0 0 20px 0; color: #1e40af; font-size: 18px; font-weight: bold;">Booking Details</h3>
                        
                                                                <!-- Service -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Service:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Barber -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Barber:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s %s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Date -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Date:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Time -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Time:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s - %s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Price -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Price:</td>
                                                                        <td style="padding: 8px 0; color: #10b981; font-size: 16px; font-weight: bold;">€%.2f</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Status -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Status:</td>
                                                                        <td style="padding: 8px 0;">
                                                                            <span style="display: inline-block; padding: 4px 12px; background-color: #10b981; color: #ffffff; border-radius: 12px; font-size: 12px; font-weight: bold;">%s</span>
                                                                        </td>
                                                                    </tr>
                                                                </table>
                                                            </td>
                                                        </tr>
                                                    </table>
                        
                                                    <!-- Important Notice -->
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #fff8e1; border-left: 4px solid #fbbf24; border-radius: 4px; margin-bottom: 20px;">
                                                        <tr>
                                                            <td style="padding: 15px;">
                                                                <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 1.6;">
                                                                    <strong>Please arrive 5 minutes early.</strong><br>
                                                                    If you need to cancel or reschedule, please do so at least 24 hours in advance.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                        
                                                    <p style="margin: 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        We look forward to seeing you!
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
                                                        This is an automated confirmation email.
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
     * Uses HTML email with professional styling.
     */
    @Async
    public void sendReminderEmail(Booking booking) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(booking.getCustomer().getEmail());
            helper.setSubject("Reminder: Tomorrow's Appointment - " + booking.getService().getName());

            String emailBody = buildReminderEmailHtml(booking);
            helper.setText(emailBody, true); // true = HTML content
            helper.setFrom("noreply.trim.bookings@gmail.com");

            mailSender.send(mimeMessage);

            // Log success
            System.out.println("Reminder email sent to: " + booking.getCustomer().getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
        }
    }

    /**
     * Build reminder email body as HTML.
     * Uses inline CSS for compatibility with email clients.
     */
    private String buildReminderEmailHtml(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

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
                                                <td style="padding: 40px 40px 20px 40px; text-align: center; background-color: #f59e0b; border-radius: 8px 8px 0 0;">
                                                    <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">⏰ Appointment Reminder</h1>
                                                </td>
                                            </tr>
                        
                                            <!-- Content -->
                                            <tr>
                                                <td style="padding: 40px;">
                                                    <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px;">Hi %s,</h2>
                        
                                                    <p style="margin: 0 0 30px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        This is a friendly reminder about your <strong>appointment tomorrow</strong>!
                                                    </p>
                        
                                                    <!-- Appointment Details Card -->
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f8f9fa; border-radius: 6px; margin-bottom: 30px;">
                                                        <tr>
                                                            <td style="padding: 25px;">
                                                                <h3 style="margin: 0 0 20px 0; color: #f59e0b; font-size: 18px; font-weight: bold;">Appointment Details</h3>
                        
                                                                <!-- Service -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Service:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Barber -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Barber:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">%s %s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Date -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Date:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px; font-weight: bold;">%s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Time -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse; margin-bottom: 15px;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Time:</td>
                                                                        <td style="padding: 8px 0; color: #f59e0b; font-size: 16px; font-weight: bold;">%s - %s</td>
                                                                    </tr>
                                                                </table>
                        
                                                                <!-- Price -->
                                                                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                                    <tr>
                                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px; font-weight: bold; width: 120px;">Price:</td>
                                                                        <td style="padding: 8px 0; color: #333333; font-size: 14px;">€%.2f</td>
                                                                    </tr>
                                                                </table>
                                                            </td>
                                                        </tr>
                                                    </table>
                        
                                                    <!-- Important Notice -->
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 4px; margin-bottom: 20px;">
                                                        <tr>
                                                            <td style="padding: 15px;">
                                                                <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 1.6;">
                                                                    <strong>⏰ Please arrive 5 minutes early.</strong><br>
                                                                    If you need to cancel or reschedule, please contact us as soon as possible.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                        
                                                    <p style="margin: 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                        See you tomorrow! 👋
                                                    </p>
                                                </td>
                                            </tr>
                        
                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center;">
                                                    <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                                        Best regards,<br>
                                                        <strong>TRiM Booking Team</strong>
                                                    </p>
                                                    <p style="margin: 0; color: #999999; font-size: 12px;">
                                                        This is an automated reminder email.
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
        // Build reset URL with business subdomain for proper tenant context
        String businessSlug = user.getBusiness() != null ? user.getBusiness().getSlug() : null;
        String resetUrl;
        if (businessSlug != null) {
            // Include subdomain so tenant context is set when user clicks the link
            resetUrl = "http://" + businessSlug + "." + baseDomain + ":" + frontendPort + "/reset-password/" + resetToken;
        } else {
            // Fallback (shouldn't happen in normal flow)
            resetUrl = "http://" + baseDomain + ":" + frontendPort + "/reset-password/" + resetToken;
        }

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