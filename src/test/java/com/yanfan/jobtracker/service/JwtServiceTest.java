package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.config.JwtConfig;
import com.yanfan.jobtracker.model.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void generateToken_shouldCreateSignedTokenWithExpectedClaims() {

        String testSecret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.UTF_8)
        );

        JwtConfig jwtConfig = new JwtConfig();

        SecretKey secretKey = jwtConfig.jwtSecretKey(testSecret);

        JwtEncoder jwtEncoder = jwtConfig.jwtEncoder(secretKey);

        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder(secretKey);

        JwtService jwtService = new JwtService(jwtEncoder, 3600L);

        AppUser user = mock(AppUser.class);

        when(user.getId())
                .thenReturn(42L);

        when(user.getEmail())
                .thenReturn("person@example.com");

        String token = jwtService.generateToken(user);

        Jwt decodedToken = jwtDecoder.decode(token);

        assertThat(decodedToken.getSubject())
                .isEqualTo("person@example.com");

        Number userIdClaim = decodedToken.getClaim("userId");

        assertThat(userIdClaim.longValue())
                .isEqualTo(42L);

        assertThat(Duration.between(
                decodedToken.getIssuedAt(),
                decodedToken.getExpiresAt()).getSeconds())
                .isEqualTo(3600);
    }

}
