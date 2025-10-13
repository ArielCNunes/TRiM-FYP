package com.trim.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enable asynchronous method execution.
 * Allows @Async methods to run in background threads.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}