package com.devops.panel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentResponse {

    private Long id;
    private String name;
    private String url;
    private String description;
    private Long projectId;
}
