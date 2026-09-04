package com.yanfan.jobtracker.dto;

import java.time.LocalDateTime;

// Contains user details returned after registration.
public record AppUserResponse(
        Long id,
        String email,
        LocalDateTime createdAt) {

}
