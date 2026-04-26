package com.devops.panel.controller;

import com.devops.panel.dto.EnvironmentResponse;
import com.devops.panel.security.JwtAuthFilter;
import com.devops.panel.service.EnvironmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvironmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EnvironmentService environmentService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(roles = "USER")
    void getByProject_Returns200() throws Exception {
        EnvironmentResponse er = EnvironmentResponse.builder().id(1L).name("prod").projectId(1L).build();
        when(environmentService.findByProjectId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(er)));

        mockMvc.perform(get("/api/projects/1/environments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("prod"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_WithValidBody_Returns201() throws Exception {
        String body = "{\"name\":\"staging\",\"url\":\"https://staging\"}";
        EnvironmentResponse created = EnvironmentResponse.builder().id(1L).name("staging").projectId(1L).build();
        when(environmentService.create(eq(1L), any())).thenReturn(created);

        mockMvc.perform(post("/api/projects/1/environments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("staging"));
    }
}
