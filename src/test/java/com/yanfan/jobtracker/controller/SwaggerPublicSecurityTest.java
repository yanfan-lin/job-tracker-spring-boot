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

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.swagger.public=true")
class SwaggerPublicSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void swaggerAccess_shouldFollowPublicConfiguration() throws Exception {

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/applications"))
                .andExpect(status().isUnauthorized());
    }

}
