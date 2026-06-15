package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.user.User;

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

    // --- Project Mapper ---
    public static ProjectResponseDTO toProjectResponseDTO(Project project) {
        if (project == null)
            return null;
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
                project.getCreateBy());
    }

    public static List<ProjectResponseDTO> toProjectResponseDTOList(List<Project> projects) {
        if (projects == null)
            return null;
        return projects.stream().map(MapperUtils::toProjectResponseDTO).collect(Collectors.toList());
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
                tutorial.getCreateBy());
    }

    public static List<TutorialResponseDTO> toTutorialResponseDTOList(List<Tutorial> tutorials) {
        if (tutorials == null)
            return null;
        return tutorials.stream().map(MapperUtils::toTutorialResponseDTO).collect(Collectors.toList());
    }

    // --- Roadmap Mapper ---
    public static RoadmapResponseDTO toRoadmapResponseDTO(Roadmap roadmap) {
        if (roadmap == null)
            return null;
        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getSlug(),
                roadmap.getDescription(),
                roadmap.getDifficulty(),
                !roadmap.isDeleted(),
                roadmap.getCreatedAt(),
                roadmap.getCreateBy());
    }

    public static List<RoadmapResponseDTO> toRoadmapResponseDTOList(List<Roadmap> roadmaps) {
        if (roadmaps == null)
            return null;
        return roadmaps.stream().map(MapperUtils::toRoadmapResponseDTO).collect(Collectors.toList());
    }
}
