package com.devops.panel.repository;

import com.devops.panel.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
