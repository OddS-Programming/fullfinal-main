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
public class DeploymentResponse {

    private Long id;
    private String status;
    private String version;
    private String description;
    private Long projectId;
    private Long environmentId;
    private String environmentName;
    private Instant createdAt;
}
