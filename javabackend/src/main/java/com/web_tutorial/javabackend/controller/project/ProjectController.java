package com.web_tutorial.javabackend.controller.project;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.project.ProjectService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResultPaginationDTO> getAllProjects(Pageable pageable) {
        ResultPaginationDTO response = this.projectService.getAllProjects(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get All by Id")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        Project project = projectById.get();
        this.projectService.incrementViewCount(project.getId());
        project.setViews((project.getViews() == null ? 0L : project.getViews()) + 1);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(project);
        if (projectResponseDTO.getCreateBy() != null) {
            projectResponseDTO.setAuthorName(this.projectService.getAuthorNameByEmail(projectResponseDTO.getCreateBy()));
        }
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
        Project project = projectBySlug.get();
        this.projectService.incrementViewCount(project.getId());
        project.setViews((project.getViews() == null ? 0L : project.getViews()) + 1);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(project);
        if (projectResponseDTO.getCreateBy() != null) {
            projectResponseDTO.setAuthorName(this.projectService.getAuthorNameByEmail(projectResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a Project")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestBody @Valid CreateProjectRequestDTO requestDTO) {
        Project project = MapperUtils.toProject(requestDTO);
        Project createdProject = projectService.createProject(project);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(createdProject);
        if (projectResponseDTO.getCreateBy() != null) {
            projectResponseDTO.setAuthorName(this.projectService.getAuthorNameByEmail(projectResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Project")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectRequestDTO requestDTO) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        Project projectDetails = new Project();
        MapperUtils.updateProjectFromDTO(requestDTO, projectDetails);
        Project updatedProject = this.projectService.updateProject(id, projectDetails);
        ProjectResponseDTO projectResponseDTO = MapperUtils.toProjectResponseDTO(updatedProject);
        if (projectResponseDTO.getCreateBy() != null) {
            projectResponseDTO.setAuthorName(this.projectService.getAuthorNameByEmail(projectResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(projectResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Project")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) throws IdInvalidException {
        Optional<Project> projectById = this.projectService.getProjectById(id);
        if (!projectById.isPresent()) {
            throw new IdInvalidException("Project with Id " + id + " does not exist");
        }
        this.projectService.deleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
