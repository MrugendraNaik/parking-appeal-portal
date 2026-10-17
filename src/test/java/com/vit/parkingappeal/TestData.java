package com.vit.parkingappeal;

import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import com.vit.parkingappeal.service.AppealForm;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared test data for unit / Spring tests. */
public final class TestData {

    private static final AtomicInteger SEQ = new AtomicInteger(500000);

    public static final String VALID_REASON =
            "The no-parking sign was completely hidden behind a tree branch. Photo attached as evidence.";

    private TestData() {
    }

    /** Saves a brand-new ticket so tests never collide with each other. */
    public static Violation freshViolation(ViolationRepository repo, int daysAgo) {
        int n = SEQ.incrementAndGet();
        return repo.save(new Violation("PV-" + n, "MH01ZZ" + String.format("%04d", n % 10000),
                "No Parking Zone", "Test Street", LocalDate.now().minusDays(daysAgo), 500));
    }

    public static AppealForm formFor(Violation v) {
        return new AppealForm(v.getTicketNumber(), v.getVehicleNumber(), "Asha Patil",
                "asha.patil@example.com", "9876543210", VALID_REASON);
    }

    public static MockMultipartFile pdf() {
        return new MockMultipartFile("evidence", "photo-proof.pdf", "application/pdf",
                "%PDF-1.4\n% test evidence\n".getBytes(StandardCharsets.US_ASCII));
    }
}
