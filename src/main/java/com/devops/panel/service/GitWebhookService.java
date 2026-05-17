package com.devops.panel.service;

import com.devops.panel.dto.GitWebhookEventResponse;
import com.devops.panel.entity.GitWebhookEvent;
import com.devops.panel.entity.Project;
import com.devops.panel.entity.Role;
import com.devops.panel.entity.User;
import com.devops.panel.exception.BadRequestException;
import com.devops.panel.exception.ResourceNotFoundException;
import com.devops.panel.repository.GitWebhookEventRepository;
import com.devops.panel.repository.ProjectRepository;
import com.devops.panel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GitWebhookService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GitWebhookEventRepository gitWebhookEventRepository;

    @Transactional
    public GitWebhookEventResponse receive(Long projectId, HttpHeaders headers, String payload) {
        Project project = getProject(projectId);
        if (payload == null || payload.isBlank()) {
            throw new BadRequestException("Webhook payload is empty");
        }

        GitWebhookEvent event = GitWebhookEvent.builder()
                .project(project)
                .provider(resolveProvider(headers))
                .eventType(resolveEventType(headers))
                .deliveryId(resolveDeliveryId(headers))
                .payload(payload)
                .build();
        return toResponse(gitWebhookEventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Page<GitWebhookEventResponse> findByProject(Long projectId, String username, Pageable pageable) {
        Project project = getProject(projectId);
        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        requireProjectAccess(project, actor);
        return gitWebhookEventRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private String resolveProvider(HttpHeaders headers) {
        if (headers.containsKey("X-GitHub-Event")) {
            return "github";
        }
        if (headers.containsKey("X-Gitlab-Event")) {
            return "gitlab";
        }
        return "git";
    }

    private String resolveEventType(HttpHeaders headers) {
        String githubEvent = headers.getFirst("X-GitHub-Event");
        if (githubEvent != null && !githubEvent.isBlank()) {
            return githubEvent;
        }
        String gitlabEvent = headers.getFirst("X-Gitlab-Event");
        if (gitlabEvent != null && !gitlabEvent.isBlank()) {
            return gitlabEvent;
        }
        return "unknown";
    }

    private String resolveDeliveryId(HttpHeaders headers) {
        String githubDelivery = headers.getFirst("X-GitHub-Delivery");
        if (githubDelivery != null && !githubDelivery.isBlank()) {
            return githubDelivery;
        }
        String gitlabDelivery = headers.getFirst("X-Gitlab-Event-UUID");
        return gitlabDelivery == null || gitlabDelivery.isBlank() ? null : gitlabDelivery;
    }

    private void requireProjectAccess(Project project, User actor) {
        boolean owner = project.getOwner().getId().equals(actor.getId());
        boolean member = project.getMembers().stream().anyMatch(memberUser -> memberUser.getId().equals(actor.getId()));
        if (!project.isPublicProject() && !owner && !member && actor.getRole() != Role.ADMIN) {
            throw new BadRequestException("No access to project webhook data");
        }
    }

    private GitWebhookEventResponse toResponse(GitWebhookEvent event) {
        return GitWebhookEventResponse.builder()
                .id(event.getId())
                .projectId(event.getProject().getId())
                .provider(event.getProvider())
                .eventType(event.getEventType())
                .deliveryId(event.getDeliveryId())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
