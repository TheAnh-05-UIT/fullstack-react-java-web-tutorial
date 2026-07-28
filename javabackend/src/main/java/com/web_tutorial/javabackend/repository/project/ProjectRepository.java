package com.web_tutorial.javabackend.repository.project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findBySlug(String slug);

    java.util.List<Project> findBySlugInAndIsDeletedFalseAndStatus(java.util.Collection<String> slugs, ProjectStatus status);
    Optional<Project> findBySlugAndStatusAndIsDeletedFalse(String slug, ProjectStatus status);
    Optional<Project> findByIdAndStatusAndIsDeletedFalse(Long id, ProjectStatus status);
    boolean existsBySlugAndStatusAndIsDeletedFalse(String slug, ProjectStatus status);

    boolean existsBySlug(String slug);

    java.util.List<Project> findAllByOrderByIdDesc();
    org.springframework.data.domain.Page<Project> findAllByOrderByIdDesc(org.springframework.data.domain.Pageable pageable);
    java.util.List<Project> findByStatusAndIsDeletedFalseOrderByIdDesc(ProjectStatus status);
    org.springframework.data.domain.Page<Project> findByStatusAndIsDeletedFalseOrderByIdDesc(
            ProjectStatus status,
            org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("UPDATE Project p SET p.views = COALESCE(p.views, 0) + 1 WHERE p.id = :id")
    void incrementViews(@Param("id") Long id);
}
