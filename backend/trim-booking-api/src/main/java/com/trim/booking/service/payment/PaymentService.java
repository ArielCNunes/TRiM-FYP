package com.trim.booking.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.Payment;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.PaymentRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling Stripe payment processing.
 */
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DepositCalculationService depositCalculationService;
    private final EmailService emailService;
    private final SmsService smsService;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          DepositCalculationService depositCalculationService, EmailService emailService, SmsService smsService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.depositCalculationService = depositCalculationService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Create a Stripe PaymentIntent for a deposit.
     * <p>
     * POST /api/payments/create-intent
     * Body: { "bookingId": 1 }
     *
     * @param bookingId ID of booking needing deposit payment
     * @return Map with clientSecret, paymentIntentId, depositAmount, outstandingBalance
     * @throws StripeException  If Stripe API call fails
     * @throws RuntimeException If booking not found
     */
    @Transactional
    public Map<String, Object> createDepositPaymentIntent(Long bookingId) throws StripeException {
        // Get booking (must exist)
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Calculate deposit amount
        BigDecimal depositAmount = depositCalculationService.calculateDeposit(
                booking.getService().getPrice(),
                booking.getService().getDepositPercentage()
        );

        // Calculate outstanding balance
        BigDecimal outstandingBalance = depositCalculationService.calculateOutstandingBalance(
                booking.getService().getPrice(),
                booking.getService().getDepositPercentage()
        );

        // Store amounts on booking
        booking.setDepositAmount(depositAmount);
        booking.setOutstandingBalance(outstandingBalance);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PENDING);
        bookingRepository.save(booking);

        // Convert deposit to cents for Stripe
        long amountInCents = depositAmount
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Safety check: Stripe minimum is 50 cents
        if (amountInCents < 50) {
            throw new IllegalArgumentException("Deposit amount must be at least 0.50 EUR. Calculated amount was too low.");
        }

        // Create PaymentIntent in Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .putMetadata("booking_id", bookingId.toString())
                .putMetadata("deposit_amount", depositAmount.toPlainString())
                .putMetadata("outstanding_balance", outstandingBalance.toPlainString())
                .setDescription("Booking Deposit: " + booking.getService().getName() + " with " +
                        booking.getBarber().getUser().getFirstName())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Create Payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(depositAmount);
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Return response for frontend
        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("depositAmount", depositAmount);
        response.put("outstandingBalance", outstandingBalance);
        response.put("bookingId", bookingId);

        return response;
    }


    /**
     * Handle successful deposit payment (called by webhook).
     * Confirms the booking after deposit is received.
     * Sends confirmation email and SMS to customer.
     *
     * @param paymentIntentId Stripe PaymentIntent ID
     */
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Update payment record
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        // Update booking to CONFIRMED
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setExpiresAt(null); // Clear expiry if booking is confirmed
        bookingRepository.save(booking);

        // Send confirmation notifications asynchronously
        emailService.sendBookingConfirmation(booking);
        smsService.sendBookingConfirmation(booking);

        System.out.println("Deposit payment succeeded for booking: " + booking.getId());
    }
}