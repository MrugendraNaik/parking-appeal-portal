package com.vit.parkingappeal.service;

import com.vit.parkingappeal.model.Violation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Document/data validation for a new appeal. Pure logic (no database access) so it is easy to unit-test:
 * the caller passes in the ticket lookup result and whether an open appeal already exists.
 */
@Component
public class AppealValidator {

    public static final Pattern TICKET = Pattern.compile("^PV-\\d{6}$");
    /** Indian registration, e.g. MH01AB1234, MH4AB1234, DL3C1234 (after removing spaces/hyphens). */
    public static final Pattern VEHICLE = Pattern.compile("^[A-Z]{2}\\d{1,2}[A-Z]{0,3}\\d{4}$");
    public static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    public static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    public static final int MIN_REASON = 30;
    public static final int MAX_REASON = 1000;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final int appealWindowDays;
    private final long maxEvidenceBytes;

    public AppealValidator(@Value("${app.appeal-window-days:30}") int appealWindowDays,
                           @Value("${app.max-evidence-bytes:2097152}") long maxEvidenceBytes) {
        this.appealWindowDays = appealWindowDays;
        this.maxEvidenceBytes = maxEvidenceBytes;
    }

    /**
     * @param f              normalised form data
     * @param evidence       uploaded file metadata, or null if nothing was uploaded
     * @param violation      result of looking up f.ticketNumber
     * @param hasOpenAppeal  true if this ticket already has a SUBMITTED / UNDER_REVIEW appeal
     * @return field -> error message; empty map means valid
     */
    public Map<String, String> validate(AppealForm f, EvidenceFile evidence, Optional<Violation> violation,
                                        boolean hasOpenAppeal, LocalDate today) {
        Map<String, String> errors = new LinkedHashMap<>();

        // 1. Format checks
        if (!TICKET.matcher(f.getTicketNumber()).matches()) {
            errors.put("ticketNumber", "Ticket number must look like PV-123456");
        }
        if (!VEHICLE.matcher(f.getVehicleNumber()).matches()) {
            errors.put("vehicleNumber", "Enter a valid vehicle number, e.g. MH01AB1234");
        }
        if (f.getOwnerName().length() < 3 || f.getOwnerName().length() > 60) {
            errors.put("ownerName", "Name must be 3 to 60 characters");
        }
        if (!EMAIL.matcher(f.getEmail()).matches()) {
            errors.put("email", "Enter a valid email address");
        }
        if (!PHONE.matcher(f.getPhone()).matches()) {
            errors.put("phone", "Enter a 10-digit mobile number starting with 6-9");
        }
        int len = f.getReason().length();
        if (len < MIN_REASON || len > MAX_REASON) {
            errors.put("reason", "Explain your appeal in " + MIN_REASON + " to " + MAX_REASON
                    + " characters (currently " + len + ")");
        }

        // 2. Evidence document checks
        String evidenceError = checkEvidence(evidence);
        if (evidenceError != null) {
            errors.put("evidence", evidenceError);
        }

        // 3. Cross-checks against the issued ticket (only if the ticket number itself is well-formed)
        if (!errors.containsKey("ticketNumber")) {
            if (violation.isEmpty()) {
                errors.put("ticketNumber", "No parking violation found with this ticket number");
            } else {
                Violation v = violation.get();
                if (!errors.containsKey("vehicleNumber") && !v.getVehicleNumber().equals(f.getVehicleNumber())) {
                    errors.put("vehicleNumber", "Vehicle number does not match the one on the ticket");
                }
                LocalDate lastDay = v.getViolationDate().plusDays(appealWindowDays);
                if (today.isAfter(lastDay)) {
                    errors.put("ticketNumber", "The " + appealWindowDays + "-day appeal window closed on "
                            + lastDay.format(DATE));
                } else if (hasOpenAppeal) {
                    errors.put("ticketNumber", "An appeal for this ticket is already being processed");
                }
            }
        }
        return errors;
    }

    String checkEvidence(EvidenceFile ev) {
        if (ev == null) {
            return "Upload supporting evidence (PDF, JPG or PNG)";
        }
        String ext = ev.extension();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return "Only PDF, JPG or PNG files are accepted";
        }
        if (ev.size() > maxEvidenceBytes) {
            return "File is too large (max " + (maxEvidenceBytes / (1024 * 1024)) + " MB)";
        }
        if (!contentMatchesExtension(ext, ev.header())) {
            return "File content does not match its ." + ext + " extension";
        }
        return null;
    }

    /** Magic-number check so a renamed .exe cannot pass as a .pdf */
    static boolean contentMatchesExtension(String ext, byte[] h) {
        return switch (ext) {
            case "pdf" -> h.length >= 4 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F';
            case "png" -> h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
            case "jpg", "jpeg" -> h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8
                    && (h[2] & 0xFF) == 0xFF;
            default -> false;
        };
    }
}
