package com.devops.panel.service;

import com.devops.panel.dto.ProjectRequest;
import com.devops.panel.dto.ProjectResponse;
import com.devops.panel.entity.Project;
import com.devops.panel.entity.Role;
import com.devops.panel.entity.User;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.ProjectRepository;
import com.devops.panel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ProjectService projectService;

    @Test
    void getById_WhenExists_ReturnsResponse() {
        User owner = User.builder().id(1L).username("user1").build();
        Project project = Project.builder().id(1L).name("P1").description("D1").owner(owner).build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("P1");
        assertThat(result.getOwnerUsername()).isEqualTo("user1");
    }

    @Test
    void getById_WhenNotExists_ThrowsResourceNotFoundException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void create_WhenValid_CreatesAndReturnsResponse() {
        User owner = User.builder().id(1L).username("user1").build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(owner));
        when(projectRepository.existsByNameAndOwnerId("New", 1L)).thenReturn(false);
        Project saved = Project.builder().id(1L).name("New").description("Desc").owner(owner).build();
        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        ProjectRequest req = new ProjectRequest();
        req.setName("New");
        req.setDescription("Desc");
        ProjectResponse result = projectService.create(req, "user1");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_WhenDuplicateName_ThrowsBadRequestException() {
        User owner = User.builder().id(1L).username("user1").build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(owner));
        when(projectRepository.existsByNameAndOwnerId("Existing", 1L)).thenReturn(true);

        ProjectRequest req = new ProjectRequest();
        req.setName("Existing");
        assertThatThrownBy(() -> projectService.create(req, "user1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void findAll_ReturnsPagedResults() {
        User owner = User.builder().id(1L).username("u").build();
        Page<Project> page = new PageImpl<>(List.of(
                Project.builder().id(1L).name("A").owner(owner).build()
        ));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProjectResponse> result = projectService.findAll(null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("A");
    }

    @Test
    void update_WhenAdmin_UpdatesForeignProject() {
        User owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        User admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        Project project = Project.builder().id(10L).name("Old").description("Desc").owner(owner).build();
        Project updated = Project.builder().id(10L).name("New").description("Updated").owner(owner).build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(projectRepository.save(any(Project.class))).thenReturn(updated);

        ProjectRequest request = new ProjectRequest();
        request.setName("New");
        request.setDescription("Updated");

        ProjectResponse result = projectService.update(10L, request, "admin");

        assertThat(result.getName()).isEqualTo("New");
        verify(projectRepository).save(project);
    }

    @Test
    void delete_WhenAdmin_DeletesForeignProject() {
        User owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        User admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        Project project = Project.builder().id(10L).name("Old").owner(owner).build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        projectService.delete(10L, "admin");

        verify(projectRepository).delete(project);
    }
}
