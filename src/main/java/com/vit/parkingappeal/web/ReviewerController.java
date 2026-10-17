package com.vit.parkingappeal.web;

import com.vit.parkingappeal.config.ReviewerAuthInterceptor;
import com.vit.parkingappeal.model.Appeal;
import com.vit.parkingappeal.model.AppealStatus;
import com.vit.parkingappeal.model.Decision;
import com.vit.parkingappeal.service.AppealService;
import com.vit.parkingappeal.service.EvidenceStorage;
import com.vit.parkingappeal.service.InvalidStateException;
import com.vit.parkingappeal.service.ValidationException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/** Reviewer (traffic officer) pages: dashboard, appeal detail, approve/reject. */
@Controller
public class ReviewerController {

    private final AppealService service;
    private final EvidenceStorage storage;
    private final String reviewerKey;

    public ReviewerController(AppealService service, EvidenceStorage storage,
                              @Value("${app.reviewer-key}") String reviewerKey) {
        this.service = service;
        this.storage = storage;
        this.reviewerKey = reviewerKey;
    }

    @GetMapping("/reviewer/login")
    public String loginPage() {
        return "reviewer-login";
    }

    @PostMapping("/reviewer/login")
    public String login(@RequestParam String key, HttpSession session, Model model) {
        if (keyMatches(key, reviewerKey)) {
            session.setAttribute(ReviewerAuthInterceptor.SESSION_KEY, Boolean.TRUE);
            return "redirect:/reviewer";
        }
        model.addAttribute("loginError", "Incorrect reviewer key");
        return "reviewer-login";
    }

    @PostMapping("/reviewer/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/reviewer")
    public String dashboard(@RequestParam(required = false) AppealStatus status, Model model) {
        Map<AppealStatus, Long> counts = service.counts();
        List<StatusTile> tiles = new ArrayList<>();
        for (AppealStatus s : AppealStatus.values()) {
            tiles.add(new StatusTile(s, counts.getOrDefault(s, 0L), s == status));
        }
        model.addAttribute("appeals", service.list(status));
        model.addAttribute("tiles", tiles);
        model.addAttribute("filter", status);
        return "reviewer-dashboard";
    }

    @GetMapping("/reviewer/appeals/{ref}")
    public String detail(@PathVariable String ref, Model model) {
        Appeal appeal = service.startReview(ref); // opening a SUBMITTED appeal moves it to UNDER_REVIEW
        model.addAttribute("appeal", appeal);
        return "reviewer-detail";
    }

    @PostMapping("/reviewer/appeals/{ref}/decision")
    public String decide(@PathVariable String ref, @RequestParam Decision decision,
                         @RequestParam(required = false) String remarks, RedirectAttributes redirect) {
        try {
            Appeal a = service.decide(ref, decision, remarks);
            redirect.addFlashAttribute("flashSuccess", "Appeal " + a.getAppealRef() + " " + a.getStatus().getLabel().toLowerCase());
        } catch (ValidationException e) {
            redirect.addFlashAttribute("flashError", e.getErrors().values().iterator().next());
            redirect.addFlashAttribute("remarks", remarks);
        } catch (InvalidStateException e) {
            redirect.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/reviewer/appeals/" + ref;
    }

    @GetMapping("/reviewer/appeals/{ref}/evidence")
    public ResponseEntity<Resource> evidence(@PathVariable String ref) {
        Appeal a = service.get(ref);
        Resource file = new FileSystemResource(storage.resolve(a.getEvidenceStoredName()));
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = MediaTypeFactory.getMediaType(a.getEvidenceFileName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + a.getEvidenceStoredName() + "\"")
                .body(file);
    }

    static boolean keyMatches(String given, String expected) {
        if (given == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
