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

// JWT signing and verification configuration
@Configuration
public class JwtConfig {

    // converts the Base64 environment variable into an HS256 secret key
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

    // signs JWT access tokens created during login
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    // verifies JWT signatures, timestamps, and required claims
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        // verify that the JWT was signed with secret key
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // verify that every JWT contains a valid userId claim
        OAuth2TokenValidator<Jwt> userIdValidator = jwt -> {
            Object userIdClaim = jwt.getClaim("userId");

            // reject tokens when userId is missing, non-numeric, or <= 0
            if (!(userIdClaim instanceof Number userId) || userId.longValue() <= 0) {
                OAuth2Error error = new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "JWT userId claim must be a positive number",
                        null
                );

                return OAuth2TokenValidatorResult.failure(error);
            }

            // userId validation passed
            return OAuth2TokenValidatorResult.success();
        };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(),
                        userIdValidator
                )
        );

        return decoder;
    }


}
