package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Accept optional fields for partially updating a job application
public class JobApplicationPatchRequest {

    @Pattern(
            regexp = ".*\\S.*",
            message = "Company must not be blank"
    )
    @Size(max = 255, message = "Company must not exceed 255 characters")
    private String company;


    @Pattern(
            regexp = ".*\\S.*",
            message = "Title must not be blank"
    )
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;


    // Validate status only when it is provided
    @Pattern(
            regexp = "applied|interview|offer|rejected",
            message = "Status must be one of: applied, interview, offer, rejected"
    )
    private String status;

    private LocalDate dateApplied;

    private String notes;

    // Allow Jackson to convert JSON into this DTO
    public JobApplicationPatchRequest() {

    }

    public JobApplicationPatchRequest(String company, String title, String status, LocalDate dateApplied, String notes) {
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
