package com.vit.parkingappeal.web;

import com.vit.parkingappeal.model.Appeal;
import com.vit.parkingappeal.service.AppealForm;
import com.vit.parkingappeal.service.AppealService;
import com.vit.parkingappeal.service.NotFoundException;
import com.vit.parkingappeal.service.ValidationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Citizen-facing pages: file an appeal and track it. */
@Controller
public class AppealController {

    private final AppealService service;

    public AppealController(AppealService service) {
        this.service = service;
    }

    @GetMapping("/appeals/new")
    public String newAppeal(@RequestParam(required = false) String ticket, Model model) {
        AppealForm form = new AppealForm();
        form.setTicketNumber(ticket);
        model.addAttribute("appealForm", form);
        return "appeal-form";
    }

    @PostMapping("/appeals")
    public String submit(@ModelAttribute("appealForm") AppealForm form,
                         @RequestParam(value = "evidence", required = false) MultipartFile evidence,
                         Model model, RedirectAttributes redirect) {
        try {
            Appeal appeal = service.submit(form, evidence);
            redirect.addFlashAttribute("appealRef", appeal.getAppealRef());
            redirect.addFlashAttribute("appealEmail", appeal.getEmail());
            redirect.addFlashAttribute("ticketNumber", appeal.getViolation().getTicketNumber());
            return "redirect:/appeals/confirmation";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getErrors());
            return "appeal-form";
        }
    }

    @GetMapping("/appeals/confirmation")
    public String confirmation(Model model) {
        if (!model.containsAttribute("appealRef")) {
            return "redirect:/";
        }
        return "appeal-confirmation";
    }

    @GetMapping("/track")
    public String track(@RequestParam(required = false) String ref,
                        @RequestParam(required = false) String email, Model model) {
        model.addAttribute("ref", ref);
        model.addAttribute("email", email);
        if (ref != null && email != null) {
            try {
                model.addAttribute("appeal", service.track(ref, email));
            } catch (NotFoundException e) {
                model.addAttribute("trackError", e.getMessage());
            }
        }
        return "track";
    }
}
