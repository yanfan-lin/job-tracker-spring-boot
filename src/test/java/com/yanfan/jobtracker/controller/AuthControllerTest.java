package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
// Disable security filters so these tests cover validation and error responses.
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        String request = """
                {
                    "email": "blahblah",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must be at least 8 characters"));

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                }
                """;

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email is already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));

    }

    @Test
    void register_shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed JSON request body"));

        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        String request = """
                {
                    "email": "not-an-email",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message")
                        .value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password is required"));

        verifyNoInteractions(authService);
    }

}
