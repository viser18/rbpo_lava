package com.example.support.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
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
        // Убедимся что секрет достаточно длинный (минимум 256 бит = 32 байта)
        if (secret.length() < 32) {
            String base = "mySuperSecretKeyForJWTWithAtLeast256BitsLengthHere";
            while (secret.length() < 32) {
                secret = secret + base;
            }
            secret = secret.substring(0, 32);
        }

        // Конвертируем строку в байты для ключа
        byte[] keyBytes = secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
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
        return extractClaim(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String getTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Валидация токенов
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            final String tokenType = getTokenType(token);

            // Проверяем что это access токен
            if (!"ACCESS".equals(tokenType)) {
                return false;
            }

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
            // Пробуем распарсить токен
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Malformed JWT token: " + e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            // Истекший токен все еще имеет валидную структуру
            return true;
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT token: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    // Конвертация дат
    public LocalDateTime getExpirationLocalDateTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return Instant.ofEpochMilli(expiration.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            // Возвращаем дату по умолчанию если не можем распарсить
            return LocalDateTime.now().plusSeconds(refreshTokenExpiration);
        }
    }

    // Дополнительные методы для отладки
    public void printTokenInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            System.out.println("=== JWT Token Info ===");
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Type: " + claims.get("type"));
            System.out.println("Issued At: " + claims.getIssuedAt());
            System.out.println("Expiration: " + claims.getExpiration());
            System.out.println("Role: " + claims.get("role"));
            System.out.println("======================");
        } catch (Exception e) {
            System.err.println("Failed to parse token: " + e.getMessage());
        }
    }

    // Проверка refresh токена
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenType(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    // Проверка access токена
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenType(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}