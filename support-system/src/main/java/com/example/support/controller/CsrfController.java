package com.example.support.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    /**
     * Получить CSRF токен (работает на всю сессию) - СОВМЕСТИМЫЙ С POSTMAN
     */
    @GetMapping("/get-token")
    public Map<String, Object> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        HttpSession session = request.getSession(true); // true = создаем если нет

        Map<String, Object> response = new HashMap<>();

        if (csrfToken != null) {
            // Сохраняем токен в сессии
            session.setAttribute("csrfToken", csrfToken.getToken());
            session.setMaxInactiveInterval(86400); // 24 часа

            response.put("status", "success");
            response.put("csrfToken", csrfToken.getToken());
            response.put("cookieName", "XSRF-TOKEN");
            response.put("headerName", "X-XSRF-TOKEN");
            response.put("sessionId", session.getId());
            response.put("createdAt", new java.util.Date(session.getCreationTime()));
            response.put("validFor", "24 hours");
            response.put("instruction", "This token will work for all requests in this session");
        } else {
            response.put("status", "error");
            response.put("message", "CSRF token not generated");
        }

        return response;
    }

    /**
     * Информация о CSRF
     */
    @GetMapping("/info")
    public Map<String, Object> getCsrfInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("purpose", "Protection against Cross-Site Request Forgery (CSRF)");
        info.put("status", "enabled");
        info.put("method", "Cookie-based CSRF Token");
        info.put("sessionDuration", "24 hours");
        info.put("cookieName", "XSRF-TOKEN");
        info.put("headerName", "X-XSRF-TOKEN");

        info.put("instruction", "1. Execute GET /api/csrf/get-token with Basic Auth\n" +
                "2. Get token and XSRF-TOKEN cookie\n" +
                "3. Token will work for 24 hours");

        return info;
    }

    /**
     * Проверить CSRF токен (СОВМЕСТИМЫЙ С POSTMAN)
     */
    @GetMapping("/check-token")
    public Map<String, Object> checkCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

        Map<String, Object> response = new HashMap<>();

        if (session != null) {
            response.put("status", "session_active");
            response.put("sessionId", session.getId());
            response.put("created", new java.util.Date(session.getCreationTime()));
            response.put("lastAccessed", new java.util.Date(session.getLastAccessedTime()));

            Object sessionCsrf = session.getAttribute("csrfToken");
            response.put("csrfTokenInSession", sessionCsrf != null ? sessionCsrf.toString() : "not_found");

            if (csrfToken != null) {
                response.put("currentCsrfToken", csrfToken.getToken());
            }

            response.put("maxInactiveInterval", session.getMaxInactiveInterval() + " seconds");
            response.put("timeRemaining", (session.getMaxInactiveInterval() -
                    (System.currentTimeMillis() - session.getLastAccessedTime()) / 1000) + " seconds");
        } else {
            response.put("status", "no_session");
            response.put("message", "Execute GET /api/csrf/get-token to create session");
        }

        return response;
    }

    /**
     * Проверить сессию
     */
    @GetMapping("/session")
    public Map<String, Object> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();

        if (session != null) {
            response.put("status", "session_active");
            response.put("id", session.getId());
            response.put("created", new java.util.Date(session.getCreationTime()));
            response.put("lastAccessed", new java.util.Date(session.getLastAccessedTime()));
            response.put("csrfTokenInSession", session.getAttribute("csrfToken"));
            response.put("maxInactiveInterval", session.getMaxInactiveInterval() + " seconds");
        } else {
            response.put("status", "no_session");
            response.put("message", "Execute GET /api/csrf/get-token to create session");
        }

        return response;
    }
}