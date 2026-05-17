package com.devops.panel.service;

import com.devops.panel.dto.GitBranchResponse;
import com.devops.panel.dto.GitCommitResponse;
import com.devops.panel.dto.GitRepositoryRequest;
import com.devops.panel.dto.ProjectResponse;
import com.devops.panel.entity.Project;
import com.devops.panel.entity.Role;
import com.devops.panel.entity.User;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.ProjectRepository;
import com.devops.panel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GitIntegrationService {

    private static final String HEADS_PREFIX = "refs/heads/";

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse updateRepository(Long projectId, GitRepositoryRequest request, String username) {
        Project project = getProject(projectId);
        User actor = getUser(username);
        requireOwnerOrAdmin(project, actor);
        project.setGitRepositoryUrl(cleanUrl(request.getRepositoryUrl()));
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GitBranchResponse> getBranches(Long projectId, String username) {
        Project project = getProject(projectId);
        requireProjectAccess(project, getUser(username));
        String repositoryUrl = requireRepositoryUrl(project);

        try {
            List<GitBranchResponse> branches = new ArrayList<>();
            for (Ref ref : Git.lsRemoteRepository()
                    .setRemote(repositoryUrl)
                    .setHeads(true)
                    .call()) {
                branches.add(GitBranchResponse.builder()
                        .name(ref.getName().replace(HEADS_PREFIX, ""))
                        .commitHash(ref.getObjectId().getName())
                        .build());
            }
            return branches;
        } catch (Exception ex) {
            throw new BadRequestException("Cannot read branches from repository");
        }
    }

    @Transactional(readOnly = true)
    public List<GitCommitResponse> getCommits(Long projectId, String branch, int limit, String username) {
        Project project = getProject(projectId);
        requireProjectAccess(project, getUser(username));
        String repositoryUrl = requireRepositoryUrl(project);
        String targetBranch = branch == null || branch.isBlank() ? null : branch.trim();
        int safeLimit = Math.max(1, Math.min(limit, 50));
        Path workDir = null;

        try {
            workDir = Files.createTempDirectory("devops-panel-git-");
            var clone = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(workDir.toFile())
                    .setCloneAllBranches(false);
            if (targetBranch != null) {
                clone.setBranch(targetBranch);
            }
            Git git = clone.call();
            try (git) {
                List<GitCommitResponse> commits = new ArrayList<>();
                Iterable<RevCommit> log = git.log().setMaxCount(safeLimit).call();
                for (RevCommit commit : log) {
                    commits.add(GitCommitResponse.builder()
                            .hash(commit.getName())
                            .shortHash(commit.getName().substring(0, 7))
                            .message(commit.getShortMessage())
                            .authorName(commit.getAuthorIdent().getName())
                            .authorEmail(commit.getAuthorIdent().getEmailAddress())
                            .committedAt(Instant.ofEpochSecond(commit.getCommitTime()))
                            .build());
                }
                return commits;
            }
        } catch (Exception ex) {
            throw new BadRequestException("Cannot read commits from repository");
        } finally {
            deleteDirectory(workDir);
        }
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private void requireOwnerOrAdmin(Project project, User actor) {
        if (!project.getOwner().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only owner can change git repository");
        }
    }

    private void requireProjectAccess(Project project, User actor) {
        boolean owner = project.getOwner().getId().equals(actor.getId());
        boolean member = project.getMembers().stream().anyMatch(memberUser -> memberUser.getId().equals(actor.getId()));
        if (!project.isPublicProject() && !owner && !member && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("No access to project git data");
        }
    }

    private String requireRepositoryUrl(Project project) {
        if (project.getGitRepositoryUrl() == null || project.getGitRepositoryUrl().isBlank()) {
            throw new BadRequestException("Git repository is not configured");
        }
        return project.getGitRepositoryUrl();
    }

    private String cleanUrl(String url) {
        return url == null || url.isBlank() ? null : url.trim();
    }

    private ProjectResponse toProjectResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .publicProject(p.isPublicProject())
                .gitRepositoryUrl(p.getGitRepositoryUrl())
                .ownerId(p.getOwner().getId())
                .ownerUsername(p.getOwner().getUsername())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private void deleteDirectory(Path dir) {
        if (dir == null) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
