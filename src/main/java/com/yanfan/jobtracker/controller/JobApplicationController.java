package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// handles HTTP requests
@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    // Constructor injection
    @Autowired
    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    // GET /applications
    // returns job application with optional filtering, search, sorting and pagination
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(name = "sort_by", defaultValue = "date_applied") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        List<JobApplicationResponse> applications = service.findAll(status, search, sortBy, order, limit, offset);

        return ResponseEntity.ok(applications);
    }

    // GET /applications/{id}
    // returns one job application by id
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(@PathVariable Long id) {
        JobApplicationResponse theApplication = service.findById(id);

        return ResponseEntity.ok(theApplication);
    }

    // POST /applications
    // creates a new job application record
    // request body validation handled by @Valid and the DTO validation
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @Valid @RequestBody JobApplicationRequest request
    ) {
        JobApplicationResponse theApplication = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(theApplication);
    }

    // PATCH /applications/{id}
    // partially updates an existing job application
    // all fields are optional
    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> patch(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationPatchRequest request
    ) {
        JobApplicationResponse updatedApplication = service.patch(id, request);

        return ResponseEntity.ok(updatedApplication);
    }

    // DELETE /applications/{id}
    // deletes a job application by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }


}
