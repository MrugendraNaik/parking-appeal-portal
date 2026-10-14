package com.vit.parkingappeal.config;

import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds sample issued tickets on first start (empty database), so the portal can be demoed immediately.
 * Dates are relative to the first start-up day; PV-100007 is deliberately outside the 30-day window.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ViolationRepository violations;
    private final Clock clock;

    public DataSeeder(ViolationRepository violations, Clock clock) {
        this.violations = violations;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (violations.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        List<Violation> seed = List.of(
                new Violation("PV-100001", "MH01AB1234", "No Parking Zone", "Dadar TT Circle", today.minusDays(3), 500),
                new Violation("PV-100002", "MH02CD5678", "Parking on Footpath", "Andheri West, Link Road", today.minusDays(10), 1000),
                new Violation("PV-100003", "MH04EF9012", "Double Parking", "Thane Station Road", today.minusDays(5), 750),
                new Violation("PV-100004", "MH03GH3456", "Expired Parking Meter", "Bandra, Hill Road", today.minusDays(15), 300),
                new Violation("PV-100005", "MH01KL2345", "Blocking a Driveway", "Powai, Hiranandani", today.minusDays(1), 1500),
                new Violation("PV-100006", "MH47PQ1122", "Disabled Bay Misuse", "Colaba Causeway", today.minusDays(20), 2000),
                new Violation("PV-100007", "MH05MN6789", "No Parking Zone", "Kurla West", today.minusDays(45), 500),
                new Violation("PV-100008", "MH02RS4455", "Parking on Footpath", "Borivali East", today.minusDays(2), 1000));
        violations.saveAll(seed);
        log.info("Seeded {} sample parking violations (PV-100001 .. PV-100008)", seed.size());
    }
}
