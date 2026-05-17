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
public class GitCommitResponse {

    private String hash;
    private String shortHash;
    private String message;
    private String authorName;
    private String authorEmail;
    private Instant committedAt;
}
