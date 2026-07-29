package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.exception.DuplicateEmailException;
import com.yanfan.jobtracker.exception.InvalidCredentialsException;
import com.yanfan.jobtracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Test AuthController HTTP behavior with a mocked service
@WebMvcTest(AuthController.class)

// Disable security filters so these tests focus on validation and HTTP responses
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;


    // Verify valid registration returns 201 Created
    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                }
                """;

        AppUserResponse response = new AppUserResponse(
                1L,
                "person@example.com",
                LocalDateTime.of(2026, 7, 24, 18, 30)
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("person@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-24T18:30:00"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

    }

    // Verify invalid registration data returns 400 Bad Request
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must be at least 8 characters"));

        // Stop before calling AuthService when validation fails
        verifyNoInteractions(authService);

    }

    // Verify duplicate registration returns 409 Conflict
    @Test
    void register_shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                }
                """;

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException(
                        "Email is already registered"
                ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));

    }

    // Verify malformed registration JSON returns 400 Bad Request
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed JSON request body"));

        // Do not create RegisterRequest from malformed JSON
        verifyNoInteractions(authService);

    }

    // Verify surrounding email whitespace is removed before calling AuthService
    @Test
    void register_shouldTrimEmailBeforeCallingService() throws Exception {
        String request = """
                {
                    "email": "  Person@Example.COM  ",
                    "password": "password123"
                }
                """;

        AppUserResponse response = new AppUserResponse(
                1L,
                "person@example.com",
                LocalDateTime.of(2026, 7, 24, 19, 30)
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegisterRequest> requestCaptor =
                ArgumentCaptor.forClass(RegisterRequest.class);

        verify(authService).register(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getEmail())
                .isEqualTo("Person@Example.COM");

    }

    // Verify valid login returns a JWT response
    @Test
    void login_shouldReturnJwtResponse() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                }
                """;

        LoginResponse response = new LoginResponse(
                "signed-jwt-token",
                "Bearer",
                3600L
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("signed-jwt-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(3600));

    }

    // Verify invalid credentials return 401 Unauthorized
    @Test
    void login_shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "wrong-password"
                }
                """;

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException(
                        "Invalid email or password"
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));

    }

    // Verify invalid login data returns 400 Bad Request
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message")
                        .value("Request body validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password is required"));

        // Reject invalid input before calling AuthService
        verifyNoInteractions(authService);

    }

    // Verify malformed login JSON returns 400 Bad Request
    @Test
    void login_shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed JSON request body"));

        // Do not create LoginRequest from malformed JSON
        verifyNoInteractions(authService);

    }


}