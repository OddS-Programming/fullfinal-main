package com.devops.panel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeploymentRequest {

    @NotBlank(message = "Status is required")
    @Size(min = 1, max = 50)
    private String status;

    @NotBlank(message = "Version is required")
    @Size(min = 1, max = 100)
    private String version;

    @Size(max = 1000)
    private String description;

    private Long environmentId;
}
