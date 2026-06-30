package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.springframework.data.domain.Page;

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
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapStepResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.roadmap.RoadmapStep;
import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.user.User;

/**
 * Facade MapperUtils delegating specific domain mappings to dedicated mappers.
 */
public class MapperUtils {

    public static <T, R> ResultPaginationDTO toResultPaginationDTO(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).collect(Collectors.toList());
        return new ResultPaginationDTO(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // --- User Mapper ---
    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserMapper.toUserResponseDTO(user);
    }

    public static CreateUserResponseDTO toCreateUserResponseDTO(User user) {
        return UserMapper.toCreateUserResponseDTO(user);
    }

    public static UpdateUserResponseDTO toUpdateUserResponseDTO(User user) {
        return UserMapper.toUpdateUserResponseDTO(user);
    }

    public static List<UserResponseDTO> toUserResponseDTOList(List<User> users) {
        return UserMapper.toUserResponseDTOList(users);
    }

    public static User toUser(CreateUserRequestDTO dto) {
        return UserMapper.toUser(dto);
    }

    public static void updateUserFromDTO(UpdateUserRequestDTO dto, User user) {
        UserMapper.updateUserFromDTO(dto, user);
    }

    // --- Project Mapper ---
    public static ProjectResponseDTO toProjectResponseDTO(Project project) {
        return ProjectMapper.toProjectResponseDTO(project);
    }

    public static List<ProjectResponseDTO> toProjectResponseDTOList(List<Project> projects) {
        return ProjectMapper.toProjectResponseDTOList(projects);
    }

    public static Project toProject(CreateProjectRequestDTO dto) {
        return ProjectMapper.toProject(dto);
    }

    public static void updateProjectFromDTO(UpdateProjectRequestDTO dto, Project project) {
        ProjectMapper.updateProjectFromDTO(dto, project);
    }

    // --- Category Mapper ---
    public static CategoryDTO toCategoryDTO(Category category) {
        return CategoryMapper.toCategoryDTO(category);
    }

    // --- Tutorial Mapper ---
    public static TutorialResponseDTO toTutorialResponseDTO(Tutorial tutorial) {
        return TutorialMapper.toTutorialResponseDTO(tutorial);
    }

    public static List<TutorialResponseDTO> toTutorialResponseDTOList(List<Tutorial> tutorials) {
        return TutorialMapper.toTutorialResponseDTOList(tutorials);
    }

    public static Tutorial toTutorial(CreateTutorialRequestDTO dto) {
        return TutorialMapper.toTutorial(dto);
    }

    public static void updateTutorialFromDTO(UpdateTutorialRequestDTO dto, Tutorial tutorial) {
        TutorialMapper.updateTutorialFromDTO(dto, tutorial);
    }

    // --- Roadmap Mapper ---
    public static RoadmapStepResponseDTO toRoadmapStepResponseDTO(RoadmapStep step) {
        return RoadmapMapper.toRoadmapStepResponseDTO(step);
    }

    public static RoadmapResponseDTO toRoadmapResponseDTO(Roadmap roadmap) {
        return RoadmapMapper.toRoadmapResponseDTO(roadmap);
    }

    public static List<RoadmapResponseDTO> toRoadmapResponseDTOList(List<Roadmap> roadmaps) {
        return RoadmapMapper.toRoadmapResponseDTOList(roadmaps);
    }

    public static Roadmap toRoadmap(CreateRoadmapRequestDTO dto) {
        return RoadmapMapper.toRoadmap(dto);
    }

    public static void updateRoadmapFromDTO(UpdateRoadmapRequestDTO dto, Roadmap roadmap) {
        RoadmapMapper.updateRoadmapFromDTO(dto, roadmap);
    }
}
