package com.yanfan.jobtracker.service;


import com.yanfan.jobtracker.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

// Create signed JWT access tokens
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final long expirationSeconds;

    // Constructor injection
    @Autowired
    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    // Create a signed access token for an authenticated user
    public String generateToken(AppUser user) {

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        // Use HS256 to match the encoder and decoder configuration
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        // Store the email as the subject and the database user ID as a custom claim
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("userId", user.getId())
                .build();

        JwtEncoderParameters parameters =
                JwtEncoderParameters.from(header, claims);

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    public long getExpirationSeconds() {
        return this.expirationSeconds;
    }


}
