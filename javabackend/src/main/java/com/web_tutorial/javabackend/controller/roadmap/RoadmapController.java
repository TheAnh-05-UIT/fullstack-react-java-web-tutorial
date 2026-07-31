package com.web_tutorial.javabackend.controller.roadmap;

import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

import com.web_tutorial.javabackend.validation.ValidatedPageRequest;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

@RestController
@RequestMapping("/api/v1/roadmaps")
@Validated
public class RoadmapController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "createdAt", "updatedAt");

    private final RoadmapService roadmapService;
    private final SecurityAuditLogger auditLogger;

    public RoadmapController(RoadmapService roadmapService, SecurityAuditLogger auditLogger) {
        this.roadmapService = roadmapService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    @ApiMessage("Get All Roadmaps")
    public ResponseEntity<ResultPaginationDTO> getAllRoadmaps(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Pageable pageable = ValidatedPageRequest.of(page, size, request.getParameterValues("sort"),
                ALLOWED_SORT_FIELDS);
        ResultPaginationDTO response = this.roadmapService.getAllRoadmaps(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get All Roadmaps for Admin")
    public ResponseEntity<ResultPaginationDTO> getAllRoadmapsForAdmin(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Pageable pageable = ValidatedPageRequest.of(page, size, request.getParameterValues("sort"),
                ALLOWED_SORT_FIELDS);
        return ResponseEntity.ok(this.roadmapService.getAllRoadmapsForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get Roadmap by Id for Admin")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapByIdForAdmin(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(this.roadmapService.getRoadmapResponseByIdForAdmin(id));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get Roadmap by Id")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapById(@PathVariable @Positive Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roadmapService.getRoadmapResponseById(id));
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Roadmap by slug")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapBySlug(
            @PathVariable
            @Size(max = VARCHAR_MAX)
            @Pattern(regexp = SLUG_PATTERN)
            String slug) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roadmapService.getRoadmapResponseBySlug(slug));
    }

    @PostMapping
    @ApiMessage("Create a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoadmapResponseDTO> createRoadmap(
            @RequestBody @Valid CreateRoadmapRequestDTO requestDTO) {
        RoadmapResponseDTO response = this.roadmapService.createRoadmapFromDTO(requestDTO);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_CREATED, auditLogger.currentActor(),
                "ROADMAP", response.getId(), "CONTENT_CREATED");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoadmapResponseDTO> updateRoadmap(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateRoadmapRequestDTO requestDTO) {
        RoadmapResponseDTO response = this.roadmapService.updateRoadmapFromDTO(id, requestDTO);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_UPDATED, auditLogger.currentActor(),
                "ROADMAP", id, "CONTENT_UPDATED");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable @Positive Long id) {
        this.roadmapService.deleteRoadmap(id);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_DELETED, auditLogger.currentActor(),
                "ROADMAP", id, "CONTENT_DELETED");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
