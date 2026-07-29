package com.yanfan.jobtracker.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Test the real JWT validation rules
class JwtConfigTest {

    private JwtEncoder jwtEncoder;

    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        // Use a fixed 32-byte secret for tests
        String testSecret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.UTF_8)
        );

        JwtConfig jwtConfig = new JwtConfig();

        SecretKey secretKey = jwtConfig.jwtSecretKey(testSecret);

        // Use the real encoder and decoder
        jwtEncoder = jwtConfig.jwtEncoder(secretKey);
        jwtDecoder = jwtConfig.jwtDecoder(secretKey);

    }

    // Verify that a signed token is rejected when userId is missing
    @Test
    void jwtDecoder_shouldRejectTokenWhenUserIdIsMissing() {

        String token = createToken(
                null,
                false
        );

        assertThatThrownBy(() ->
                jwtDecoder.decode(token)
        )
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining(
                        "JWT userId claim must be a positive number"
                );

    }

    // Verify that userId must be stored as a number rather than text
    @Test
    void jwtDecoder_shouldRejectTokenWhenUserIdIsNotNumeric() {

        String token = createToken(
                "not-a-number",
                true
        );

        assertThatThrownBy(() ->
                jwtDecoder.decode(token)
        )
                .isInstanceOf(JwtValidationException.class)
                .hasMessageContaining(
                        "JWT userId claim must be a positive number"
                );

    }

    // Verify that an expired JWT is rejected
    @Test
    void jwtDecoder_shouldRejectExpiredToken() {

        // Create a token that expired one hour ago
        Instant issuedAt = Instant.now().minusSeconds(7200);
        Instant expiresAt = issuedAt.plusSeconds(3600);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("person@example.com")
                .claim("userId", 42L)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        // Expect the timestamp validator to reject the expired token
        assertThatThrownBy(() -> jwtDecoder.decode(token))
                .isInstanceOf(JwtValidationException.class);

    }

    // Create a correctly signed JWT with customizable userId content
    private String createToken(
            Object userIdClaim,
            boolean includeUserId
    ) {
        Instant issuedAt = Instant.now();

        JwtClaimsSet.Builder claimsBuilder =
                JwtClaimsSet.builder()
                        .subject("person@example.com")
                        .issuedAt(issuedAt)
                        .expiresAt(issuedAt.plusSeconds(3600)
                        );

        // Skip the userId claim for the missing-claim test
        if (includeUserId) {
            claimsBuilder.claim("userId", userIdClaim);
        }

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        JwtEncoderParameters parameters =
                JwtEncoderParameters.from(
                        header,
                        claimsBuilder.build()
                );

        return jwtEncoder
                .encode(parameters)
                .getTokenValue();

    }


}
