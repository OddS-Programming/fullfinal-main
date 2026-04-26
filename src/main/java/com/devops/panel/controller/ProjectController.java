package com.devops.panel.controller;

import com.devops.panel.dto.ProjectRequest;
import com.devops.panel.dto.ProjectResponse;
import com.devops.panel.service.ProjectService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ProjectResponse>> getAll(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("Fetching projects page={}, size={}, filter={}", pageable.getPageNumber(), pageable.getPageSize(), name);
        return ResponseEntity.ok(projectService.findAll(name, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ProjectResponse>> getMy(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("Fetching projects for user={}", user.getUsername());
        return ResponseEntity.ok(projectService.findByOwner(user.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        log.debug("Fetching project id={}", id);
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        log.debug("Creating project for user={}", user.getUsername());
        ProjectResponse created = projectService.create(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        log.debug("Updating project id={} for user={}", id, user.getUsername());
        return ResponseEntity.ok(projectService.update(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        log.debug("Deleting project id={} for user={}", id, user.getUsername());
        projectService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
