package com.trim.booking.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Initialize Twilio with credentials from application.properties.
 * Runs once on application startup.
 */
@Configuration
public class TwilioConfig {
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String phoneNumber;

    /**
     * Initialize Twilio after properties are loaded.
     */
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        System.out.println("Twilio initialized with phone number: " + phoneNumber);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}