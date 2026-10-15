package com.ems.controller;

import com.ems.dto.ReportForm;
import com.ems.dto.StatusUpdateForm;
import com.ems.model.MaintenanceReport;
import com.ems.model.enums.IssueType;
import com.ems.model.enums.Priority;
import com.ems.model.enums.ReportStatus;
import com.ems.repository.LocationRepository;
import com.ems.security.AppUserDetails;
import com.ems.service.EquipmentService;
import com.ems.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final EquipmentService equipmentService;
    private final LocationRepository locationRepository;

    public ReportController(ReportService reportService, EquipmentService equipmentService,
                            LocationRepository locationRepository) {
        this.reportService = reportService;
        this.equipmentService = equipmentService;
        this.locationRepository = locationRepository;
    }

    /** RQ04: the teacher's own reports. */
    @GetMapping("/mine")
    public String mine(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("reports", reportService.myReports(user.getId()));
        return "reports/my";
    }

    /** RQ01: show the "Report an issue" form (optionally pre-selecting equipment). */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long equipmentId,
                          @AuthenticationPrincipal AppUserDetails user, Model model) {
        ReportForm form = new ReportForm();
        form.setEquipmentId(equipmentId);
        model.addAttribute("reportForm", form);
        addFormData(model, user);
        return "reports/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("reportForm") ReportForm form,
                       BindingResult result,
                       @AuthenticationPrincipal AppUserDetails user,
                       Model model, RedirectAttributes redirect) {
        boolean duplicate = !result.hasErrors() && !form.isConfirmDuplicate()
                && reportService.isDuplicate(form, user);
        if (result.hasErrors() || duplicate) {
            model.addAttribute("duplicateWarning", duplicate);
            addFormData(model, user);
            return "reports/form";
        }
        MaintenanceReport saved = reportService.submit(form, user);
        redirect.addFlashAttribute("success",
                "Report submitted. Your reference number is " + saved.getReferenceNo());
        return "redirect:/reports/mine";
    }

    /** Report details + audit trail (+ status form for staff). */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, Model model) {
        addDetailData(model, id, user);
        model.addAttribute("statusForm", new StatusUpdateForm());
        return "reports/detail";
    }

    /** RQ06: staff changes the status. */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @Valid @ModelAttribute("statusForm") StatusUpdateForm form,
                               BindingResult result,
                               @AuthenticationPrincipal AppUserDetails user,
                               Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                reportService.updateStatus(id, form, user);
                redirect.addFlashAttribute("success", "Status updated.");
                return "redirect:/reports/" + id;
            } catch (IllegalArgumentException ex) {
                result.rejectValue("newStatus", "invalid", ex.getMessage());
            }
        }
        addDetailData(model, id, user);
        return "reports/detail";
    }

    private void addFormData(Model model, AppUserDetails user) {
        model.addAttribute("equipmentList", equipmentService.list(user.getSchoolId(), null));
        model.addAttribute("locations", locationRepository.findBySchoolIdOrderByBuildingAscRoomAsc(user.getSchoolId()));
        model.addAttribute("issueTypes", IssueType.values());
        model.addAttribute("priorities", Priority.values());
    }

    private void addDetailData(Model model, Long id, AppUserDetails user) {
        model.addAttribute("report", reportService.getReport(id, user));
        model.addAttribute("history", reportService.getHistory(id));
        model.addAttribute("statuses", ReportStatus.values());
    }
}
