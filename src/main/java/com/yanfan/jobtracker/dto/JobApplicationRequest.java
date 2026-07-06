package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

// requested DTO used when creating a new job application
public class JobApplicationRequest {

    // company and title are required fields
    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Title is required")
    private String title;

    // status is required and must match one of the supported status values
    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "applied|interview|offer|rejected",
            message = "Status must be one of: applied, interview, offer, rejected"
    )
    private String status;

    @NotNull(message = "Date applied is required")
    private LocalDate dateApplied;

    // optional
    private String notes;


    // required by Jackson so Spring can convert JSON into this DTO
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
