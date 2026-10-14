package com.vit.parkingappeal.service;

import com.vit.parkingappeal.TestData;
import com.vit.parkingappeal.model.Appeal;
import com.vit.parkingappeal.model.AppealStatus;
import com.vit.parkingappeal.model.Decision;
import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Workflow tests against the real Spring context and an in-memory H2 database. */
@SpringBootTest
@ActiveProfiles("test")
class AppealServiceTest {

    @Autowired
    private AppealService service;

    @Autowired
    private ViolationRepository violations;

    @Test
    void submitCreatesSubmittedAppealWithReference() {
        Violation v = TestData.freshViolation(violations, 2);
        Appeal a = service.submit(TestData.formFor(v), TestData.pdf());

        assertThat(a.getAppealRef()).matches("APL-\\d{8}-[A-Z2-9]{4}");
        assertThat(a.getStatus()).isEqualTo(AppealStatus.SUBMITTED);
        assertThat(a.getEvidenceFileName()).isEqualTo("photo-proof.pdf");
    }

    @Test
    void secondAppealForSameTicketIsBlockedWhileOpen() {
        Violation v = TestData.freshViolation(violations, 2);
        service.submit(TestData.formFor(v), TestData.pdf());

        assertThatThrownBy(() -> service.submit(TestData.formFor(v), TestData.pdf()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reviewerApprovesAppeal() {
        Violation v = TestData.freshViolation(violations, 2);
        Appeal a = service.submit(TestData.formFor(v), TestData.pdf());

        service.startReview(a.getAppealRef());
        Appeal decided = service.decide(a.getAppealRef(), Decision.APPROVE, "Sign was obstructed");

        assertThat(decided.getStatus()).isEqualTo(AppealStatus.APPROVED);
        assertThat(decided.getDecidedAt()).isNotNull();
    }

    @Test
    void rejectionNeedsRemarksAndFinalDecisionCannotChange() {
        Violation v = TestData.freshViolation(violations, 2);
        Appeal a = service.submit(TestData.formFor(v), TestData.pdf());

        assertThatThrownBy(() -> service.decide(a.getAppealRef(), Decision.REJECT, "no"))
                .isInstanceOf(ValidationException.class);

        service.decide(a.getAppealRef(), Decision.REJECT, "Photo shows the vehicle inside the no-parking zone");
        assertThatThrownBy(() -> service.decide(a.getAppealRef(), Decision.APPROVE, null))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void trackingRequiresMatchingEmail() {
        Violation v = TestData.freshViolation(violations, 2);
        Appeal a = service.submit(TestData.formFor(v), TestData.pdf());

        assertThat(service.track(a.getAppealRef().toLowerCase(), "ASHA.PATIL@example.com").getStatus())
                .isEqualTo(AppealStatus.SUBMITTED);
        assertThatThrownBy(() -> service.track(a.getAppealRef(), "someone@else.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
