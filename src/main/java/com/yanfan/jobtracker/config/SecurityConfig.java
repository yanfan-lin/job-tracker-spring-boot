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


// security configuration for REST APIs
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // defines public endpoints and JWT-protected endpoints
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT authentication does not use browser from sessions
                .csrf(csrf -> csrf.disable())

                // each request must carry its own JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // user registration and login are public
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        // all read endpoints are public
                        .requestMatchers(HttpMethod.GET, "/**")
                        .permitAll()

                        // all other requests requires a valid JWT
                        .anyRequest().authenticated()
                )

                // Reads and validates Authorization: Bearer <JWT>
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();

    }

    // use BCrypt to encrypt the password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
