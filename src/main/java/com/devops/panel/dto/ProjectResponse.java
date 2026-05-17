package com.devops.panel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private boolean publicProject;
    private String gitRepositoryUrl;
    private Long ownerId;
    private String ownerUsername;
    private Instant createdAt;
}
