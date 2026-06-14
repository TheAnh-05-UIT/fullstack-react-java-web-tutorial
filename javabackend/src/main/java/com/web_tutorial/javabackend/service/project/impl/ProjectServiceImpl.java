package com.web_tutorial.javabackend.service.project.impl;

import com.web_tutorial.javabackend.model.project.Project;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.service.project.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> getAllProjects() {
        return this.projectRepository.findAll();
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
    public Project createProject(Project project) {
        return this.projectRepository.save(project);
    }

    @Override
    public Project updateProject(Long id, Project projectDetails) {
        return this.projectRepository.findById(id).map(project -> {
            project.setTitle(projectDetails.getTitle());
            project.setSlug(projectDetails.getSlug());
            project.setDescription(projectDetails.getDescription());
            project.setContent(projectDetails.getContent());
            return this.projectRepository.save(project);
        }).orElseThrow(() -> new RuntimeException("Project not found with id " + id));
    }

    @Override
    public void deleteProject(Long id) {
        this.projectRepository.deleteById(id);
    }
}
