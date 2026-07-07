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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// controller tests for JobApplicationController
// The service is mocked so that tests focus on
// HTTP status codes and JSON responses
@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    // verifies that GET /applications returns 200 ok
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

        when(service.findAll(null, null, "date_applied", "desc", 10, 0))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].company").value("Amazon"))
                .andExpect(jsonPath("$[0].title").value("Backend Developer"))
                .andExpect(jsonPath("$[0].status").value("applied"))
                .andExpect(jsonPath("$[0].dateApplied").value("2026-07-06"))
                .andExpect(jsonPath("$[0].notes").value("Applied through LinkedIn"));

    }

    // verifies that GET /applications/{id} returns 200 ok
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

        when(service.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.company").value("Amazon"))
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("applied"))
                .andExpect(jsonPath("$.dateApplied").value("2026-07-06"))
                .andExpect(jsonPath("$.notes").value("Applied through LinkedIn"));
    }

    // verifies that GET /applications/{id} returns 404 not found
    @Test
    void findById_shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        when(service.findById(999L))
                .thenThrow(new ResourceNotFoundException("Job application not found with id: 999"));

        mockMvc.perform(get("/applications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Job application not found with id: 999"));
    }

    // verifies that POST /applications returns 201 created
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

        when(service.create(any(JobApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.company").value("Amazon"))
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("applied"))
                .andExpect(jsonPath("$.dateApplied").value("2026-07-06"))
                .andExpect(jsonPath("$.notes").value("Applied through LinkedIn"));
    }

    // verifies that POST /applications returns 400 bad request
    // when required fields are blank or invalid
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.company").exists())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists())
                .andExpect(jsonPath("$.fieldErrors.dateApplied").exists());
    }

    // verifies that POST /applications returns 400 bad request
    // when the request body is malformed
    @Test
    void create_shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        String request = """
                {
                  "company": "Amazon",
                  "title": "Backend Developer",
                  "status": "applied",
                  "dateApplied": "2026-07-06",
                  "notes": "Missing closing brace"
                """;

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
    }

    // verifies that PATCH /applications/{id} returns 200 ok
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

        when(service.patch(any(Long.class), any(JobApplicationPatchRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.company").value("Amazon"))
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("interview"))
                .andExpect(jsonPath("$.dateApplied").value("2026-07-06"))
                .andExpect(jsonPath("$.notes").value("Recruiter screen scheduled"));
    }

    // verifies that PATCH /applications/{id} returns 400 bad request
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    // verifies that DELETE /applications/{id} returns 204 no content
    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/applications/1"))
                .andExpect(status().isNoContent());
    }

    // verifies that DELETE /applications/{id} returns 404 not found
    @Test
    void delete_shouldReturnNotFoundWhenApplicationDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Job application not found with id: 999"))
                .when(service).delete(999L);

        mockMvc.perform(delete("/applications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Job application not found with id: 999"));
    }


}
