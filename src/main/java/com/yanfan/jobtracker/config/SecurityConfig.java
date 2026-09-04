package com.yanfan.jobtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// Configures authentication and public routes.
@Configuration
public class SecurityConfig {

    private final boolean swaggerPublic;

    public SecurityConfig(
            @Value("${app.swagger.public:false}") boolean swaggerPublic)
    {
        this.swaggerPublic = swaggerPublic;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF protection is unnecessary because authentication uses headers instead of cookies
                .csrf(csrf -> csrf.disable())

                // Each request carries its own JWT, so the server does not need sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            HttpMethod.POST,
                            "/auth/register",
                            "/auth/login"
                    ).permitAll();

                    if (swaggerPublic) {
                        auth.requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll();
                    }

                    auth.anyRequest().authenticated();
                })

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
