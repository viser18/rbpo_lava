package com.example.support.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @GetMapping("/token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @GetMapping("/info")
    public String getCsrfInfo() {
        return """
               Для работы с защищенными эндпоинтами:
               1. Получите CSRF токен через GET /api/csrf/token
               2. Добавьте заголовок X-CSRF-TOKEN с полученным значением
               3. Или используйте куку XSRF-TOKEN (автоматически отправляется браузером)
               
               Публичные эндпоинты (/api/auth/**) не требуют CSRF токена.
               """;
    }
}