package com.devops.panel.service;

import com.devops.panel.dto.EnvironmentRequest;
import com.devops.panel.dto.EnvironmentResponse;
import com.devops.panel.entity.Environment;
import com.devops.panel.entity.Project;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.EnvironmentRepository;
import com.devops.panel.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);
    private final EnvironmentRepository environmentRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public Page<EnvironmentResponse> findByProjectId(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return environmentRepository.findByProjectId(projectId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EnvironmentResponse> listByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return environmentRepository.findByProjectId(projectId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnvironmentResponse getById(Long projectId, Long id) {
        Environment env = getEnvironmentByProject(projectId, id);
        return toResponse(env);
    }

    @Transactional
    public EnvironmentResponse create(Long projectId, EnvironmentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        if (environmentRepository.existsByNameAndProjectId(request.getName(), projectId)) {
            throw new BadRequestException("Environment with this name already exists in project");
        }
        Environment env = Environment.builder()
                .name(request.getName())
                .url(request.getUrl())
                .description(request.getDescription())
                .project(project)
                .build();
        env = environmentRepository.save(env);
        log.info("Environment created: id={}, name={}, projectId={}", env.getId(), env.getName(), projectId);
        return toResponse(env);
    }

    @Transactional
    public EnvironmentResponse update(Long projectId, Long id, EnvironmentRequest request) {
        Environment env = getEnvironmentByProject(projectId, id);
        if (!env.getName().equals(request.getName())
                && environmentRepository.existsByNameAndProjectId(request.getName(), projectId)) {
            throw new BadRequestException("Environment with this name already exists in project");
        }
        env.setName(request.getName());
        env.setUrl(request.getUrl());
        env.setDescription(request.getDescription());
        env = environmentRepository.save(env);
        log.info("Environment updated: id={}, name={}", env.getId(), env.getName());
        return toResponse(env);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        Environment env = getEnvironmentByProject(projectId, id);
        environmentRepository.delete(env);
        log.info("Environment deleted: id={}", id);
    }

    private Environment getEnvironmentByProject(Long projectId, Long id) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return environmentRepository.findByIdAndProjectId(id, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Environment not found: " + id + " in project " + projectId));
    }

    private EnvironmentResponse toResponse(Environment e) {
        return EnvironmentResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .url(e.getUrl())
                .description(e.getDescription())
                .projectId(e.getProject().getId())
                .build();
    }
}
