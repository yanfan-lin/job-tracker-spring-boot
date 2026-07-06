package com.yanfan.jobtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


// security configuration for REST APIs
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.username}")
    private String appUsername;

    @Value("${app.security.password}")
    private String appPassword;

    // define which HTTP requests are public and which require authentication
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is disabled because this is a stateless REST API, not a browser form app
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // public read endpoints
                        .requestMatchers(HttpMethod.GET, "/applications/**").permitAll()

                        // public health check for local testing, Docker, and AWS health checks.
                        .requestMatchers("/actuator/health").permitAll()

                        // public API documentation paths
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // all other requests need authentication
                        .anyRequest().authenticated()
                )

                // HTTP basic auth
                .httpBasic(Customizer.withDefaults());

        return http.build();

    }

    // create in-memory user for basic auth
    // username and password come from application.properties and environment variables.
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername(appUsername)
                .password(passwordEncoder.encode(appPassword))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    // use BCrypt to encrypt the password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
