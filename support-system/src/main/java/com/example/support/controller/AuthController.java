package com.example.support.controller;

import com.example.support.entity.User;
import com.example.support.repository.UserRepository;
import com.example.support.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    // Регистрация обычного пользователя (ОТКРЫТЫЙ эндпоинт)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        return register(request, "ROLE_USER");
    }

    // Создание админа (используй один раз!) - ОТКРЫТЫЙ эндпоинт
    @PostMapping("/setup-admin")
    public ResponseEntity<?> setupAdmin(@RequestBody RegistrationRequest request) {
        // Проверяем, есть ли уже админы
        if (userRepository.findAll().stream()
                .anyMatch(user -> user.getRole().equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body("Администратор уже существует");
        }

        return register(request, "ROLE_ADMIN");
    }

    // JWT аутентификация
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Map<String, Object> tokens = authService.authenticate(
                    request.getEmail(),
                    request.getPassword(),
                    httpRequest
            );
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication failed",
                    "message", e.getMessage()
            ));
        }
    }

    // Обновление токенов
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        try {
            Map<String, Object> tokens = authService.refreshTokens(request.getRefreshToken(), httpRequest);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Token refresh failed",
                    "message", e.getMessage()
            ));
        }
    }

    // Выход из системы
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Logout failed",
                    "message", e.getMessage()
            ));
        }
    }

    // Для проверки сессий (только для админа)
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserSessions(@RequestParam(required = false) Long userId) {
        try {
            return ResponseEntity.ok(Map.of(
                    "message", "Session endpoint - check database for session statuses",
                    "instruction", "Check user_sessions table in PostgreSQL for session statuses"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    private ResponseEntity<?> register(RegistrationRequest request, String role) {
        // Проверка email
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email уже используется");
        }

        // Проверка пароля
        if (!isValidPassword(request.getPassword())) {
            return ResponseEntity.badRequest().body(
                    "Пароль должен содержать: минимум 8 символов, цифру, " +
                            "заглавную и строчную букву, специальный символ (@#$%^&+=)"
            );
        }

        // Создание пользователя
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());
        response.put("message", "Пользователь успешно создан");

        return ResponseEntity.ok(response);
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    // DTO классы
    public static class RegistrationRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}