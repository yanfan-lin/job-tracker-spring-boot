package com.yanfan.jobtracker.dto;

// Contains token details returned after login.
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

}
