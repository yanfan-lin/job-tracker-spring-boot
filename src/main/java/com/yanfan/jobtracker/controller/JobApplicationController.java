package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    // returns job application owned by the authenticated user,
    // with optional filtering, search, sorting and pagination
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(name = "sort_by", defaultValue = "date_applied") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long userId = extractUserId(authentication);

        List<JobApplicationResponse> applications = service.findAll(
                userId,
                status,
                search,
                sortBy,
                order,
                limit,
                page
        );

        return ResponseEntity.ok(applications);

    }

    // GET /applications/{id}
    // returns one job application by id
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(
            JwtAuthenticationToken authentication,
            @PathVariable Long id

    ) {
        Long userId = extractUserId(authentication);

        JobApplicationResponse theApplication = service.findById(userId, id);

        return ResponseEntity.ok(theApplication);

    }

    // POST /applications
    // creates a new job application record
    // request body validation handled by @Valid and the DTO validation
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody JobApplicationRequest request
    ) {
        Long userId = extractUserId(authentication);

        JobApplicationResponse theApplication = service.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(theApplication);

    }

    // PATCH /applications/{id}
    // partially updates an application owned by the authenticated user
    // all fields are optional
    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> patch(
            JwtAuthenticationToken authentication,
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationPatchRequest request
    ) {
        Long userId = extractUserId(authentication);

        JobApplicationResponse updatedApplication = service.patch(userId, id, request);

        return ResponseEntity.ok(updatedApplication);
    }

    // DELETE /applications/{id}
    // deletes a job application owned by the authenticated user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            JwtAuthenticationToken authentication,
            @PathVariable Long id
    ) {
        Long userId = extractUserId(authentication);

        service.delete(userId, id);

        return ResponseEntity.noContent().build();

    }

    // helper to extract the database user id stored in the JWT
    private Long extractUserId(JwtAuthenticationToken authentication) {
        Number userIdClaim = authentication.getToken().getClaim("userId");

        return userIdClaim.longValue();
    }


}
