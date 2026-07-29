package com.yanfan.jobtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

// Configure JWT signing and validation
@Configuration
public class JwtConfig {

    // Convert the Base64 environment variable into an HS256 secret key
    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}") String encodedSecret
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedSecret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // Sign JWT access tokens created during login
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    // Verify JWT signatures, timestamps, and required claims
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        // Use the same HS256 secret key to verify incoming tokens
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Require every JWT to contain a positive numeric userId claim
        OAuth2TokenValidator<Jwt> userIdValidator = jwt -> {
            Object userIdClaim = jwt.getClaim("userId");

            // Reject tokens when userId is missing, non-numeric,
            // or less than or equal to 0
            if (!(userIdClaim instanceof Number userId) || userId.longValue() <= 0) {
                OAuth2Error error = new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "JWT userId claim must be a positive number",
                        null
                );

                return OAuth2TokenValidatorResult.failure(error);
            }

            return OAuth2TokenValidatorResult.success();
        };

        // Keep default timestamp checks and add the custom userId check
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(),
                        userIdValidator
                )
        );

        return decoder;

    }


}
