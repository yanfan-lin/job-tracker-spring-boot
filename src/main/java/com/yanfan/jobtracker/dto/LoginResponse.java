package com.yanfan.jobtracker.dto;

// Returns token details after login.
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

}
