package com.yanfan.jobtracker.dto;

import java.time.LocalDateTime;

// Returns safe user details after registration.
public record AppUserResponse(
        Long id,
        String email,
        LocalDateTime createdAt) {

}
