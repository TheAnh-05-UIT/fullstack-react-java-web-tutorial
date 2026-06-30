package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.tutorial.Category;

public class ProjectMapper {

    public static ProjectResponseDTO toProjectResponseDTO(Project project) {
        if (project == null) {
            return null;
        }
        return new ProjectResponseDTO(
                project.getId(),
                project.getTitle(),
                project.getSlug(),
                project.getDescription(),
                project.getCoverImage(),
                project.getGithubUrl(),
                project.getDemoUrl(),
                project.getViews(),
                project.getCreatedAt(),
                project.getCreateBy(),
                project.getContent(),
                CategoryMapper.toCategoryDTO(project.getCategory()),
                project.getDifficulty(),
                project.getStatus(),
                project.getTags(),
                project.getAuthor() != null ? project.getAuthor().getName() : project.getCreateBy());
    }

    public static List<ProjectResponseDTO> toProjectResponseDTOList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream().map(ProjectMapper::toProjectResponseDTO).collect(Collectors.toList());
    }

    public static Project toProject(CreateProjectRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setSlug(dto.getSlug());
        project.setDescription(dto.getDescription());
        project.setContent(dto.getContent());
        project.setCoverImage(dto.getThumbnail());
        project.setGithubUrl(dto.getGithubUrl());
        project.setDemoUrl(dto.getDemoUrl());
        project.setDifficulty(dto.getDifficulty());
        project.setStatus(dto.getStatus());
        project.setTags(dto.getTechStack());

        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            project.setCategory(category);
        }
        return project;
    }

    public static void updateProjectFromDTO(UpdateProjectRequestDTO dto, Project project) {
        if (dto == null || project == null) {
            return;
        }
        project.setTitle(dto.getTitle());
        project.setSlug(dto.getSlug());
        project.setDescription(dto.getDescription());
        project.setContent(dto.getContent());
        project.setCoverImage(dto.getThumbnail());
        project.setGithubUrl(dto.getGithubUrl());
        project.setDemoUrl(dto.getDemoUrl());
        project.setDifficulty(dto.getDifficulty());
        project.setStatus(dto.getStatus());
        project.setTags(dto.getTechStack());

        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            project.setCategory(category);
        }
    }
}
