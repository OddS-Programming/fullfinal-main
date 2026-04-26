package com.devops.panel.controller;

import com.devops.panel.dto.AuthResponse;
import com.devops.panel.security.JwtAuthFilter;
import com.devops.panel.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthService authService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_WithValidBody_Returns201() throws Exception {
        String body = "{\"username\":\"user1\",\"email\":\"u@mail.com\",\"password\":\"pass1234\"}";
        AuthResponse response = AuthResponse.builder().token("jwt").username("user1").role("USER").userId(1L).build();
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void login_WithValidBody_Returns200() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        AuthResponse response = AuthResponse.builder().token("jwt").username("admin").role("ADMIN").userId(1L).build();
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.token").value("jwt"));
    }
}
