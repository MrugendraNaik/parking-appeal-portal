package com.vit.parkingappeal.web;

import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test-data endpoint used by the Selenium suite to create a fresh, unused ticket for every test,
 * so the suite can be re-run against the same deployment. Only exists when app.test-support=true
 * (never enable it in production).
 */
@RestController
@ConditionalOnProperty(name = "app.test-support", havingValue = "true")
public class TestSupportController {

    private static final String LETTERS = "ABCDEFGHJKLMNPRSTUVWXYZ";
    private final ViolationRepository violations;

    public TestSupportController(ViolationRepository violations) {
        this.violations = violations;
    }

    @PostMapping("/api/test/violations")
    @ResponseStatus(HttpStatus.CREATED)
    public Violation createFreshViolation() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String ticket;
        do {
            ticket = "PV-" + (900000 + r.nextInt(100000));
        } while (violations.existsByTicketNumber(ticket));
        String vehicle = "MH" + String.format("%02d", 1 + r.nextInt(50))
                + LETTERS.charAt(r.nextInt(LETTERS.length())) + LETTERS.charAt(r.nextInt(LETTERS.length()))
                + String.format("%04d", r.nextInt(10000));
        return violations.save(new Violation(ticket, vehicle, "No Parking Zone", "Selenium Test Lane",
                LocalDate.now().minusDays(2), 500));
    }
}
