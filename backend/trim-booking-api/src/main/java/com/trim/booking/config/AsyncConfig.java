package com.trim.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enable asynchronous method execution.
 * Allows @Async methods to run in background threads.
 * Also enables scheduling support with @Scheduled methods.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}