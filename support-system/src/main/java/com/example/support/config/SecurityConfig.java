package com.example.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ПУБЛИЧНЫЕ эндпоинты (доступны всем)
                        .requestMatchers("/api/auth/**").permitAll()

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

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}