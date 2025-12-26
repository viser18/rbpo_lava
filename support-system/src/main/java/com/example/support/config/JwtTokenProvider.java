package com.example.support.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationWithAtLeast256Bits}")
    private String secret;

    @Value("${jwt.access.expiration:900}") // 15 минут
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:604800}") // 7 дней
    private Long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Убедимся что секрет достаточно длинный
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Генерация токенов
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ACCESS");
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Извлечение информации из токена
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String getTokenType(String token) {
        return getClaimFromToken(token, claims -> claims.get("type", String.class));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Валидация токенов
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateTokenStructure(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Конвертация дат
    public LocalDateTime getExpirationLocalDateTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return LocalDateTime.now().plusSeconds(refreshTokenExpiration);
        }
        return Instant.ofEpochMilli(expiration.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // Дополнительные методы для удобства
    public Date getExpirationDateFromRefreshToken(String refreshToken) {
        try {
            return getExpirationDateFromToken(refreshToken);
        } catch (Exception e) {
            return new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000);
        }
    }
}