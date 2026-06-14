package com.web_tutorial.javabackend.service.project;

import com.web_tutorial.javabackend.model.project.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<Project> getAllProjects();
    
    Optional<Project> getProjectById(Long id);
    
    Optional<Project> getProjectBySlug(String slug);
    
    Project createProject(Project project);
    
    Project updateProject(Long id, Project projectDetails);
    
    void deleteProject(Long id);
}
