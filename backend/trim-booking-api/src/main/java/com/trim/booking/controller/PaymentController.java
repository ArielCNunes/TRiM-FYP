package com.trim.booking.controller;

import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.trim.booking.service.payment.PaymentService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final Environment environment;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService, Environment environment) {
        this.paymentService = paymentService;
        this.environment = environment;
    }

    @PostConstruct
    public void validateWebhookSecret() {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (webhookSecret == null || webhookSecret.isEmpty()) {
            if (isProduction) {
                throw new IllegalStateException("stripe.webhook.secret must be configured in production environment");
            } else {
                logger.warn("SECURITY WARNING: stripe.webhook.secret is not configured. " +
                        "Webhook signature verification will be required for all requests. " +
                        "Configure this property before deploying to production.");
            }
        }
    }

    /**
     * Create payment intent for a booking.
     * <p>
     * POST /api/payments/create-intent
     * Body: { "bookingId": 1 }
     */
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Long> request) throws StripeException {
        Long bookingId = request.get("bookingId");
        if (bookingId == null) {
            throw new IllegalArgumentException("bookingId is required");
        }

        Map<String, Object> response = paymentService.createDepositPaymentIntent(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook endpoint.
     * Handles payment_intent.succeeded events.
     * <p>
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        Event event;

        // Require webhook secret and signature for all environments
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            logger.error("Webhook rejected: stripe.webhook.secret is not configured");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook signature verification required");
        }

        if (sigHeader == null || sigHeader.isEmpty()) {
            logger.warn("Webhook rejected: Missing Stripe-Signature header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook signature verification required");
        }

        // Verify webhook signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            logger.info("Webhook signature verified successfully");
        } catch (Exception e) {
            logger.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Handle payment success
        if ("payment_intent.succeeded".equals(event.getType())) {
            try {
                com.google.gson.JsonObject eventJson = new Gson().fromJson(payload, com.google.gson.JsonObject.class);
                com.google.gson.JsonObject dataObject = eventJson.getAsJsonObject("data").getAsJsonObject("object");
                String paymentIntentId = dataObject.get("id").getAsString();

                // Extract business_id from metadata for cross-tenant verification
                Long businessIdFromMetadata = null;
                if (dataObject.has("metadata") && dataObject.getAsJsonObject("metadata").has("business_id")) {
                    String businessIdStr = dataObject.getAsJsonObject("metadata").get("business_id").getAsString();
                    businessIdFromMetadata = Long.parseLong(businessIdStr);
                }

                logger.info("Processing payment success for: {}, business: {}", paymentIntentId, businessIdFromMetadata);
                paymentService.handlePaymentSuccess(paymentIntentId, businessIdFromMetadata);
                logger.info("Deposit payment succeeded for payment intent: {}", paymentIntentId);

            } catch (Exception e) {
                logger.error("Error processing webhook: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Webhook processing failed");
            }
        }
        return ResponseEntity.ok("Webhook received");
    }
}