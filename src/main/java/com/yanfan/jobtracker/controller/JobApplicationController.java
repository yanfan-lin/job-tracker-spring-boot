package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handle authenticated job application requests
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    // Return the authenticated user's applications with optional filters and pagination
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

    // Return one application only when it belongs to the authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(
            JwtAuthenticationToken authentication,
            @PathVariable Long id

    ) {
        Long userId = extractUserId(authentication);

        JobApplicationResponse theApplication = service.findById(userId, id);

        return ResponseEntity.ok(theApplication);

    }

    // Create a new application for the authenticated user
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody JobApplicationRequest request
    ) {
        Long userId = extractUserId(authentication);

        JobApplicationResponse theApplication = service.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(theApplication);

    }

    // Update only the provided fields of an application owned by the user
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

    // Delete an application only when it belongs to the authenticated user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            JwtAuthenticationToken authentication,
            @PathVariable Long id
    ) {
        Long userId = extractUserId(authentication);

        service.delete(userId, id);

        return ResponseEntity.noContent().build();

    }

    // Extract the database user ID stored in the validated JWT
    private Long extractUserId(JwtAuthenticationToken authentication) {
        Number userIdClaim = authentication.getToken().getClaim("userId");

        return userIdClaim.longValue();
    }


}
