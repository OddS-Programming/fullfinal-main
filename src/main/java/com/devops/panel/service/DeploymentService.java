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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    private final DeploymentRepository deploymentRepository;
    private final ProjectRepository projectRepository;
    private final EnvironmentRepository environmentRepository;

    @Transactional(readOnly = true)
    public Page<DeploymentResponse> findByProjectId(Long projectId, String status, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        Page<Deployment> page = status != null && !status.isBlank()
                ? deploymentRepository.findByProjectIdAndStatus(projectId, status.trim(), pageable)
                : deploymentRepository.findByProjectId(projectId, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DeploymentResponse getById(Long projectId, Long id) {
        Deployment d = getDeploymentByProject(projectId, id);
        return toResponse(d);
    }

    @Transactional
    public DeploymentResponse create(Long projectId, DeploymentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Long envId = request.getEnvironmentId() != null ? request.getEnvironmentId() : null;
        Environment environment = null;
        if (envId != null) {
            environment = environmentRepository.findById(envId)
                    .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + envId));
            if (!environment.getProject().getId().equals(projectId)) {
                throw new ResourceNotFoundException("Environment does not belong to this project");
            }
        }
        if (environment == null) {
            throw new ResourceNotFoundException("Environment is required for deployment");
        }
        Deployment d = Deployment.builder()
                .status(request.getStatus())
                .version(request.getVersion())
                .description(request.getDescription())
                .project(project)
                .environment(environment)
                .build();
        d = deploymentRepository.save(d);
        log.info("Deployment created: id={}, projectId={}, status={}", d.getId(), projectId, d.getStatus());
        return toResponse(d);
    }

    @Transactional
    public DeploymentResponse update(Long projectId, Long id, DeploymentRequest request) {
        Deployment d = getDeploymentByProject(projectId, id);
        d.setStatus(request.getStatus());
        d.setVersion(request.getVersion());
        d.setDescription(request.getDescription());
        if (request.getEnvironmentId() != null) {
            Environment env = environmentRepository.findById(request.getEnvironmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + request.getEnvironmentId()));
            if (!env.getProject().getId().equals(d.getProject().getId())) {
                throw new ResourceNotFoundException("Environment does not belong to this project");
            }
            d.setEnvironment(env);
        }
        d = deploymentRepository.save(d);
        log.info("Deployment updated: id={}", d.getId());
        return toResponse(d);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        Deployment deployment = getDeploymentByProject(projectId, id);
        deploymentRepository.delete(deployment);
        log.info("Deployment deleted: id={}", id);
    }

    private Deployment getDeploymentByProject(Long projectId, Long id) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return deploymentRepository.findByIdAndProjectId(id, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deployment not found: " + id + " in project " + projectId));
    }

    private DeploymentResponse toResponse(Deployment d) {
        return DeploymentResponse.builder()
                .id(d.getId())
                .status(d.getStatus())
                .version(d.getVersion())
                .description(d.getDescription())
                .projectId(d.getProject().getId())
                .environmentId(d.getEnvironment().getId())
                .environmentName(d.getEnvironment().getName())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
