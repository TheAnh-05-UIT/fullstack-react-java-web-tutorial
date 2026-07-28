package com.web_tutorial.javabackend.controller.project;

import com.web_tutorial.javabackend.domain.dto.request.project.CreateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.project.UpdateProjectRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.service.project.ProjectService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get All Projects for Admin")
    public ResponseEntity<ResultPaginationDTO> getAllProjectsForAdmin(Pageable pageable) {
        return ResponseEntity.ok(this.projectService.getAllProjectsForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get Project by Id for Admin")
    public ResponseEntity<ProjectResponseDTO> getProjectByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(this.projectService.getProjectResponseByIdForAdmin(id));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get All by Id")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.projectService.getProjectResponseById(id));
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Project by slug")
    public ResponseEntity<ProjectResponseDTO> getProjectBySlug(@PathVariable String slug) {
        return ResponseEntity.status(HttpStatus.OK).body(this.projectService.getProjectResponseBySlug(slug));
    }

    @PostMapping
    @ApiMessage("Create a Project")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestBody @Valid CreateProjectRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.projectService.createProjectFromDTO(requestDTO));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Project")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(this.projectService.updateProjectFromDTO(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Project")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        this.projectService.deleteProject(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
