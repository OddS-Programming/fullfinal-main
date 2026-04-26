package com.devops.panel.controller;

import com.devops.panel.dto.ProjectResponse;
import com.devops.panel.security.JwtAuthFilter;
import com.devops.panel.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(roles = "USER")
    void getAll_Returns200() throws Exception {
        ProjectResponse pr = ProjectResponse.builder().id(1L).name("P1").ownerUsername("user").build();
        when(projectService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(pr)));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("P1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getById_WhenExists_Returns200() throws Exception {
        ProjectResponse pr = ProjectResponse.builder().id(1L).name("P1").ownerUsername("user").build();
        when(projectService.getById(1L)).thenReturn(pr);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("P1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_WithValidBody_Returns201() throws Exception {
        String body = "{\"name\":\"NewProject\",\"description\":\"Desc\"}";
        ProjectResponse created = ProjectResponse.builder().id(1L).name("NewProject").ownerUsername("user").build();
        when(projectService.create(any(), eq("user"))).thenReturn(created);

        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NewProject"));
    }
}
