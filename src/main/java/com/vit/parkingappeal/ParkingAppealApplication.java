package com.vit.parkingappeal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extends SpringBootServletInitializer so the WAR can be dropped into
 * an external Tomcat 10.1, and also runs standalone with "java -jar parking-appeal.war".
 */
@SpringBootApplication
public class ParkingAppealApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ParkingAppealApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ParkingAppealApplication.class, args);
    }
}
