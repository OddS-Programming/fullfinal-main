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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findAll(String name, Pageable pageable) {
        Page<Project> page = name != null && !name.isBlank()
                ? projectRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                : projectRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findByOwner(String username, Pageable pageable) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return projectRepository.findByOwnerId(owner.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findPublic(String name, Pageable pageable) {
        Page<Project> page = name != null && !name.isBlank()
                ? projectRepository.findByPublicProjectTrueAndNameContainingIgnoreCase(name.trim(), pageable)
                : projectRepository.findByPublicProjectTrue(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (projectRepository.existsByNameAndOwnerId(request.getName(), owner.getId())) {
            throw new BadRequestException("Project with this name already exists");
        }
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .publicProject(Boolean.TRUE.equals(request.getPublicProject()))
                .owner(owner)
                .build();
        project = projectRepository.save(project);
        log.info("Project created: id={}, name={}, owner={}", project.getId(), project.getName(), username);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (!project.getOwner().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only owner can update project");
        }
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setPublicProject(Boolean.TRUE.equals(request.getPublicProject()));
        project = projectRepository.save(project);
        log.info("Project updated: id={}, name={}", project.getId(), project.getName());
        return toResponse(project);
    }

    @Transactional
    public void delete(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (!project.getOwner().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only owner can delete project");
        }
        projectRepository.delete(project);
        log.info("Project deleted: id={}", id);
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .publicProject(p.isPublicProject())
                .ownerId(p.getOwner().getId())
                .ownerUsername(p.getOwner().getUsername())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
