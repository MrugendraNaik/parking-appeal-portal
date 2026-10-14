package com.vit.parkingappeal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injected wherever "today" matters, so tests can control time. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
