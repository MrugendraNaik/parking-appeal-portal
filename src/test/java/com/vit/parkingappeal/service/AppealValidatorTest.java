package com.vit.parkingappeal.service;

import com.vit.parkingappeal.model.Violation;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests for the document/data validation rules (no Spring context). */
class AppealValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private final AppealValidator validator = new AppealValidator(30, 2 * 1024 * 1024);
    private final Violation ticket = new Violation("PV-100001", "MH01AB1234", "No Parking Zone", "Dadar",
            TODAY.minusDays(3), 500);

    private static AppealForm validForm() {
        return new AppealForm("PV-100001", "MH01AB1234", "Asha Patil", "asha@example.com", "9876543210",
                "The no-parking sign was hidden behind a tree. Photo attached as proof.");
    }

    private static EvidenceFile pdf() {
        return new EvidenceFile("proof.pdf", 1024, "%PDF-1.4".getBytes(StandardCharsets.US_ASCII));
    }

    private Map<String, String> run(AppealForm f, EvidenceFile ev, Optional<Violation> v, boolean open) {
        return validator.validate(f.normalised(), ev, v, open, TODAY);
    }

    @Test
    void acceptsAValidAppeal() {
        assertThat(run(validForm(), pdf(), Optional.of(ticket), false)).isEmpty();
    }

    @Test
    void normalisesVehicleNumberWithSpacesAndLowercase() {
        AppealForm f = validForm();
        f.setVehicleNumber(" mh 01 ab-1234 ");
        assertThat(run(f, pdf(), Optional.of(ticket), false)).isEmpty();
    }

    @Test
    void rejectsMalformedTicketNumber() {
        AppealForm f = validForm();
        f.setTicketNumber("12345");
        assertThat(run(f, pdf(), Optional.empty(), false)).containsKey("ticketNumber");
    }

    @Test
    void rejectsUnknownTicket() {
        assertThat(run(validForm(), pdf(), Optional.empty(), false))
                .containsEntry("ticketNumber", "No parking violation found with this ticket number");
    }

    @Test
    void rejectsVehicleThatDoesNotMatchTicket() {
        AppealForm f = validForm();
        f.setVehicleNumber("MH02CD5678");
        assertThat(run(f, pdf(), Optional.of(ticket), false)).containsKey("vehicleNumber");
    }

    @Test
    void rejectsAppealAfterThirtyDayWindow() {
        Violation old = new Violation("PV-100001", "MH01AB1234", "No Parking Zone", "Dadar", TODAY.minusDays(31), 500);
        assertThat(run(validForm(), pdf(), Optional.of(old), false).get("ticketNumber")).contains("appeal window closed");
    }

    @Test
    void rejectsSecondAppealWhileOneIsOpen() {
        assertThat(run(validForm(), pdf(), Optional.of(ticket), true).get("ticketNumber")).contains("already");
    }

    @Test
    void rejectsShortReasonAndBadContactDetails() {
        AppealForm f = validForm();
        f.setReason("Not mine");
        f.setEmail("not-an-email");
        f.setPhone("12345");
        assertThat(run(f, pdf(), Optional.of(ticket), false)).containsKeys("reason", "email", "phone");
    }

    @Test
    void requiresEvidence() {
        assertThat(run(validForm(), null, Optional.of(ticket), false)).containsKey("evidence");
    }

    @Test
    void rejectsWrongExtensionTooLargeAndFakeContent() {
        EvidenceFile exe = new EvidenceFile("virus.exe", 100, new byte[]{'M', 'Z'});
        EvidenceFile huge = new EvidenceFile("big.pdf", 3 * 1024 * 1024, "%PDF".getBytes(StandardCharsets.US_ASCII));
        EvidenceFile renamed = new EvidenceFile("fake.pdf", 100, new byte[]{'M', 'Z', 0, 0});
        assertThat(validator.checkEvidence(exe)).contains("Only PDF");
        assertThat(validator.checkEvidence(huge)).contains("too large");
        assertThat(validator.checkEvidence(renamed)).contains("does not match");
    }

    @Test
    void acceptsRealPngAndJpegHeaders() {
        assertThat(AppealValidator.contentMatchesExtension("png", new byte[]{(byte) 0x89, 'P', 'N', 'G'})).isTrue();
        assertThat(AppealValidator.contentMatchesExtension("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})).isTrue();
    }
}
