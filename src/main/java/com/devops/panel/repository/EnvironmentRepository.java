package com.devops.panel.repository;

import com.devops.panel.entity.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    List<Environment> findByProjectId(Long projectId);

    Page<Environment> findByProjectId(Long projectId, Pageable pageable);

    Optional<Environment> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByNameAndProjectId(String name, Long projectId);
}
