package com.ems.controller;

import com.ems.dao.ReportDao;
import com.ems.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalController {

    private final ReportDao reportDao;

    public GlobalController(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /** Makes `unreadNotifications` available to every HTML page for the logged-in user. */
    @ModelAttribute("unreadNotifications")
    public List<Map<String, Object>> getUnreadNotifications(@SessionAttribute(name = "user", required = false) User user) {
        if (user == null) {
            return List.of(); // Empty list if not logged in
        }
        return reportDao.findUnreadNotifications(user.getId());
    }
}