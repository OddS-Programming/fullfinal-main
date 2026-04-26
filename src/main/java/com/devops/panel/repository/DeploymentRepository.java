package com.devops.panel.repository;

import com.devops.panel.entity.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    Page<Deployment> findByProjectId(Long projectId, Pageable pageable);

    Page<Deployment> findByStatus(String status, Pageable pageable);

    List<Deployment> findByEnvironmentId(Long environmentId);

    Page<Deployment> findByProjectIdAndStatus(Long projectId, String status, Pageable pageable);

    Optional<Deployment> findByIdAndProjectId(Long id, Long projectId);
}
