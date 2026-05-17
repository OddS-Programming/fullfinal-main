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
public class GitWebhookEventResponse {

    private Long id;
    private Long projectId;
    private String provider;
    private String eventType;
    private String deliveryId;
    private Instant createdAt;
}
