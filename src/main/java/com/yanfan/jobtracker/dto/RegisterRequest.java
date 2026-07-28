package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Accept and validate registration data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Allow Jackson to convert JSON into this DTO
    public RegisterRequest() {

    }

    public RegisterRequest(String email, String password) {
        this.setEmail(email);
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    // Trim surrounding spaces before validation and normalization
    public void setEmail(String email) {

        this.email = email == null ? null : email.trim();

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
