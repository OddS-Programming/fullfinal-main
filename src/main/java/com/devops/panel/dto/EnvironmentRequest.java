package com.devops.panel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnvironmentRequest {

    @NotBlank(message = "Environment name is required")
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 500)
    private String url;

    @Size(max = 1000)
    private String description;
}
