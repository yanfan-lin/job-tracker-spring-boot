package com.yanfan.jobtracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Returns job application details to API clients.
public record JobApplicationResponse(
        Long id,
        String company,
        String title,
        String status,
        LocalDate dateApplied,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
