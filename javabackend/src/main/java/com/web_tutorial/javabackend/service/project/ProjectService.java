package com.web_tutorial.javabackend.service.project;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;

import org.springframework.data.domain.Pageable;

public interface ProjectService {
    List<Project> getAllProjects();
    ResultPaginationDTO getAllProjects(Pageable pageable);
    String getAuthorNameByEmail(String email);

    Optional<Project> getProjectById(Long id);

    Optional<Project> getProjectBySlug(String slug);

    Project createProject(Project project);

    Project updateProject(Long id, Project projectDetails);

    void deleteProject(Long id);
    void incrementViewCount(Long id);

    // DTO response methods cho Phase 3
    ProjectResponseDTO getProjectResponseById(Long id);

    ProjectResponseDTO getProjectResponseBySlug(String slug);

    ProjectResponseDTO createProjectFromDTO(CreateProjectRequestDTO requestDTO);

    ProjectResponseDTO updateProjectFromDTO(Long id, UpdateProjectRequestDTO requestDTO);
}
