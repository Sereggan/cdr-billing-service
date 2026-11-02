package com.nikolaichuk.cdr.cdrbillingservice.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuration class for the time source used across the application.
 */
@Configuration
public class TimeConfiguration {

    /**
     * UTC clock for domain timestamps.
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
