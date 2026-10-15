package com.ems.controller;

import com.ems.dao.ReportDao;
import com.ems.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private static final List<String> STATUSES = List.of("PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED");

    private final ReportDao reportDao;

    public DashboardController(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /** RQ05 + RQ11: staff dashboard. Example URL: /dashboard?status=PENDING */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String status,
                            @SessionAttribute("user") User user,
                            Model model) {

        if (status != null && !STATUSES.contains(status)) {
            status = null;                                   // ignore invalid filter values
        }

        Map<String, Integer> counts = reportDao.countByStatus(user.getSchoolId());
        int unresolved = counts.get("PENDING") + counts.get("IN_PROGRESS");

        model.addAttribute("counts", counts);
        model.addAttribute("unresolved", unresolved);
        model.addAttribute("reports", reportDao.findBySchool(user.getSchoolId(), status));
        model.addAttribute("statuses", STATUSES);
        model.addAttribute("selectedStatus", status);
        return "dashboard";
    }
}
