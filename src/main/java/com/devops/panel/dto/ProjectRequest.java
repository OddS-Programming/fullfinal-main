package com.devops.panel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 200)
    private String name;

    @Size(max = 1000)
    private String description;
}
