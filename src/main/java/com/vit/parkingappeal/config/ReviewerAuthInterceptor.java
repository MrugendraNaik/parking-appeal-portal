package com.vit.parkingappeal.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/** Keeps /reviewer/** behind the reviewer login (simple session flag; enough for the MVP). */
public class ReviewerAuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "REVIEWER_AUTHENTICATED";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_KEY))) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/reviewer/login");
        return false;
    }
}
