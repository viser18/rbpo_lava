package com.example.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Настраиваем CSRF токены для использования в куках
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
                // Настройка CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Настройка CSRF
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                "/api/auth/**",        // Отключаем CSRF для публичных эндпоинтов
                                "/h2-console/**"      // Отключаем для H2 консоли (если используется)
                        )
                )

                // Настройка авторизации
                .authorizeHttpRequests(auth -> auth
                        // ПУБЛИЧНЫЕ эндпоинты (доступны всем)
                        .requestMatchers(
                                "/api/auth/**",
                                "/error",
                                "/favicon.ico",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Эндпоинты статистики доступны админам и агентам
                        .requestMatchers("/api/stats/**").hasAnyRole("ADMIN", "AGENT")

                        // ЗАЩИЩЕННЫЕ эндпоинты
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/agents/**").hasRole("ADMIN")
                        .requestMatchers("/slas/**").hasRole("ADMIN")
                        .requestMatchers("/tickets/**").hasAnyRole("USER", "ADMIN", "AGENT")
                        .requestMatchers("/api/tickets/**").hasAnyRole("AGENT", "ADMIN")

                        // Все остальные требуют любой аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // Для H2 консоли (если используется)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React app
                "http://localhost:8080"   // Само приложение
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-CSRF-TOKEN",
                "Accept"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}