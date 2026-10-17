package com.vit.parkingappeal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Adds an "app" section to /actuator/health. Setting APP_SIMULATE_FAILURE=true makes the
 * service report DOWN (HTTP 503) - used to demonstrate automatic rollback in Experiment 14.
 */
@Component("app")
public class AppHealthIndicator implements HealthIndicator {

    private final boolean simulateFailure;
    private final String env;
    private final String version;
    private final String build;

    public AppHealthIndicator(@Value("${app.simulate-failure:false}") boolean simulateFailure,
                              @Value("${app.env:dev}") String env,
                              @Value("${app.version:dev}") String version,
                              @Value("${app.build:local}") String build) {
        this.simulateFailure = simulateFailure;
        this.env = env;
        this.version = version;
        this.build = build;
    }

    @Override
    public Health health() {
        Health.Builder b = simulateFailure
                ? Health.down().withDetail("reason", "APP_SIMULATE_FAILURE is true")
                : Health.up();
        return b.withDetail("environment", env)
                .withDetail("version", version)
                .withDetail("build", build)
                .build();
    }
}
