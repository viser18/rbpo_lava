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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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
        // Упрощенная настройка CSRF для Postman тестирования
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");
        tokenRepository.setCookieName("XSRF-TOKEN");
        tokenRepository.setHeaderName("X-XSRF-TOKEN");
        tokenRepository.setCookieMaxAge(86400);

        // Новый обработчик для Spring Security 6+
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Настраиваем сессии как STATELESS для JWT
                // Но оставляем возможность CSRF для некоторых эндпоинтов
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().migrateSession()
                )

                // ВАЖНО: Настраиваем CSRF для тестирования
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        // Разрешаем POST без CSRF для отладки (можно убрать позже)
                        .ignoringRequestMatchers(
                                "/api/auth/**",     // Эндпоинты аутентификации
                                "/error",           // Эндпоинты ошибок
                                "/h2-console/**",   // H2 консоль
                                "/tickets/test"     // Для тестирования
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (доступны без аутентификации)
                        .requestMatchers(
                                "/api/auth/**",         // Регистрация, логин, обновление токенов
                                "/api/csrf/info",       // Информация о CSRF
                                "/error",               // Ошибки
                                "/favicon.ico",         // Favicon
                                "/tickets/test",        // Тестовый эндпоинт
                                "/h2-console/**"        // H2 консоль (для разработки)
                        ).permitAll()

                        // Эндпоинты с CSRF (требуют аутентификации + CSRF токен)
                        .requestMatchers("/api/csrf/**").authenticated()

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

                // Оставляем Basic Auth для обратной совместимости и тестирования
                .httpBasic(httpBasic -> {})

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

        // Разрешаем все для тестирования (в production ограничьте конкретными доменами)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8080",
                "http://localhost:5173",  // Vite dev server
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-XSRF-TOKEN",
                "Accept",
                "Origin",
                "Cache-Control",
                "Pragma"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-XSRF-TOKEN",
                "Set-Cookie"
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