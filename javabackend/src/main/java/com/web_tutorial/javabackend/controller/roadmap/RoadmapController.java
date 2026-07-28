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

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    @ApiMessage("Get All Roadmaps")
    public ResponseEntity<ResultPaginationDTO> getAllRoadmaps(Pageable pageable) {
        ResultPaginationDTO response = this.roadmapService.getAllRoadmaps(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get All Roadmaps for Admin")
    public ResponseEntity<ResultPaginationDTO> getAllRoadmapsForAdmin(Pageable pageable) {
        return ResponseEntity.ok(this.roadmapService.getAllRoadmapsForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get Roadmap by Id for Admin")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(this.roadmapService.getRoadmapResponseByIdForAdmin(id));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get Roadmap by Id")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roadmapService.getRoadmapResponseById(id));
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Roadmap by slug")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapBySlug(@PathVariable String slug) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roadmapService.getRoadmapResponseBySlug(slug));
    }

    @PostMapping
    @ApiMessage("Create a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoadmapResponseDTO> createRoadmap(
            @RequestBody @Valid CreateRoadmapRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roadmapService.createRoadmapFromDTO(requestDTO));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoadmapResponseDTO> updateRoadmap(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoadmapRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roadmapService.updateRoadmapFromDTO(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Roadmap")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable Long id) {
        this.roadmapService.deleteRoadmap(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
