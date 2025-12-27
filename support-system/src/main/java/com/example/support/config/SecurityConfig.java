package com.example.support.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Отключаем CSRF полностью
                .csrf(csrf -> csrf.disable())

                // Настраиваем сессии как STATELESS для JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (доступны без аутентификации)
                        .requestMatchers(
                                "/api/auth/**",         // Регистрация, логин, обновление токенов
                                "/error",               // Ошибки
                                "/favicon.ico",         // Favicon
                                "/tickets/test",        // Тестовый эндпоинт
                                "/h2-console/**",       // H2 консоль (для разработки)
                                "/swagger-ui/**",       // Swagger UI
                                "/v3/api-docs/**"       // OpenAPI документация
                        ).permitAll()

                        // Статистика (только для админов и агентов)
                        .requestMatchers("/api/stats/**").hasAnyRole("ADMIN", "AGENT")

                        // Управление пользователями (только для админов)
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Управление агентами (только для админов)
                        .requestMatchers("/agents/**").hasRole("ADMIN")

                        // Управление SLA (только для админов)
                        .requestMatchers("/slas/**").hasRole("ADMIN")

                        // Тикеты (для пользователей, админов и агентов)
                        .requestMatchers("/tickets/**").hasAnyRole("USER", "ADMIN", "AGENT")

                        // Операции с тикетами (только для агентов и админов)
                        .requestMatchers("/api/tickets/**").hasAnyRole("AGENT", "ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Добавляем JWT фильтр перед UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Отключаем Basic Auth (опционально)
                .httpBasic(httpBasic -> httpBasic.disable())

                // Отключаем frameOptions для H2 консоли
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешаем все для тестирования
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8080",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Cache-Control",
                "Pragma"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}