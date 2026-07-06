package com.yanfan.jobtracker.dto;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

// request DTO use when partially updating an existing job application
// ALL fields are optional
public class JobApplicationPatchRequest {

    private String company;

    private String title;

    // if provided, status must be valid
    @Pattern(
            regexp = "applied|interview|offer|rejected",
            message = "Status must be one of: applied, interview, offer, rejected"
    )
    private String status;

    private LocalDate dateApplied;

    private String notes;

    // required by Jackson so Spring can convert JSON into this DTO
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
