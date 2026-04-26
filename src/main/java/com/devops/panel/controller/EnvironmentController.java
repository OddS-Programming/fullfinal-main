package com.devops.panel.controller;

import com.devops.panel.dto.EnvironmentRequest;
import com.devops.panel.dto.EnvironmentResponse;
import com.devops.panel.service.EnvironmentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentController.class);
    private final EnvironmentService environmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<EnvironmentResponse>> getByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("Fetching environments for projectId={}", projectId);
        return ResponseEntity.ok(environmentService.findByProjectId(projectId, pageable));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EnvironmentResponse>> listByProject(@PathVariable Long projectId) {
        log.debug("Listing environments for projectId={}", projectId);
        return ResponseEntity.ok(environmentService.listByProjectId(projectId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EnvironmentResponse> getById(@PathVariable Long projectId, @PathVariable Long id) {
        log.debug("Fetching environment id={} for projectId={}", id, projectId);
        return ResponseEntity.ok(environmentService.getById(projectId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EnvironmentResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody EnvironmentRequest request
    ) {
        log.debug("Creating environment for projectId={}", projectId);
        EnvironmentResponse created = environmentService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EnvironmentResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody EnvironmentRequest request
    ) {
        log.debug("Updating environment id={} for projectId={}", id, projectId);
        return ResponseEntity.ok(environmentService.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        log.debug("Deleting environment id={} for projectId={}", id, projectId);
        environmentService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
