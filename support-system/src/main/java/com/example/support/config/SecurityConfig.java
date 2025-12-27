package com.example.support.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.port:8443}")
    private int serverPort;

    @Value("${server.http.port:8080}")
    private int httpPort;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Настройка CSRF с безопасными cookies для HTTPS
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");
        tokenRepository.setCookieName("XSRF-TOKEN");
        tokenRepository.setHeaderName("X-XSRF-TOKEN");
        tokenRepository.setCookieMaxAge(86400);
        tokenRepository.setSecure(true); // Только для HTTPS
        tokenRepository.setCookieHttpOnly(false); // Для доступа из JS

        // Новый обработчик для Spring Security 6+
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Настраиваем сессии как STATELESS для JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().migrateSession()
                )

                // Настройка CSRF с безопасными параметрами для HTTPS
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        // Разрешаем POST без CSRF для эндпоинтов аутентификации и WebSocket
                        .ignoringRequestMatchers(
                                "/api/auth/**",         // Эндпоинты аутентификации
                                "/error",               // Эндпоинты ошибок
                                "/h2-console/**",       // H2 консоль (только для разработки)
                                "/tickets/test",        // Тестовый эндпоинт
                                "/ws/**",              // WebSocket
                                "/actuator/**"         // Actuator endpoints
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
                                "/h2-console/**",       // H2 консоль (для разработки)
                                "/actuator/health",     // Health check
                                "/actuator/info",       // Application info
                                "/swagger-ui/**",       // Swagger UI
                                "/v3/api-docs/**",      // OpenAPI docs
                                "/swagger-resources/**" // Swagger resources
                        ).permitAll()

                        // Эндпоинты с CSRF (требуют аутентификации + CSRF токен)
                        .requestMatchers("/api/csrf/**").authenticated()

                        // WebSocket эндпоинты (требуют аутентификации)
                        .requestMatchers("/ws/**").authenticated()

                        // Actuator эндпоинты (только для админов)
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

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

                // Отключаем Basic Auth для production, оставляем для тестирования
                .httpBasic(httpBasic -> {})

                // Настройка заголовков безопасности для HTTPS
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Разрешаем фреймы с того же origin
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';")
                        )
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)
                        )
                        // Убираем проблемную XSS защиту или используем совместимый метод
                        .xssProtection(xss -> {})
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

        // Разрешенные origins для HTTPS и HTTP (для разработки)
        configuration.setAllowedOrigins(Arrays.asList(
                "https://localhost:" + serverPort,
                "https://localhost:3000",
                "https://127.0.0.1:" + serverPort,
                "https://127.0.0.1:3000",
                "http://localhost:" + httpPort,         // HTTP редирект порт
                "http://localhost:3000",               // Dev frontend
                "http://127.0.0.1:" + httpPort,        // HTTP редирект порт
                "http://127.0.0.1:3000",               // Dev frontend
                "http://localhost:5173",               // Vite dev server
                "http://127.0.0.1:5173"                // Vite dev server
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
                "Pragma",
                "Upgrade",
                "Connection",
                "Sec-WebSocket-Key",
                "Sec-WebSocket-Version",
                "Sec-WebSocket-Extensions"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-XSRF-TOKEN",
                "Set-Cookie",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
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