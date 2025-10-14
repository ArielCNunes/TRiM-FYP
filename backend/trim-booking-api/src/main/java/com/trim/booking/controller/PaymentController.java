package com.trim.booking.controller;

import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.trim.booking.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create payment intent for a booking.
     * <p>
     * POST /api/payments/create-intent
     * Body: { "bookingId": 1 }
     */
    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Long> request) {
        try {
            Long bookingId = request.get("bookingId");
            Map<String, String> response = paymentService.createPaymentIntent(bookingId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Payment creation failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

        // In production with webhook secret configured, verify signature
        if (webhookSecret != null && !webhookSecret.isEmpty() && sigHeader != null) {
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
                System.out.println("Webhook signature verified");
            } catch (Exception e) {
                System.out.println("Webhook signature verification failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }
        } else {
            // For testing without webhook secret (development mode)
            try {
                event = new Gson().fromJson(payload, Event.class);
                System.out.println("Webhook received (test mode, no signature verification)");
            } catch (Exception e) {
                System.out.println("Failed to parse webhook payload: " + e.getMessage());
                return ResponseEntity.badRequest().body("Invalid payload");
            }
        }

        // Handle payment_intent.succeeded event
        if ("payment_intent.succeeded".equals(event.getType())) {
            try {
                // Extract payment intent ID from event
                com.google.gson.JsonObject eventJson = new Gson().fromJson(payload, com.google.gson.JsonObject.class);
                com.google.gson.JsonObject dataObject = eventJson.getAsJsonObject("data").getAsJsonObject("object");
                String paymentIntentId = dataObject.get("id").getAsString();

                System.out.println("Processing payment success for: " + paymentIntentId);
                paymentService.handlePaymentSuccess(paymentIntentId);
                System.out.println("Payment processed successfully");

            } catch (Exception e) {
                System.out.println("Error processing webhook: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body("Webhook processing failed");
            }
        }

        return ResponseEntity.ok("Webhook received");
    }
}