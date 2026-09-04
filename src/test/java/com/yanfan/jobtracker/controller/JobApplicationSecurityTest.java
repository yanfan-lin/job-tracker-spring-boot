package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.config.SecurityConfig;
import com.yanfan.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(JobApplicationController.class)
@Import(SecurityConfig.class)
class JobApplicationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getApplications_shouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/applications"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);

    }

    @Test
    void getApplications_shouldAllowRequestWithValidJwt() throws Exception {

        when(service.findAll(
                42L,
                null,
                null,
                "date_applied",
                "desc",
                10,
                0
        )).thenReturn(List.of());

        mockMvc.perform(get("/applications")
                        .with(jwt().jwt(token -> token
                                .subject("person@example.com")
                                .claim("userId", 42L)
                        )))
                .andExpect(status().isOk());

        verify(service).findAll(
                42L,
                null,
                null,
                "date_applied",
                "desc",
                10,
                0
        );

    }

    @Test
    void getApplications_shouldRejectInvalidJwt() throws Exception {

        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid JWT"));

        mockMvc.perform(get("/applications")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void applicationById_shouldRequireAuthentication() throws Exception {

        mockMvc.perform(delete("/applications/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

}
