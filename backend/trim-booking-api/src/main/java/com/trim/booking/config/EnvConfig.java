package com.trim.booking.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * Loads environment variables from .env file at application startup.
 * This class runs before Spring beans are initialized.
 */
@Configuration
public class EnvConfig {
    static {
        // Load .env file from project root
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .filename(".env")
                .load();

        // Set environment variables that Spring can read
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}

