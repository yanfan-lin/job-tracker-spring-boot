package com.yanfan.jobtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// Configure stateless JWT security for the REST API
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Define public routes and enable JWT bearer authentication
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                // CSRF is disabled because JWTs are sent in the Authorization header, not cookies
                .csrf(csrf -> csrf.disable())

                // Do not create server-side sessions; each request must include its own JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // User registration and login are public
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        // All job application endpoints require authentication
                        .requestMatchers(
                                "/applications",
                                "/applications/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                // Read and validate bearer tokens from the Authorization header
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();

    }

    // Use BCrypt to hash and verify passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
