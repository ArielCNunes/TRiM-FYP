package com.trim.booking.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Initialize Stripe with API key from application.properties.
 */
@Configuration
public class StripeConfig {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.publishable.key}")
    private String publishableKey;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = secretKey;
        System.out.println("Stripe initialized successfully");
    }

    public String getPublishableKey() {
        return publishableKey;
    }
}