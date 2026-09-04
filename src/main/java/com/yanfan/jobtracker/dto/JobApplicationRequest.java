package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Accepts and validates a new job application.
public record JobApplicationRequest(

        @NotBlank(message = "Company is required")
        @Size(max = 255, message = "Company must not exceed 255 characters")
        String company,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Status is required")
        @Pattern(
                regexp = "applied|interview|offer|rejected",
                message = "Status must be one of: applied, interview, offer, rejected")
        String status,

        @NotNull(message = "Date applied is required")
        LocalDate dateApplied,

        String notes) {

}
