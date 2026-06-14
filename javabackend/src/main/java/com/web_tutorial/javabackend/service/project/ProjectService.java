package com.web_tutorial.javabackend.service.project;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.project.Project;

public interface ProjectService {
    List<Project> getAllProjects();

    Optional<Project> getProjectById(Long id);

    Optional<Project> getProjectBySlug(String slug);

    Project createProject(Project project);

    Project updateProject(Long id, Project projectDetails);

    void deleteProject(Long id);
}
