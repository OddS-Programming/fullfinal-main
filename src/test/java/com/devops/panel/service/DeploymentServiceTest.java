package com.devops.panel.service;

import com.devops.panel.dto.DeploymentRequest;
import com.devops.panel.dto.DeploymentResponse;
import com.devops.panel.entity.Deployment;
import com.devops.panel.entity.Environment;
import com.devops.panel.entity.Project;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.DeploymentRepository;
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
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private DeploymentService deploymentService;

    @Test
    void getById_WhenDeploymentBelongsToProject_ReturnsResponse() {
        Project project = Project.builder().id(1L).build();
        Environment environment = Environment.builder().id(2L).name("prod").project(project).build();
        Deployment deployment = Deployment.builder()
                .id(3L)
                .status("SUCCESS")
                .version("1.0.0")
                .project(project)
                .environment(environment)
                .build();

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(deploymentRepository.findByIdAndProjectId(3L, 1L)).thenReturn(Optional.of(deployment));

        DeploymentResponse result = deploymentService.getById(1L, 3L);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getEnvironmentName()).isEqualTo("prod");
    }

    @Test
    void getById_WhenProjectMissing_ThrowsResourceNotFoundException() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> deploymentService.getById(1L, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void update_WhenEnvironmentBelongsToAnotherProject_ThrowsResourceNotFoundException() {
        Project project = Project.builder().id(1L).build();
        Project anotherProject = Project.builder().id(9L).build();
        Environment currentEnvironment = Environment.builder().id(2L).name("prod").project(project).build();
        Environment foreignEnvironment = Environment.builder().id(5L).name("staging").project(anotherProject).build();
        Deployment deployment = Deployment.builder()
                .id(3L)
                .status("SUCCESS")
                .version("1.0.0")
                .project(project)
                .environment(currentEnvironment)
                .build();
        DeploymentRequest request = new DeploymentRequest();
        request.setStatus("FAILED");
        request.setVersion("1.0.1");
        request.setEnvironmentId(5L);

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(deploymentRepository.findByIdAndProjectId(3L, 1L)).thenReturn(Optional.of(deployment));
        when(environmentRepository.findById(5L)).thenReturn(Optional.of(foreignEnvironment));

        assertThatThrownBy(() -> deploymentService.update(1L, 3L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void delete_WhenDeploymentBelongsToProject_DeletesEntity() {
        Project project = Project.builder().id(1L).build();
        Environment environment = Environment.builder().id(2L).name("prod").project(project).build();
        Deployment deployment = Deployment.builder()
                .id(3L)
                .status("SUCCESS")
                .version("1.0.0")
                .project(project)
                .environment(environment)
                .build();

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(deploymentRepository.findByIdAndProjectId(3L, 1L)).thenReturn(Optional.of(deployment));

        deploymentService.delete(1L, 3L);

        verify(deploymentRepository).delete(deployment);
    }
}
