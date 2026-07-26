package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.config.SecurityConfig;
import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// security tests for authentication endpoints
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;


    // verifies that users can register without being authenticated
    @Test
    void register_shouldBePublic() throws Exception {
        String request = """
                {
                    "email": "person@example.com",
                    "password": "password123"
                }
                """;

        AppUserResponse response = new AppUserResponse(
                1L,
                "person@example.com",
                LocalDateTime.of(2026, 7, 24, 19, 0)
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("person@example.com"));

    }

    // verifies that users can log in without already being authenticated
    @Test
    void login_shouldBePublic() throws Exception {
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

        // authentication credentials are not included
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("signed-jwt-token"));
    }





}
