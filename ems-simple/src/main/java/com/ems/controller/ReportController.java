package com.ems.controller;

import com.ems.dao.LookupDao;
import com.ems.dao.ReportDao;
import com.ems.model.Report;
import com.ems.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    private static final List<String> ISSUE_TYPES = List.of("HARDWARE_FAULT", "SOFTWARE", "ELECTRICAL", "DAMAGE", "OTHER");
    private static final List<String> PRIORITIES = List.of("LOW", "MEDIUM", "HIGH", "URGENT");
    private static final List<String> STATUSES = List.of("PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED");

    private final ReportDao reportDao;
    private final LookupDao lookupDao;

    public ReportController(ReportDao reportDao, LookupDao lookupDao) {
        this.reportDao = reportDao;
        this.lookupDao = lookupDao;
    }

    /** RQ04: the logged-in teacher's own reports. */
    @GetMapping("/reports/mine")
    public String myReports(@SessionAttribute("user") User user, Model model) {
        model.addAttribute("reports", reportDao.findByTeacher(user.getId()));
        return "reports/my";
    }

    /** RQ01: empty "Report an issue" form. The equipment can be pre-selected: /reports/new?equipmentId=1 */
    @GetMapping("/reports/new")
    public String newForm(@RequestParam(required = false) String equipmentId,
                          @SessionAttribute("user") User user, Model model) {
        Map<String, String> form = new HashMap<>();
        if (equipmentId != null) {
            form.put("equipmentId", equipmentId);
        }
        return showForm(user, model, form, false, null);
    }

    /** RQ01: the form was submitted. */
    @PostMapping("/reports/save")
    public String save(@RequestParam Map<String, String> form,
                       @SessionAttribute("user") User user,
                       Model model, RedirectAttributes redirect) {

        String equipmentId = form.getOrDefault("equipmentId", "");
        String issueType = form.getOrDefault("issueType", "");
        String priority = form.getOrDefault("priority", "");
        String description = form.getOrDefault("description", "").trim();
        String locationId = form.getOrDefault("locationId", "");
        boolean confirmed = form.containsKey("confirmDuplicate");

        // 1. Check the input
        String error = null;
        if (equipmentId.isEmpty()) {
            error = "Please choose the equipment.";
        } else if (!ISSUE_TYPES.contains(issueType) || !PRIORITIES.contains(priority)) {
            error = "Please choose an issue type and a priority.";
        } else if (description.length() < 10) {
            error = "Please describe the problem (at least 10 characters).";
        }
        if (error != null) {
            return showForm(user, model, form, false, error);
        }

        // 2. Duplicate warning: same equipment reported in the last 7 days
        if (!confirmed && reportDao.existsRecent(Long.parseLong(equipmentId))) {
            return showForm(user, model, form, true, null);
        }

        // 3. Save
        String referenceNo = reportDao.create(user.getSchoolId(), user.getId(),
                Long.parseLong(equipmentId),
                locationId.isEmpty() ? null : Long.parseLong(locationId),
                issueType, priority, description, form.get("locationDetails"));

        redirect.addFlashAttribute("success", "Report submitted. Your reference number is " + referenceNo);
        return "redirect:/reports/mine";
    }

    /** Report details + status history. */
    @GetMapping("/reports/{id}")
    public String detail(@PathVariable long id, @SessionAttribute("user") User user, Model model) {
        Report report = reportDao.findById(id, user.getSchoolId());   // only reports of the user's school

        if (report == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // Teachers may only open their own reports
        if (!user.isStaff() && report.getTeacherId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("report", report);
        model.addAttribute("history", reportDao.findHistory(id));
        model.addAttribute("statuses", STATUSES);
        return "reports/detail";
    }

    /** RQ06: staff changes the status (staff-only is checked in LoginInterceptor). */
    @PostMapping("/reports/{id}/status")
    public String updateStatus(@PathVariable long id,
                               @RequestParam String newStatus,
                               @RequestParam String comment,
                               @SessionAttribute("user") User user,
                               RedirectAttributes redirect) {

        if (reportDao.findById(id, user.getSchoolId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!STATUSES.contains(newStatus)) {
            redirect.addFlashAttribute("error", "Please choose a status.");
        } else if (comment.isBlank()) {
            redirect.addFlashAttribute("error", "A comment is required when changing the status.");
        } else {
            try {
                reportDao.updateStatus(id, user.getId(), newStatus, comment.trim());
                redirect.addFlashAttribute("success", "Status updated.");
            } catch (IllegalArgumentException e) {
                redirect.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/reports/" + id;
    }

    private String showForm(User user, Model model, Map<String, String> form,
                            boolean duplicateWarning, String error) {
        model.addAttribute("form", form);
        model.addAttribute("duplicateWarning", duplicateWarning);
        model.addAttribute("error", error);
        model.addAttribute("equipmentOptions", lookupDao.equipment(user.getSchoolId()));
        model.addAttribute("locations", lookupDao.locations(user.getSchoolId()));
        model.addAttribute("issueTypes", ISSUE_TYPES);
        model.addAttribute("priorities", PRIORITIES);
        return "reports/form";
    }
}
