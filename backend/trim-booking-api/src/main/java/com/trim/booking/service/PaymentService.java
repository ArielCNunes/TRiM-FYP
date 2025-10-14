package com.trim.booking.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.Payment;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling Stripe payment processing.
 */
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a Stripe PaymentIntent for a booking.
     * Returns client_secret for frontend to complete payment.
     */
    @Transactional
    public Map<String, String> createPaymentIntent(Long bookingId) throws StripeException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Convert price to cents (Stripe uses smallest currency unit)
        long amountInCents = booking.getService().getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Create PaymentIntent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .putMetadata("booking_id", bookingId.toString())
                .setDescription("Booking: " + booking.getService().getName() + " with " +
                        booking.getBarber().getUser().getFirstName())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Create Payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getService().getPrice());
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Update booking payment status
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        bookingRepository.save(booking);

        // Return client_secret for frontend
        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        return response;
    }

    /**
     * Handle successful payment (called by webhook).
     */
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Update payment
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update booking
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        System.out.println("Payment succeeded for booking: " + booking.getId());
    }

    /**
     * Process refund for a cancelled booking.
     */
    @Transactional
    public void processRefund(Long bookingId) throws StripeException {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new RuntimeException("Cannot refund payment that hasn't succeeded");
        }

        // Create refund in Stripe
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getStripePaymentIntentId())
                .build();

        Refund refund = Refund.create(params);

        // Update payment
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundId(refund.getId());
        paymentRepository.save(payment);

        // Update booking
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        System.out.println("Refund processed for booking: " + bookingId);
    }
}