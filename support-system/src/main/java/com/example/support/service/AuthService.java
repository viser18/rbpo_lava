package com.example.support.service;

import com.example.support.config.JwtTokenProvider;
import com.example.support.entity.SessionStatus;
import com.example.support.entity.User;
import com.example.support.entity.UserSession;
import com.example.support.repository.UserRepository;
import com.example.support.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public Map<String, Object> authenticate(String email, String password, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Генерируем токены
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Создаем сессию
        LocalDateTime refreshExpiresAt = jwtTokenProvider.getExpirationLocalDateTime(refreshToken);
        UserSession session = new UserSession(user, refreshToken, refreshExpiresAt);

        // Добавляем информацию о клиенте
        session.setIpAddress(getClientIp(request));
        session.setUserAgent(request.getHeader("User-Agent"));

        userSessionRepository.save(session);

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtTokenProvider.getExpirationDateFromToken(accessToken).getTime());
        response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
        response.put("sessionId", session.getId());

        return response;
    }

    public Map<String, Object> refreshTokens(String refreshToken, HttpServletRequest request) {
        System.out.println("=== REFRESH TOKEN PROCESS ===");
        System.out.println("Refresh token received: " + refreshToken);

        // Вначале проверяем структуру токена
        if (!jwtTokenProvider.validateTokenStructure(refreshToken)) {
            System.err.println("Invalid refresh token structure");
            throw new RuntimeException("Invalid refresh token structure");
        }

        // Проверяем что это refresh токен
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            System.err.println("Token is not a refresh token");
            throw new RuntimeException("Invalid token type. Expected REFRESH token");
        }

        // Находим сессию по refresh токену
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    System.err.println("Session not found for refresh token");
                    return new RuntimeException("Session not found");
                });

        // Проверяем статус сессии
        if (session.getStatus() != SessionStatus.ACTIVE) {
            System.err.println("Session is not active. Status: " + session.getStatus());
            throw new RuntimeException("Session is not active. Status: " + session.getStatus());
        }

        // Проверяем не истек ли токен
        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            System.err.println("Refresh token expired");
            session.setStatus(SessionStatus.EXPIRED);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        // Получаем пользователя из сессии
        User user = session.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Генерируем новую пару токенов
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        System.out.println("New access token generated: " + newAccessToken.substring(0, 30) + "...");
        System.out.println("New refresh token generated: " + newRefreshToken.substring(0, 30) + "...");

        // Обновляем сессию
        LocalDateTime newExpiresAt = jwtTokenProvider.getExpirationLocalDateTime(newRefreshToken);

        // Сохраняем старый токен в историю (опционально)
        System.out.println("Old refresh token: " + session.getRefreshToken().substring(0, 30) + "...");

        // Обновляем сессию новым токеном
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(newExpiresAt);
        session.setLastRefreshedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.REFRESHED);
        session.setIpAddress(getClientIp(request));
        session.setUserAgent(request.getHeader("User-Agent"));

        userSessionRepository.save(session);
        System.out.println("Session updated with new refresh token");

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtTokenProvider.getExpirationDateFromToken(newAccessToken).getTime());
        response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
        response.put("sessionId", session.getId());
        response.put("message", "Tokens refreshed successfully");

        System.out.println("=== REFRESH COMPLETE ===");
        return response;
    }

    public void logout(String refreshToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        sessionOpt.ifPresent(session -> {
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
        });
    }

    public void logoutAllUserSessions(Long userId) {
        userSessionRepository.revokeAllUserSessions(userId);
    }

    public Map<String, Object> checkSessionStatus(String refreshToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);

        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            Map<String, Object> status = new HashMap<>();
            status.put("sessionId", session.getId());
            status.put("status", session.getStatus().toString());
            status.put("createdAt", session.getCreatedAt());
            status.put("expiresAt", session.getExpiresAt());
            status.put("lastRefreshedAt", session.getLastRefreshedAt());
            status.put("isActive", session.isActive());
            status.put("userId", session.getUser().getId());
            return status;
        }

        throw new RuntimeException("Session not found");
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}