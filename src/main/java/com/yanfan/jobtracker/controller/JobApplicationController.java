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

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> findAll(
            JwtAuthenticationToken authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(name = "sort_by", defaultValue = "date_applied") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page)
    {

        return ResponseEntity.ok(service.findAll(
                extractUserId(authentication),
                status,
                search,
                sortBy,
                order,
                limit,
                page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> findById(
            JwtAuthenticationToken authentication,
            @PathVariable Long id) {

        return ResponseEntity.ok(service.findById(extractUserId(authentication), id));
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            JwtAuthenticationToken authentication,
            @Valid @RequestBody JobApplicationRequest request)
    {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(extractUserId(authentication), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> patch(
            JwtAuthenticationToken authentication,
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationPatchRequest request)
    {

        return ResponseEntity.ok(service.patch(extractUserId(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            JwtAuthenticationToken authentication,
            @PathVariable Long id)
    {

        service.delete(extractUserId(authentication), id);

        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(JwtAuthenticationToken authentication) {

        Number userIdClaim = authentication.getToken().getClaim("userId");

        return userIdClaim.longValue();
    }

}
