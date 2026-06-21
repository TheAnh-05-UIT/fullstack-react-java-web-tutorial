package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.roadmap.RoadmapStep;
import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapStepResponseDTO;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;

public class MapperUtils {

    // --- User Mapper ---
    public static UserResponseDTO toUserResponseDTO(User user) {
        if (user == null)
            return null;
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static CreateUserResponseDTO toCreateUserResponseDTO(User user) {
        if (user == null)
            return null;
        return new CreateUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt(),
                user.getCreateBy());
    }

    public static UpdateUserResponseDTO toUpdateUserResponseDTO(User user) {
        if (user == null)
            return null;
        return new UpdateUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getUpdatedAt(), // Mapping từ updatedAt của Entity sang updateAt của DTO
                user.getUpdateBy());
    }

    public static List<UserResponseDTO> toUserResponseDTOList(List<User> users) {
        if (users == null)
            return null;
        return users.stream().map(MapperUtils::toUserResponseDTO).collect(Collectors.toList());
    }

    public static User toUser(CreateUserRequestDTO dto) {
        if (dto == null)
            return null;
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public static void updateUserFromDTO(UpdateUserRequestDTO dto, User user) {
        if (dto == null || user == null)
            return;
        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
    }

    // --- Project Mapper ---
    public static ProjectResponseDTO toProjectResponseDTO(Project project) {
        if (project == null)
            return null;
        ProjectResponseDTO responseDTO = new ProjectResponseDTO(
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
                toCategoryDTO(project.getCategory()),
                project.getDifficulty(),
                project.getStatus(),
                project.getTags(),
                project.getAuthor() != null ? project.getAuthor().getName() : project.getCreateBy());
        return responseDTO;
    }

    public static List<ProjectResponseDTO> toProjectResponseDTOList(List<Project> projects) {
        if (projects == null)
            return null;
        return projects.stream().map(MapperUtils::toProjectResponseDTO).collect(Collectors.toList());
    }

    public static Project toProject(CreateProjectRequestDTO dto) {
        if (dto == null)
            return null;
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
        if (dto == null || project == null)
            return;
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

    // --- Category Mapper ---
    public static CategoryDTO toCategoryDTO(Category category) {
        if (category == null)
            return null;
        return new CategoryDTO(category.getId(), category.getName(), category.getSlug());
    }

    // --- Tutorial Mapper ---
    public static TutorialResponseDTO toTutorialResponseDTO(Tutorial tutorial) {
        if (tutorial == null)
            return null;
        return new TutorialResponseDTO(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getSlug(),
                tutorial.getDescription(),
                tutorial.getCoverImage(),
                tutorial.getStatus(),
                tutorial.getViews(),
                tutorial.getCreatedAt(),
                tutorial.getCreateBy(),
                tutorial.getContent(),
                toCategoryDTO(tutorial.getCategory()),
                null);
    }

    public static List<TutorialResponseDTO> toTutorialResponseDTOList(List<Tutorial> tutorials) {
        if (tutorials == null)
            return null;
        return tutorials.stream().map(MapperUtils::toTutorialResponseDTO).collect(Collectors.toList());
    }

    public static Tutorial toTutorial(CreateTutorialRequestDTO dto) {
        if (dto == null)
            return null;
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(dto.getTitle());
        tutorial.setSlug(dto.getSlug());
        tutorial.setDescription(dto.getDescription());
        tutorial.setContent(dto.getContent());
        tutorial.setCoverImage(dto.getCoverImage());
        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            tutorial.setCategory(category);
        }
        return tutorial;
    }

    public static void updateTutorialFromDTO(UpdateTutorialRequestDTO dto, Tutorial tutorial) {
        if (dto == null || tutorial == null)
            return;
        tutorial.setTitle(dto.getTitle());
        tutorial.setSlug(dto.getSlug());
        tutorial.setDescription(dto.getDescription());
        tutorial.setContent(dto.getContent());
        tutorial.setCoverImage(dto.getCoverImage());
        tutorial.setStatus(dto.getStatus());
        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            tutorial.setCategory(category);
        }
    }

    // --- Roadmap Mapper ---
    public static RoadmapStepResponseDTO toRoadmapStepResponseDTO(RoadmapStep step) {
        if (step == null)
            return null;
        return new RoadmapStepResponseDTO(
                step.getId(),
                step.getTitle(),
                step.getDescription());
    }

    public static RoadmapResponseDTO toRoadmapResponseDTO(Roadmap roadmap) {
        if (roadmap == null)
            return null;

        List<RoadmapStepResponseDTO> steps = roadmap.getSteps() != null
                ? roadmap.getSteps().stream().map(MapperUtils::toRoadmapStepResponseDTO).collect(Collectors.toList())
                : null;

        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getSlug(),
                roadmap.getDescription(),
                roadmap.getDifficulty(),
                !roadmap.isDeleted(),
                roadmap.getCreatedAt(),
                roadmap.getCreateBy(),
                roadmap.getContent(),
                roadmap.getCoverImage(),
                roadmap.getIcon(),
                roadmap.getColor(),
                steps,
                null);
    }

    public static List<RoadmapResponseDTO> toRoadmapResponseDTOList(List<Roadmap> roadmaps) {
        if (roadmaps == null)
            return null;
        return roadmaps.stream().map(MapperUtils::toRoadmapResponseDTO).collect(Collectors.toList());
    }

    public static Roadmap toRoadmap(CreateRoadmapRequestDTO dto) {
        if (dto == null)
            return null;
        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(dto.getTitle());
        roadmap.setSlug(dto.getSlug());
        roadmap.setDescription(dto.getDescription());
        roadmap.setContent(dto.getContent());
        roadmap.setCoverImage(dto.getCoverImage());
        roadmap.setDifficulty(dto.getDifficulty());
        roadmap.setIcon(dto.getIcon());
        roadmap.setColor(dto.getColor());
        return roadmap;
    }

    public static void updateRoadmapFromDTO(UpdateRoadmapRequestDTO dto, Roadmap roadmap) {
        if (dto == null || roadmap == null)
            return;
        roadmap.setTitle(dto.getTitle());
        roadmap.setSlug(dto.getSlug());
        roadmap.setDescription(dto.getDescription());
        roadmap.setContent(dto.getContent());
        roadmap.setCoverImage(dto.getCoverImage());
        roadmap.setDifficulty(dto.getDifficulty());
        roadmap.setIcon(dto.getIcon());
        roadmap.setColor(dto.getColor());
    }
}
