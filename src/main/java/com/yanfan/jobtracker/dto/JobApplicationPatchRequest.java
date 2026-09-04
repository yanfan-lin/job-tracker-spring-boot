package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Carries optional changes to a job application.
public record JobApplicationPatchRequest(

        @Pattern(
                regexp = ".*\\S.*",
                message = "Company must not be blank")
        @Size(max = 255, message = "Company must not exceed 255 characters")
        String company,

        @Pattern(
                regexp = ".*\\S.*",
                message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Pattern(
                regexp = "applied|interview|offer|rejected",
                message = "Status must be one of: applied, interview, offer, rejected")
        String status,

        LocalDate dateApplied,

        String notes) {

}
