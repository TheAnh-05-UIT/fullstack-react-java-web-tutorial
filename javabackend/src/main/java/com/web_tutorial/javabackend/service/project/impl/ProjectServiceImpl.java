package com.web_tutorial.javabackend.service.project.impl;

import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.service.project.ProjectService;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

import com.web_tutorial.javabackend.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getAuthorNameByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).map(u -> u.getUsername()).orElse(email);
    }

    @Override
    public List<Project> getAllProjects() {
        return this.projectRepository.findAllByOrderByIdDesc();
    }

    @Override
    public ResultPaginationDTO getAllProjects(Pageable pageable) {
        Page<Project> page = this.projectRepository.findAllByOrderByIdDesc(pageable);
        List<Project> projects = page.getContent();

        // Thu thập danh sách email tác giả (loại bỏ null/rỗng và trùng lặp)
        Set<String> emails = projects.stream()
                .map(Project::getCreateBy)
                .filter(email -> email != null && !email.trim().isEmpty())
                .collect(Collectors.toSet());

        // Batch query 1 lần duy nhất bằng IN thay vì N query trong vòng lặp
        Map<String, String> authorMap;
        if (emails.isEmpty()) {
            authorMap = Collections.emptyMap();
        } else {
            authorMap = userRepository.findAllByEmailIn(emails).stream()
                    .filter(u -> u.getEmail() != null && u.getUsername() != null)
                    .collect(Collectors.toMap(
                            User::getEmail,
                            User::getUsername,
                            (existing, replacement) -> existing
                    ));
        }

        return MapperUtils.toResultPaginationDTO(page, project -> {
            ProjectResponseDTO dto = MapperUtils.toProjectResponseDTO(project);
            if (dto.getCreateBy() != null) {
                String authorName = authorMap.getOrDefault(dto.getCreateBy(), dto.getCreateBy());
                dto.setAuthorName(authorName);
            }
            return dto;
        });
    }

    @Override
    public Optional<Project> getProjectById(Long id) {
        return this.projectRepository.findById(id);
    }

    @Override
    public Optional<Project> getProjectBySlug(String slug) {
        return this.projectRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Project createProject(Project project) {
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        project.setCreateBy(currentUser);
        project.setCreatedAt(Instant.now());

        if (project.getCategory() != null && project.getCategory().getName() != null) {
            String catName = project.getCategory().getName();
            Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                Category newCat = new Category();
                newCat.setName(catName);
                newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                return categoryRepository.save(newCat);
            });
            project.setCategory(category);
        }

        return this.projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(Long id, Project projectDetails) {
        return this.projectRepository.findById(id).map(project -> {
            if (projectDetails.getTitle() != null)
                project.setTitle(projectDetails.getTitle());
            if (projectDetails.getSlug() != null)
                project.setSlug(projectDetails.getSlug());
            if (projectDetails.getDescription() != null)
                project.setDescription(projectDetails.getDescription());
            if (projectDetails.getContent() != null)
                project.setContent(projectDetails.getContent());
            if (projectDetails.getCoverImage() != null)
                project.setCoverImage(projectDetails.getCoverImage());
            if (projectDetails.getGithubUrl() != null)
                project.setGithubUrl(projectDetails.getGithubUrl());
            if (projectDetails.getDemoUrl() != null)
                project.setDemoUrl(projectDetails.getDemoUrl());
            if (projectDetails.getDifficulty() != null)
                project.setDifficulty(projectDetails.getDifficulty());
            if (projectDetails.getStatus() != null)
                project.setStatus(projectDetails.getStatus());

            if (projectDetails.getCategory() != null && projectDetails.getCategory().getName() != null) {
                String catName = projectDetails.getCategory().getName();
                Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(catName);
                    newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                    return categoryRepository.save(newCat);
                });
                project.setCategory(category);
            }

            return this.projectRepository.save(project);
        }).orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        if (!this.projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project with Id " + id + " does not exist");
        }
        this.projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        this.projectRepository.incrementViews(id);
    }

    @Override
    @Transactional
    public ProjectResponseDTO getProjectResponseById(Long id) {
        Project project = this.getProjectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project with Id " + id + " does not exist"));
        this.incrementViewCount(project.getId());
        project.setViews((project.getViews() == null ? 0L : project.getViews()) + 1);
        ProjectResponseDTO dto = MapperUtils.toProjectResponseDTO(project);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    @Transactional
    public ProjectResponseDTO getProjectResponseBySlug(String slug) {
        Project project = this.getProjectBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project with slug " + slug + " does not exist"));
        this.incrementViewCount(project.getId());
        project.setViews((project.getViews() == null ? 0L : project.getViews()) + 1);
        ProjectResponseDTO dto = MapperUtils.toProjectResponseDTO(project);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    @Transactional
    public ProjectResponseDTO createProjectFromDTO(CreateProjectRequestDTO requestDTO) {
        Project project = MapperUtils.toProject(requestDTO);
        Project createdProject = this.createProject(project);
        ProjectResponseDTO dto = MapperUtils.toProjectResponseDTO(createdProject);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProjectFromDTO(Long id, UpdateProjectRequestDTO requestDTO) {
        Project projectDetails = new Project();
        MapperUtils.updateProjectFromDTO(requestDTO, projectDetails);
        Project updatedProject = this.updateProject(id, projectDetails);
        ProjectResponseDTO dto = MapperUtils.toProjectResponseDTO(updatedProject);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }
}
