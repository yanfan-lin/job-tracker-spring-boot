package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @Test
    void create_shouldRejectInvalidRequest() throws Exception {

        String request = """
                {
                  "company": "%s",
                  "title": "",
                  "status": "random",
                  "dateApplied": null
                }
                """.formatted("A".repeat(256));

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.company")
                        .value("Company must not exceed 255 characters"))
                .andExpect(jsonPath("$.fieldErrors.title")
                        .value("Title is required"))
                .andExpect(jsonPath("$.fieldErrors.status")
                        .value("Status must be one of: applied, interview, offer, rejected"))
                .andExpect(jsonPath("$.fieldErrors.dateApplied")
                        .value("Date applied is required"));

        verifyNoInteractions(service);
    }

    @Test
    void patch_shouldRejectInvalidRequest() throws Exception {

        String request = """
                {
                  "company": "",
                  "title": "%s",
                  "status": "random"
                }
                """.formatted("T".repeat(256));

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.company")
                        .value("Company must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.title")
                        .value("Title must not exceed 255 characters"))
                .andExpect(jsonPath("$.fieldErrors.status")
                        .value("Status must be one of: applied, interview, offer, rejected"));

        verifyNoInteractions(service);
    }

}
