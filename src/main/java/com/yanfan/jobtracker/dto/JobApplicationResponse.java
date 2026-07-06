package com.yanfan.jobtracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// response DTO used when returning job application data back to the clients
public class JobApplicationResponse {

    private Long id;
    private String company;
    private String title;
    private String status;
    private LocalDate dateApplied;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public JobApplicationResponse(){
    }

    public JobApplicationResponse(Long id, String company, String title, String status, LocalDate dateApplied, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.title = title;
        this.status = status;
        this.dateApplied = dateApplied;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
