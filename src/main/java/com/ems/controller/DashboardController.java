package com.ems.controller;

import com.ems.model.enums.ReportStatus;
import com.ems.security.AppUserDetails;
import com.ems.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final ReportService reportService;

    public DashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** RQ05 / RQ11: staff dashboard with unresolved count, status counts and a filterable table. */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) ReportStatus status,
                            @AuthenticationPrincipal AppUserDetails user,
                            Model model) {
        Long schoolId = user.getSchoolId();
        model.addAttribute("unresolvedCount", reportService.countUnresolved(schoolId));
        model.addAttribute("counts", reportService.countByStatus(schoolId));
        model.addAttribute("reports", reportService.schoolReports(schoolId, status));
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("selectedStatus", status);
        return "dashboard";
    }
}
