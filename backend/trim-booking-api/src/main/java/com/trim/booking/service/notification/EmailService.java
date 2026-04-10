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

            String businessName = booking.getBusiness().getName();

            // Recipient
            helper.setTo(booking.getCustomer().getEmail());

            // Subject
            helper.setSubject(businessName + " — Booking Confirmed");

            // Email body (HTML)
            String emailBody = buildConfirmationEmailHtml(booking, businessName);
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
     * Minimalistic black & white design.
     */
    private String buildConfirmationEmailHtml(Booking booking, String businessName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Helvetica, Arial, sans-serif; background-color: #ffffff; color: #000000;">
                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td align="center" style="padding: 40px 20px;">
                                        <table role="presentation" style="width: 560px; max-width: 100%%; border-collapse: collapse; background-color: #ffffff;">
                                            <!-- Business Name -->
                                            <tr>
                                                <td style="padding: 0 0 8px 0; text-align: left;">
                                                    <p style="margin: 0; color: #000000; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;">%s</p>
                                                </td>
                                            </tr>

                                            <!-- Title -->
                                            <tr>
                                                <td style="padding: 0 0 32px 0; text-align: left; border-bottom: 1px solid #000000;">
                                                    <h1 style="margin: 0 0 24px 0; color: #000000; font-size: 28px; font-weight: 400; letter-spacing: -0.5px;">Booking confirmed</h1>
                                                </td>
                                            </tr>

                                            <!-- Greeting -->
                                            <tr>
                                                <td style="padding: 32px 0 24px 0;">
                                                    <p style="margin: 0 0 16px 0; color: #000000; font-size: 15px; line-height: 1.6;">Hi %s,</p>
                                                    <p style="margin: 0; color: #000000; font-size: 15px; line-height: 1.6;">Your booking with %s has been confirmed. The details are below.</p>
                                                </td>
                                            </tr>

                                            <!-- Details -->
                                            <tr>
                                                <td style="padding: 16px 0 24px 0; border-top: 1px solid #000000; border-bottom: 1px solid #000000;">
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Business</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Service</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Barber</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s %s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Date</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Time</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s – %s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Price</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">€%.2f</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Status</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>

                                            <!-- Notice -->
                                            <tr>
                                                <td style="padding: 24px 0 0 0;">
                                                    <p style="margin: 0 0 8px 0; color: #000000; font-size: 13px; line-height: 1.6;">Please arrive 5 minutes early.</p>
                                                    <p style="margin: 0; color: #000000; font-size: 13px; line-height: 1.6;">To cancel or reschedule, please do so at least 24 hours in advance.</p>
                                                </td>
                                            </tr>

                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 40px 0 0 0; border-top: 1px solid #000000;">
                                                    <p style="margin: 32px 0 4px 0; color: #000000; font-size: 13px;">%s</p>
                                                    <p style="margin: 0; color: #000000; font-size: 11px;">This is an automated confirmation email.</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """,
                businessName,
                booking.getCustomer().getFirstName(),
                businessName,
                businessName,
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBarber().getUser().getLastName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter),
                booking.getEndTime().format(timeFormatter),
                booking.getService().getPrice(),
                booking.getStatus(),
                businessName
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

            String businessName = booking.getBusiness().getName();

            helper.setTo(booking.getCustomer().getEmail());
            helper.setSubject(businessName + " — Appointment Reminder");

            String emailBody = buildReminderEmailHtml(booking, businessName);
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
     * Minimalistic black & white design.
     */
    private String buildReminderEmailHtml(Booking booking, String businessName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Helvetica, Arial, sans-serif; background-color: #ffffff; color: #000000;">
                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td align="center" style="padding: 40px 20px;">
                                        <table role="presentation" style="width: 560px; max-width: 100%%; border-collapse: collapse; background-color: #ffffff;">
                                            <!-- Business Name -->
                                            <tr>
                                                <td style="padding: 0 0 8px 0; text-align: left;">
                                                    <p style="margin: 0; color: #000000; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;">%s</p>
                                                </td>
                                            </tr>

                                            <!-- Title -->
                                            <tr>
                                                <td style="padding: 0 0 32px 0; text-align: left; border-bottom: 1px solid #000000;">
                                                    <h1 style="margin: 0 0 24px 0; color: #000000; font-size: 28px; font-weight: 400; letter-spacing: -0.5px;">Appointment reminder</h1>
                                                </td>
                                            </tr>

                                            <!-- Greeting -->
                                            <tr>
                                                <td style="padding: 32px 0 24px 0;">
                                                    <p style="margin: 0 0 16px 0; color: #000000; font-size: 15px; line-height: 1.6;">Hi %s,</p>
                                                    <p style="margin: 0; color: #000000; font-size: 15px; line-height: 1.6;">This is a reminder about your appointment tomorrow with %s.</p>
                                                </td>
                                            </tr>

                                            <!-- Details -->
                                            <tr>
                                                <td style="padding: 16px 0 24px 0; border-top: 1px solid #000000; border-bottom: 1px solid #000000;">
                                                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Business</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Service</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Barber</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s %s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Date</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Time</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">%s – %s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; width: 140px;">Price</td>
                                                            <td style="padding: 8px 0; color: #000000; font-size: 15px;">€%.2f</td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>

                                            <!-- Notice -->
                                            <tr>
                                                <td style="padding: 24px 0 0 0;">
                                                    <p style="margin: 0 0 8px 0; color: #000000; font-size: 13px; line-height: 1.6;">Please arrive 5 minutes early.</p>
                                                    <p style="margin: 0; color: #000000; font-size: 13px; line-height: 1.6;">If you need to cancel or reschedule, please contact us as soon as possible.</p>
                                                </td>
                                            </tr>

                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 40px 0 0 0; border-top: 1px solid #000000;">
                                                    <p style="margin: 32px 0 4px 0; color: #000000; font-size: 13px;">%s</p>
                                                    <p style="margin: 0; color: #000000; font-size: 11px;">This is an automated reminder email.</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """,
                businessName,
                booking.getCustomer().getFirstName(),
                businessName,
                businessName,
                booking.getService().getName(),
                booking.getBarber().getUser().getFirstName(),
                booking.getBarber().getUser().getLastName(),
                booking.getBookingDate().format(dateFormatter),
                booking.getStartTime().format(timeFormatter),
                booking.getEndTime().format(timeFormatter),
                booking.getService().getPrice(),
                businessName
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

            String businessName = user.getBusiness() != null ? user.getBusiness().getName() : "Account";

            // Recipient
            helper.setTo(user.getEmail());

            // Subject
            helper.setSubject(businessName + " — Reset Your Password");

            // Email body (HTML)
            String emailBody = buildPasswordResetEmailHtml(user, resetToken, businessName);
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
     * Minimalistic black & white design.
     */
    private String buildPasswordResetEmailHtml(User user, String resetToken, String businessName) {
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
                        <body style="margin: 0; padding: 0; font-family: Helvetica, Arial, sans-serif; background-color: #ffffff; color: #000000;">
                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td align="center" style="padding: 40px 20px;">
                                        <table role="presentation" style="width: 560px; max-width: 100%%; border-collapse: collapse; background-color: #ffffff;">
                                            <!-- Business Name -->
                                            <tr>
                                                <td style="padding: 0 0 8px 0; text-align: left;">
                                                    <p style="margin: 0; color: #000000; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;">%s</p>
                                                </td>
                                            </tr>

                                            <!-- Title -->
                                            <tr>
                                                <td style="padding: 0 0 32px 0; text-align: left; border-bottom: 1px solid #000000;">
                                                    <h1 style="margin: 0 0 24px 0; color: #000000; font-size: 28px; font-weight: 400; letter-spacing: -0.5px;">Reset your password</h1>
                                                </td>
                                            </tr>

                                            <!-- Greeting -->
                                            <tr>
                                                <td style="padding: 32px 0 24px 0;">
                                                    <p style="margin: 0 0 16px 0; color: #000000; font-size: 15px; line-height: 1.6;">Hi %s,</p>
                                                    <p style="margin: 0 0 16px 0; color: #000000; font-size: 15px; line-height: 1.6;">We received a request to reset the password for your %s account.</p>
                                                    <p style="margin: 0; color: #000000; font-size: 15px; line-height: 1.6;">Click the button below to choose a new password.</p>
                                                </td>
                                            </tr>

                                            <!-- Button -->
                                            <tr>
                                                <td style="padding: 8px 0 32px 0;">
                                                    <table role="presentation" style="border-collapse: collapse;">
                                                        <tr>
                                                            <td>
                                                                <a href="%s"
                                                                   style="display: inline-block; padding: 14px 32px; background-color: #000000; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 500; letter-spacing: 1px; text-transform: uppercase; border: 1px solid #000000;">
                                                                    Reset password
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>

                                            <!-- Details -->
                                            <tr>
                                                <td style="padding: 16px 0 24px 0; border-top: 1px solid #000000; border-bottom: 1px solid #000000;">
                                                    <p style="margin: 0 0 12px 0; color: #000000; font-size: 13px; line-height: 1.6;">This link will expire in 1 hour.</p>
                                                    <p style="margin: 0 0 12px 0; color: #000000; font-size: 13px; line-height: 1.6;">If the button doesn't work, copy and paste this URL into your browser:</p>
                                                    <p style="margin: 0; color: #000000; font-size: 12px; line-height: 1.6; word-break: break-all;">%s</p>
                                                </td>
                                            </tr>

                                            <!-- Notice -->
                                            <tr>
                                                <td style="padding: 24px 0 0 0;">
                                                    <p style="margin: 0; color: #000000; font-size: 13px; line-height: 1.6;">If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
                                                </td>
                                            </tr>

                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 40px 0 0 0; border-top: 1px solid #000000;">
                                                    <p style="margin: 32px 0 4px 0; color: #000000; font-size: 13px;">%s</p>
                                                    <p style="margin: 0; color: #000000; font-size: 11px;">This is an automated message, please do not reply to this email.</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """,
                businessName,
                user.getFirstName(),
                businessName,
                resetUrl,
                resetUrl,
                businessName
        );
    }
}