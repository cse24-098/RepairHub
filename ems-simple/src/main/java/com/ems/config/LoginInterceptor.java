package com.ems.config;

import com.ems.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs BEFORE every controller method (see WebConfig for which URLs).
 * 1. Not logged in?  -> go to the login page.
 * 2. Staff-only page but the user is a teacher? -> 403 Forbidden.
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;                       // stop: do not call the controller
        }

        String path = request.getRequestURI();
        boolean staffOnly = path.equals("/dashboard")
                || path.equals("/equipment/new")
                || path.equals("/equipment/save")
                || path.matches("/reports/\\d+/status");

        if (staffOnly && !user.isStaff()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;                            // OK: continue to the controller
    }
}
