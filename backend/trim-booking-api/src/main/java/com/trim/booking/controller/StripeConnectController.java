package com.trim.booking.controller;

import com.stripe.exception.StripeException;
import com.trim.booking.service.payment.StripeConnectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe-connect")
@PreAuthorize("hasRole('ADMIN')")
public class StripeConnectController {

    private final StripeConnectService stripeConnectService;

    public StripeConnectController(StripeConnectService stripeConnectService) {
        this.stripeConnectService = stripeConnectService;
    }

    @PostMapping("/create-account")
    public ResponseEntity<Map<String, Object>> createAccount() throws StripeException {
        return ResponseEntity.ok(stripeConnectService.createConnectAccount());
    }

    @GetMapping("/account-link")
    public ResponseEntity<Map<String, Object>> getAccountLink() throws StripeException {
        return ResponseEntity.ok(stripeConnectService.createAccountLink());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() throws StripeException {
        return ResponseEntity.ok(stripeConnectService.checkOnboardingStatus());
    }

    @GetMapping("/dashboard-link")
    public ResponseEntity<Map<String, String>> getDashboardLink() {
        return ResponseEntity.ok(Map.of("url", "https://dashboard.stripe.com"));
    }
}
