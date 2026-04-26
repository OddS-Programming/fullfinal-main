package com.devops.panel.controller;

import com.devops.panel.dto.DeploymentRequest;
import com.devops.panel.dto.DeploymentResponse;
import com.devops.panel.service.DeploymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);
    private final DeploymentService deploymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<DeploymentResponse>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("Fetching deployments for projectId={}, status={}", projectId, status);
        return ResponseEntity.ok(deploymentService.findByProjectId(projectId, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DeploymentResponse> getById(@PathVariable Long projectId, @PathVariable Long id) {
        log.debug("Fetching deployment id={} for projectId={}", id, projectId);
        return ResponseEntity.ok(deploymentService.getById(projectId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DeploymentResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody DeploymentRequest request
    ) {
        log.debug("Creating deployment for projectId={}", projectId);
        DeploymentResponse created = deploymentService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DeploymentResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody DeploymentRequest request
    ) {
        log.debug("Updating deployment id={} for projectId={}", id, projectId);
        return ResponseEntity.ok(deploymentService.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        log.debug("Deleting deployment id={} for projectId={}", id, projectId);
        deploymentService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
