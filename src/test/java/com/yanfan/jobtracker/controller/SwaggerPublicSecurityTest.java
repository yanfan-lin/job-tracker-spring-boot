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

// Test locally enabled Swagger access without compromising API security
@WebMvcTest(AuthController.class)

// Load the real security rules for these endpoint tests
@Import(SecurityConfig.class)

// Enable public Swagger access for this test
@TestPropertySource(properties = "app.swagger.public=true")
class SwaggerPublicSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // Provide a mocked decoder so SecurityConfig can load in this MVC test
    @MockitoBean
    private JwtDecoder jwtDecoder;

    // Verify Swagger UI passes security when enabled locally
    @Test
    void swaggerUi_shouldPassSecurityWhenEnabled() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound());
    }

    // Verify OpenAPI documentation passes security when enabled locally
    @Test
    void apiDocs_shouldPassSecurityWhenEnabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    // Verify enabling Swagger does not expose application endpoints
    @Test
    void applications_shouldRemainProtectedWhenSwaggerIsPublic() throws Exception {
        mockMvc.perform(get("/applications"))
                .andExpect(status().isUnauthorized());
    }


}
