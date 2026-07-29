package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.config.SecurityConfig;
import com.yanfan.jobtracker.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test protected Swagger access when local exposure is disabled
@WebMvcTest(AuthController.class)

// Load the real security rules for these endpoint tests
@Import(SecurityConfig.class)

// Disable public Swagger access for this test context
@TestPropertySource(properties = "app.swagger.public=false")
class SwaggerProtectedSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;


    @Test
    void swaggerUi_shouldRequireAuthenticationWhenDisabled() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocs_shouldRequireAuthenticationWhenDisabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized());
    }


}
