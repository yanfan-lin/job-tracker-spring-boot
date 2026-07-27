package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.config.SecurityConfig;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// security test for JobApplicationController
@WebMvcTest(JobApplicationController.class)
@Import(SecurityConfig.class)
class JobApplicationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;


    // verifies that GET /applications is public
    @Test
    void getApplications_shouldBePublic() throws Exception {
        when(service.findAll(null, null, "date_applied", "desc", 10, 0))
                .thenReturn(List.<JobApplicationResponse>of());

        this.mockMvc.perform(get("/applications"))
                .andExpect(status().isOk());

    }

    // verifies that POST /applications requires authentication
    @Test
    void create_shouldRequireAuthentication() throws Exception {
        String request = """
                {
                  "company": "Amazon",
                  "title": "Backend Developer",
                  "status": "applied",
                  "dateApplied": "2026-07-06",
                  "notes": "Applied through LinkedIn"
                }
                """;

        this.mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

    }

    // verifies that PATCH /applications/{id} requires authentication
    @Test
    void patch_shouldRequireAuthentication() throws Exception {
        String request = """
                {
                  "status": "interview"
                }
                """;

        this.mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    // verifies that DELETE /applications/{id} requires authentication
    @Test
    void delete_shouldRequireAuthentication() throws Exception {
        this.mockMvc.perform(delete("/applications/1"))
                .andExpect(status().isUnauthorized());
    }

    // verifies that a valid JWT allows access to a protected endpoint
    @Test
    void create_shouldAllowRequestWithValidJwt() throws Exception {
        String request = """
                        {
                          "company": "Microsoft",
                          "title": "Software Engineer",
                          "status": "applied",
                          "dateApplied": "2026-07-06",
                          "notes": "Applied through LinkedIn"
                        }
                """;

        JobApplicationResponse response = new JobApplicationResponse(
                1L,
                "Microsoft",
                "Software Engineer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn",
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 10, 0)
        );

        when(service.create(
                eq(42L),
                any(JobApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/applications")
                        .with(jwt().jwt(token -> token
                                .subject("person@example.com")
                                .claim("userId", 42L)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.company").value("Microsoft"))
                .andExpect(jsonPath("$.title")
                        .value("Software Engineer"));

    }

    // verifies that an invalid JWT is rejected before the controller runs
    @Test
    void create_shouldRejectInvalidJwt() throws Exception {
        String request = """
                {
                  "company": "Microsoft",
                  "title": "Software Engineer",
                  "status": "applied",
                  "dateApplied": "2026-07-06",
                  "notes": "Applied through LinkedIn"
                }
                """;

        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid JWT"));

        mockMvc.perform(post("/applications")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        // JWT verification fails before reaching a controller
        verifyNoInteractions(service);

    }


}
