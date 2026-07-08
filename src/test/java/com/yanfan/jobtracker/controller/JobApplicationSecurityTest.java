package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.config.SecurityConfig;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// security test for JobApplicationController
@WebMvcTest(JobApplicationController.class)
@Import(SecurityConfig.class)
class JobApplicationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

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


}
