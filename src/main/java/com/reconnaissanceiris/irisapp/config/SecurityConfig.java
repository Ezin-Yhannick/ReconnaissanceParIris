package com.reconnaissanceiris.irisapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Activer CORS avec notre configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Désactiver CSRF (protection pas nécessaire pour une API REST)
                .csrf(csrf -> csrf.disable())

                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/admin-login",
                                "/api/auth/**",
                                "/api/iris/**",
                                "/api/test/**",
                                "/api/stats/**",
                                "/api/users/**",
                                "/api/admin/**",
                                "/uploads/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Configuration CORS intégrée à Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origins autorisées (frontend)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8000",
                "http://127.0.0.1:8000",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Autoriser les credentials (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Durée du cache preflight (1 heure)
        configuration.setMaxAge(3600L);

        // Appliquer cette config à toutes les routes /api/**
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}