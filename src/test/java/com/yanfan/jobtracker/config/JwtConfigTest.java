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

// test the real JWT validation rules
class JwtConfigTest {

    private JwtEncoder jwtEncoder;

    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        // fixed secret used inside tests
        String testSecret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.UTF_8)
        );

        JwtConfig jwtConfig = new JwtConfig();

        SecretKey secretKey = jwtConfig.jwtSecretKey(testSecret);

        // use real encoder and decoder
        jwtEncoder = jwtConfig.jwtEncoder(secretKey);
        jwtDecoder = jwtConfig.jwtDecoder(secretKey);

    }

    // test if a signed token is still invalid when userId is missing
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

    // verifies that userId must be stored as a number rather than text
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

    // helper to create a correctly signed JWT with customizable userId content
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

        // the missing-userId tests will skip this claim
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
