package com.devops.panel.repository;

import com.devops.panel.entity.GitWebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitWebhookEventRepository extends JpaRepository<GitWebhookEvent, Long> {

    Page<GitWebhookEvent> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}
