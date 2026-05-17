package com.devops.panel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GitRepositoryRequest {

    @Size(max = 500)
    private String repositoryUrl;
}
