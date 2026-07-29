package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Accept and validate data for creating a job application
public class JobApplicationRequest {

    // Require company and title
    @NotBlank(message = "Company is required")
    @Size(max = 255, message = "Company must not exceed 255 characters")
    private String company;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    // Require one of the supported status values
    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "applied|interview|offer|rejected",
            message = "Status must be one of: applied, interview, offer, rejected"
    )
    private String status;

    @NotNull(message = "Date applied is required")
    private LocalDate dateApplied;

    private String notes;


    // Allow Jackson to convert JSON into this DTO
    public JobApplicationRequest() {

    }

    public JobApplicationRequest(String company, String title, String status, LocalDate dateApplied, String notes) {
        this.company = company;
        this.title = title;
        this.status = status;
        this.dateApplied = dateApplied;
        this.notes = notes;
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public void setDateApplied(LocalDate dateApplied) {
        this.dateApplied = dateApplied;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
