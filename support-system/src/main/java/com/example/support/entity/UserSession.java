package com.example.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String refreshToken;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    // Конструкторы
    public UserSession() {
        this.createdAt = LocalDateTime.now();
    }

    public UserSession(User user, String refreshToken, LocalDateTime expiresAt) {
        this();
        this.user = user;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.status = SessionStatus.ACTIVE;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastRefreshedAt() { return lastRefreshedAt; }
    public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) { this.lastRefreshedAt = lastRefreshedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    // Вспомогательные методы
    public boolean isActive() {
        return status == SessionStatus.ACTIVE &&
                LocalDateTime.now().isBefore(expiresAt);
    }

    public void refresh(LocalDateTime newExpiresAt) {
        this.lastRefreshedAt = LocalDateTime.now();
        this.expiresAt = newExpiresAt;
        this.status = SessionStatus.REFRESHED;
    }

    public void revoke() {
        this.status = SessionStatus.REVOKED;
    }
}