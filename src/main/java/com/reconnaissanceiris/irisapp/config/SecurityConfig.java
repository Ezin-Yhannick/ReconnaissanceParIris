package com.reconnaissanceiris.irisapp.config; // Adaptez le nom du package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver la protection CSRF, essentielle pour les requêtes POST
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 2. Autoriser l'accès sans authentification à votre route
                        .requestMatchers("/api/iris/enroll", "/api/iris/compare").permitAll()
                        // 3. Exiger l'authentification pour toutes les autres routes
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}