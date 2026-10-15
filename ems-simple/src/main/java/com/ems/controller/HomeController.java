package com.ems.controller;

import com.ems.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class HomeController {

    /** After login everybody lands on "/": staff see the dashboard, teachers see their reports. */
    @GetMapping("/")
    public String home(@SessionAttribute("user") User user) {
        return user.isStaff() ? "redirect:/dashboard" : "redirect:/reports/mine";
    }
}
