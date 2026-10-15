package com.ems.controller;

import com.ems.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** After login everyone lands on "/". Send staff to the dashboard, teachers to their reports. */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUserDetails user) {
        return user.isStaff() ? "redirect:/dashboard" : "redirect:/reports/mine";
    }
}
