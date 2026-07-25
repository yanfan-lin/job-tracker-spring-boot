package com.yanfan.jobtracker.dto;

import java.time.LocalDateTime;

// response DTO containing safe user information after registration
public class AppUserResponse {

    private Long id;
    private String email;
    private LocalDateTime createdAt;


    public AppUserResponse() {

    }

    public AppUserResponse(Long id, String email, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
