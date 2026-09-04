package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.exception.ResourceNotFoundException;
import com.yanfan.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Test JobApplicationController HTTP behavior with a mocked service
@WebMvcTest(JobApplicationController.class)
// Disable security filters so tests focus on validation and responses
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @Test
    void findAll_shouldReturnApplications() throws Exception {

        JobApplicationResponse response = new JobApplicationResponse(
                1L,
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn",
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 10, 0)

        );

        when(service.findAll(
                42L,
                null,
                null,
                "date_applied",
                "desc",
                10,
                0
        ))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/applications")
                        .principal(createAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].company").value("Amazon"))
                .andExpect(jsonPath("$[0].title").value("Backend Developer"))
                .andExpect(jsonPath("$[0].status").value("applied"))
                .andExpect(jsonPath("$[0].dateApplied").value("2026-07-06"))
                .andExpect(jsonPath("$[0].notes").value("Applied through LinkedIn"));
    }

    @Test
    void findById_shouldReturnApplicationWhenFound() throws Exception {

        JobApplicationResponse response = new JobApplicationResponse(
                1L,
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn",
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 10, 0)
        );

        when(service.findById(42L, 1L))
                .thenReturn(response);

        mockMvc.perform(get("/applications/1")
                        .principal(createAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.company")
                        .value("Amazon"))
                .andExpect(jsonPath("$.title")
                        .value("Backend Developer"))
                .andExpect(jsonPath("$.status")
                        .value("applied"))
                .andExpect(jsonPath("$.dateApplied")
                        .value("2026-07-06"))
                .andExpect(jsonPath("$.notes")
                        .value("Applied through LinkedIn"));
    }

    @Test
    void findById_shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {

        when(service.findById(42L, 999L))
                .thenThrow(new ResourceNotFoundException("Job application not found with id: 999"));

        mockMvc.perform(get("/applications/999")
                        .principal(createAuthentication()))
                .andExpect(status()
                        .isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Job application not found with id: 999"));
    }

    @Test
    void create_shouldReturnCreatedApplication() throws Exception {

        String request = """
                {
                  "company": "Amazon",
                  "title": "Backend Developer",
                  "status": "applied",
                  "dateApplied": "2026-07-06",
                  "notes": "Applied through LinkedIn"
                }
                """;

        JobApplicationResponse response = new JobApplicationResponse(
                1L,
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn",
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 10, 0)
        );

        when(service.create(
                eq(42L),
                any(JobApplicationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/applications")
                        .principal(createAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.company")
                        .value("Amazon"))
                .andExpect(jsonPath("$.title")
                        .value("Backend Developer"))
                .andExpect(jsonPath("$.status")
                        .value("applied"))
                .andExpect(jsonPath("$.dateApplied")
                        .value("2026-07-06"))
                .andExpect(jsonPath("$.notes")
                        .value("Applied through LinkedIn"));
    }

    @Test
    void create_shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {

        String request = """
                {
                  "company": "",
                  "title": "",
                  "status": "random",
                  "dateApplied": null,
                  "notes": "Invalid test input"
                }
                """;

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.company").exists())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists())
                .andExpect(jsonPath("$.fieldErrors.dateApplied").exists());
    }

    @Test
    void create_shouldReturnBadRequestWhenCompanyExceedsMaximumLength() throws Exception {

        String overlongCompany = "A".repeat(256);

        String request = """
                {
                  "company": "%s",
                  "title": "Backend Developer",
                  "status": "applied",
                  "dateApplied": "2026-07-06"
                }
                """.formatted(overlongCompany);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.company")
                        .value("Company must not exceed 255 characters"));

        verifyNoInteractions(service);
    }

    @Test
    void patch_shouldReturnUpdatedApplication() throws Exception {
        String request = """
                {
                  "status": "interview",
                  "notes": "Recruiter screen scheduled"
                }
                """;

        JobApplicationResponse response = new JobApplicationResponse(
                1L,
                "Amazon",
                "Backend Developer",
                "interview",
                LocalDate.of(2026, 7, 6),
                "Recruiter screen scheduled",
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 11, 0)
        );

        when(service.patch(
                eq(42L),
                eq(1L),
                any(JobApplicationPatchRequest.class)
        ))
                .thenReturn(response);

        mockMvc.perform(patch("/applications/1")
                        .principal(createAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.company")
                        .value("Amazon"))
                .andExpect(jsonPath("$.title")
                        .value("Backend Developer"))
                .andExpect(jsonPath("$.status")
                        .value("interview"))
                .andExpect(jsonPath("$.dateApplied")
                        .value("2026-07-06"))
                .andExpect(jsonPath("$.notes")
                        .value("Recruiter screen scheduled"));
    }

    @Test
    void patch_shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        String request = """
                {
                  "status": "random"
                }
                """;

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status()
                        .isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Error"))
                .andExpect(jsonPath("$.message")
                        .value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status")
                        .exists());
    }

    @Test
    void patch_shouldReturnBadRequestWhenCompanyAndTitleAreBlank() throws Exception {

        String request = """
                {
                  "company": "",
                  "title": "   "
                }
                """;

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.company")
                        .value("Company must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.title")
                        .value("Title must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void patch_shouldReturnBadRequestWhenTitleExceedsMaximumLength() throws Exception {
        String overlongTitle = "T".repeat(256);

        String request = """
                {
                  "title": "%s"
                }
                """.formatted(overlongTitle);

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status()
                        .isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors.title")
                        .value("Title must not exceed 255 characters"));

        verifyNoInteractions(service);
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/applications/1")
                        .principal(createAuthentication()))
                .andExpect(status().isNoContent());

        verify(service).delete(42L, 1L);
    }

    // Create JWT authentication with the expected userId claim
    private JwtAuthenticationToken createAuthentication() {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("person@example.com")
                .claim("userId", 42L)
                .build();

        return new JwtAuthenticationToken(jwt);
    }

}
