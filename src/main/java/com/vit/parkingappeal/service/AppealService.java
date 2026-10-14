package com.vit.parkingappeal.service;

import com.vit.parkingappeal.model.Appeal;
import com.vit.parkingappeal.model.AppealStatus;
import com.vit.parkingappeal.model.Decision;
import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.AppealRepository;
import com.vit.parkingappeal.repository.ViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The appeal workflow: submit -> validate -> review -> approve/reject -> track. */
@Service
public class AppealService {

    public static final int MIN_REJECTION_REMARKS = 10;
    private static final Set<AppealStatus> OPEN = Set.of(AppealStatus.SUBMITTED, AppealStatus.UNDER_REVIEW);
    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final AppealRepository appeals;
    private final ViolationRepository violations;
    private final AppealValidator validator;
    private final EvidenceStorage storage;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AppealService(AppealRepository appeals, ViolationRepository violations, AppealValidator validator,
                         EvidenceStorage storage, Clock clock) {
        this.appeals = appeals;
        this.violations = violations;
        this.validator = validator;
        this.storage = storage;
        this.clock = clock;
    }

    @Transactional
    public Appeal submit(AppealForm raw, MultipartFile evidence) {
        AppealForm f = raw.normalised();
        Optional<Violation> violation = violations.findByTicketNumber(f.getTicketNumber());
        boolean open = appeals.existsByViolationTicketNumberAndStatusIn(f.getTicketNumber(), OPEN);

        Map<String, String> errors = validator.validate(f, EvidenceFile.from(evidence), violation, open,
                LocalDate.now(clock));
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        String ref = newReference();
        Appeal appeal = new Appeal(ref, violation.orElseThrow(), f.getVehicleNumber(), f.getOwnerName(),
                f.getEmail(), f.getPhone(), f.getReason(), now());
        String stored = storage.store(ref, evidence);
        appeal.attachEvidence(evidence.getOriginalFilename(), stored, evidence.getSize());
        return appeals.save(appeal);
    }

    /** Citizen lookup: needs both the reference and the email used when filing. */
    @Transactional(readOnly = true)
    public Appeal track(String appealRef, String email) {
        String ref = appealRef == null ? "" : appealRef.trim().toUpperCase();
        String mail = email == null ? "" : email.trim();
        return appeals.findByAppealRef(ref)
                .filter(a -> a.getEmail().equalsIgnoreCase(mail))
                .orElseThrow(() -> new NotFoundException("No appeal matches that reference and email"));
    }

    @Transactional(readOnly = true)
    public Appeal get(String appealRef) {
        return appeals.findByAppealRef(appealRef)
                .orElseThrow(() -> new NotFoundException("Appeal " + appealRef + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Appeal> list(AppealStatus filter) {
        return filter == null ? appeals.findAllByOrderBySubmittedAtDesc()
                : appeals.findByStatusOrderBySubmittedAtDesc(filter);
    }

    @Transactional(readOnly = true)
    public Map<AppealStatus, Long> counts() {
        Map<AppealStatus, Long> counts = new EnumMap<>(AppealStatus.class);
        for (AppealStatus s : AppealStatus.values()) {
            counts.put(s, appeals.countByStatus(s));
        }
        return counts;
    }

    /** Called when a reviewer opens a submitted appeal. */
    @Transactional
    public Appeal startReview(String appealRef) {
        Appeal a = get(appealRef);
        a.markUnderReview(now());
        return appeals.save(a);
    }

    @Transactional
    public Appeal decide(String appealRef, Decision decision, String remarks) {
        Appeal a = get(appealRef);
        if (a.getStatus().isFinal()) {
            throw new InvalidStateException("Appeal " + appealRef + " is already " + a.getStatus().getLabel());
        }
        String text = remarks == null ? "" : remarks.trim();
        if (decision == Decision.REJECT && text.length() < MIN_REJECTION_REMARKS) {
            throw new ValidationException(Map.of("remarks",
                    "Give the citizen a reason for rejection (at least " + MIN_REJECTION_REMARKS + " characters)"));
        }
        AppealStatus result = decision == Decision.APPROVE ? AppealStatus.APPROVED : AppealStatus.REJECTED;
        a.decide(result, text.isEmpty() ? null : text, now());
        return appeals.save(a);
    }

    private String newReference() {
        String date = LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE);
        String ref;
        do {
            StringBuilder sb = new StringBuilder("APL-").append(date).append('-');
            for (int i = 0; i < 4; i++) {
                sb.append(REF_CHARS.charAt(random.nextInt(REF_CHARS.length())));
            }
            ref = sb.toString();
        } while (appeals.existsByAppealRef(ref));
        return ref;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
