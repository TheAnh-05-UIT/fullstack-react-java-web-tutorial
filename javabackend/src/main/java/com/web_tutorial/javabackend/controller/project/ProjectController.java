package com.web_tutorial.javabackend.controller.project;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.project.ProjectService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @ApiMessage("Get All Projects")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<Project> lisProjects = this.projectService.getAllProjects();
        List<ProjectResponseDTO> projectResponseDTO = MapperUtils.toProjectResponseDTOList(lisProjects);
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get All by Id")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(projectById.get());
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Project by slug")
    public ResponseEntity<ProjectResponseDTO> getProjectBySlug(@PathVariable String slug)
            throws ResourceNotFoundException {
        Optional<Project> projectBySlug = this.projectService.getProjectBySlug(slug);
        if (!projectBySlug.isPresent()) {
            throw new ResourceNotFoundException("Project with slug " + slug + " does not exist");
        }
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(projectBySlug.get());
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a Project")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestBody @Valid CreateProjectRequestDTO requestDTO) {
        Project project = MapperUtils.toProject(requestDTO);
        Project createdProject = projectService.createProject(project);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(createdProject);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Project")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @RequestBody Project projectDetails) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        Project updatedProject = this.projectService.updateProject(id, projectDetails);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(updatedProject);
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        this.projectService.deleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
