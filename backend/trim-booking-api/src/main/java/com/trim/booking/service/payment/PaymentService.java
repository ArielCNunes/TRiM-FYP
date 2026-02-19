package com.trim.booking.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.trim.booking.config.RlsBypass;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.Payment;
import com.trim.booking.repository.BookingRepository;
import com.trim.booking.repository.PaymentRepository;
import com.trim.booking.service.notification.EmailService;
import com.trim.booking.service.notification.SmsService;
import com.trim.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DepositCalculationService depositCalculationService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final RlsBypass rlsBypass;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          DepositCalculationService depositCalculationService,
                          EmailService emailService,
                          SmsService smsService,
                          RlsBypass rlsBypass) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.depositCalculationService = depositCalculationService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.rlsBypass = rlsBypass;
    }

    private Long getBusinessId() {
        return TenantContext.getCurrentBusinessId();
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
        // Get booking with tenant isolation
        Booking booking = bookingRepository.findByIdAndBusinessId(bookingId, getBusinessId())
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
                .putMetadata("business_id", getBusinessId().toString())
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
        payment.setBusiness(booking.getBusiness());
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
     * @param businessIdFromMetadata Business ID from Stripe metadata for cross-tenant verification
     */
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId, Long businessIdFromMetadata) {
        rlsBypass.runWithoutRls(() -> {
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Add business validation
            Booking booking = payment.getBooking();
            if (booking == null || booking.getBusiness() == null) {
                throw new RuntimeException("Invalid payment - no associated business");
            }

            // Verify business_id from Stripe metadata matches the booking's business
            Long bookingBusinessId = booking.getBusiness().getId();
            if (businessIdFromMetadata != null && !businessIdFromMetadata.equals(bookingBusinessId)) {
                logger.error("Business ID mismatch. Metadata: {}, Booking: {}", businessIdFromMetadata, bookingBusinessId);
                throw new RuntimeException("Payment business verification failed - potential cross-tenant attack");
            }

            // Log the business context for audit
            logger.info("Processing payment for business: {}", bookingBusinessId);

            // Update payment record
            payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);

            // Update booking to CONFIRMED
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setExpiresAt(null); // Clear expiry if booking is confirmed
            bookingRepository.save(booking);

            // Send confirmation notifications asynchronously
            emailService.sendBookingConfirmation(booking);
            smsService.sendBookingConfirmation(booking);

            logger.info("Deposit payment succeeded for booking: {}", booking.getId());
        });
    }
}