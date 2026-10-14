package com.vit.parkingappeal.web;

import com.vit.parkingappeal.model.AppealStatus;
import com.vit.parkingappeal.model.Decision;
import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import com.vit.parkingappeal.service.AppealForm;
import com.vit.parkingappeal.service.AppealService;
import com.vit.parkingappeal.service.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** REST API - same workflow as the web pages, for integrations and API testing (Postman / curl). */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final AppealService service;
    private final ViolationRepository violations;
    private final String reviewerKey;

    public ApiController(AppealService service, ViolationRepository violations,
                         @Value("${app.reviewer-key}") String reviewerKey) {
        this.service = service;
        this.violations = violations;
        this.reviewerKey = reviewerKey;
    }

    public record DecisionRequest(Decision decision, String remarks) {
    }

    @GetMapping("/violations/{ticketNumber}")
    public Violation violation(@PathVariable String ticketNumber) {
        return violations.findByTicketNumber(ticketNumber.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Ticket " + ticketNumber + " not found"));
    }

    @PostMapping(value = "/appeals", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppealSummary> submit(@ModelAttribute AppealForm form,
                                                @RequestParam(value = "evidence", required = false) MultipartFile evidence) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AppealSummary.of(service.submit(form, evidence)));
    }

    @GetMapping("/appeals/{ref}")
    public AppealSummary track(@PathVariable String ref, @RequestParam String email) {
        return AppealSummary.of(service.track(ref, email));
    }

    @GetMapping("/appeals")
    public ResponseEntity<?> list(@RequestHeader(value = "X-Reviewer-Key", required = false) String key,
                                  @RequestParam(required = false) AppealStatus status) {
        if (!ReviewerController.keyMatches(key, reviewerKey)) {
            return unauthorized();
        }
        List<AppealSummary> list = service.list(status).stream().map(AppealSummary::of).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/appeals/{ref}/decision")
    public ResponseEntity<?> decide(@PathVariable String ref, @RequestBody DecisionRequest body,
                                    @RequestHeader(value = "X-Reviewer-Key", required = false) String key) {
        if (!ReviewerController.keyMatches(key, reviewerKey)) {
            return unauthorized();
        }
        if (body == null || body.decision() == null) {
            return ResponseEntity.badRequest().body(Map.of("errors", Map.of("decision", "APPROVE or REJECT required")));
        }
        return ResponseEntity.ok(AppealSummary.of(service.decide(ref, body.decision(), body.remarks())));
    }

    private static ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or wrong X-Reviewer-Key"));
    }
}
