package com.ems.controller;

import com.ems.dao.UserDao;
import com.ems.model.User;
import com.ems.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserDao userDao;

    public AuthController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";                       // templates/login.html
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session, Model model) {

        User user = userDao.login(email.trim(), PasswordUtil.sha256(password));

        if (user == null) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
        session.setAttribute("user", user);   // this is what "logged in" means
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
