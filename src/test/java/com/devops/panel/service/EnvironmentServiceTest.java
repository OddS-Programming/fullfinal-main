package com.devops.panel.service;

import com.devops.panel.dto.EnvironmentRequest;
import com.devops.panel.dto.EnvironmentResponse;
import com.devops.panel.entity.Environment;
import com.devops.panel.entity.Project;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.EnvironmentRepository;
import com.devops.panel.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentServiceTest {

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private EnvironmentService environmentService;

    @Test
    void getById_WhenEnvironmentBelongsToProject_ReturnsResponse() {
        Project project = Project.builder().id(1L).build();
        Environment environment = Environment.builder()
                .id(2L)
                .name("prod")
                .url("https://prod.example.com")
                .project(project)
                .build();

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(environmentRepository.findByIdAndProjectId(2L, 1L)).thenReturn(Optional.of(environment));

        EnvironmentResponse result = environmentService.getById(1L, 2L);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("prod");
        assertThat(result.getProjectId()).isEqualTo(1L);
    }

    @Test
    void getById_WhenProjectMissing_ThrowsResourceNotFoundException() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> environmentService.getById(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void update_WhenDuplicateNameInProject_ThrowsBadRequestException() {
        Project project = Project.builder().id(1L).build();
        Environment environment = Environment.builder()
                .id(2L)
                .name("prod")
                .url("https://prod.example.com")
                .project(project)
                .build();
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("staging");
        request.setUrl("https://staging.example.com");

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(environmentRepository.findByIdAndProjectId(2L, 1L)).thenReturn(Optional.of(environment));
        when(environmentRepository.existsByNameAndProjectId("staging", 1L)).thenReturn(true);

        assertThatThrownBy(() -> environmentService.update(1L, 2L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void delete_WhenEnvironmentBelongsToProject_DeletesEntity() {
        Project project = Project.builder().id(1L).build();
        Environment environment = Environment.builder()
                .id(2L)
                .name("prod")
                .project(project)
                .build();

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(environmentRepository.findByIdAndProjectId(2L, 1L)).thenReturn(Optional.of(environment));

        environmentService.delete(1L, 2L);

        verify(environmentRepository).delete(environment);
    }
}
