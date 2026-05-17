package com.devops.panel.controller;

import com.devops.panel.dto.GitBranchResponse;
import com.devops.panel.dto.GitCommitResponse;
import com.devops.panel.dto.GitRepositoryRequest;
import com.devops.panel.dto.ProjectResponse;
import com.devops.panel.service.GitIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/git")
@RequiredArgsConstructor
public class GitIntegrationController {

    private final GitIntegrationService gitIntegrationService;

    @PutMapping("/repository")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> updateRepository(
            @PathVariable Long projectId,
            @Valid @RequestBody GitRepositoryRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(gitIntegrationService.updateRepository(projectId, request, user.getUsername()));
    }

    @GetMapping("/branches")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<GitBranchResponse>> getBranches(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(gitIntegrationService.getBranches(projectId, user.getUsername()));
    }

    @GetMapping("/commits")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<GitCommitResponse>> getCommits(
            @PathVariable Long projectId,
            @RequestParam(required = false) String branch,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(gitIntegrationService.getCommits(projectId, branch, limit, user.getUsername()));
    }
}
