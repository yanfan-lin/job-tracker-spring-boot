package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// handle HTTP requests
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


}
