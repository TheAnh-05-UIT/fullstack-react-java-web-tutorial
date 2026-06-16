package com.web_tutorial.javabackend.controller.roadmap;

import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    @ApiMessage("Get All Roadmaps")
    public ResponseEntity<List<RoadmapResponseDTO>> getAllRoadmaps() {
        List<Roadmap> listRoadmap = this.roadmapService.getAllRoadmaps();
        List<RoadmapResponseDTO> listRoadmapResponseDTOs = MapperUtils.toRoadmapResponseDTOList(listRoadmap);
        return ResponseEntity.status(HttpStatus.OK).body(listRoadmapResponseDTOs);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get Roadmap by Id")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapById(@PathVariable Long id) throws IdInvalidException {
        Optional<Roadmap> roadmapById = this.roadmapService.getRoadmapById(id);
        if (!roadmapById.isPresent()) {
            throw new IdInvalidException("Roadmap with Id " + id + " does not exist");
        }
        RoadmapResponseDTO roadmapResponseDTO = MapperUtils.toRoadmapResponseDTO(roadmapById.get());
        return ResponseEntity.status(HttpStatus.OK).body(roadmapResponseDTO);
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Roadmap by slug")
    public ResponseEntity<RoadmapResponseDTO> getRoadmapBySlug(@PathVariable String slug)
            throws ResourceNotFoundException {
        Optional<Roadmap> roadmapBySlug = this.roadmapService.getRoadmapBySlug(slug);
        if (!roadmapBySlug.isPresent()) {
            throw new ResourceNotFoundException("Roadmap with slug " + slug + " does not exist");
        }
        RoadmapResponseDTO roadmapResponseDTO = MapperUtils.toRoadmapResponseDTO(roadmapBySlug.get());
        return ResponseEntity.status(HttpStatus.OK).body(roadmapResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a Roadmap")
    public ResponseEntity<RoadmapResponseDTO> createRoadmap(
            @RequestBody @Valid CreateRoadmapRequestDTO requestDTO) {
        Roadmap roadmap = MapperUtils.toRoadmap(requestDTO);
        Roadmap createdRoadmap = roadmapService.createRoadmap(roadmap);
        RoadmapResponseDTO roadmapResponseDTO = MapperUtils.toRoadmapResponseDTO(createdRoadmap);
        return ResponseEntity.status(HttpStatus.CREATED).body(roadmapResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Roadmap")
    public ResponseEntity<RoadmapResponseDTO> updateRoadmap(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoadmapRequestDTO requestDTO) throws IdInvalidException {
        Optional<Roadmap> roadmapById = this.roadmapService.getRoadmapById(id);
        if (!roadmapById.isPresent()) {
            throw new IdInvalidException("Roadmap with Id " + id + " does not exist");
        }
        Roadmap roadmapDetails = new Roadmap();
        MapperUtils.updateRoadmapFromDTO(requestDTO, roadmapDetails);
        Roadmap updatedRoadmap = this.roadmapService.updateRoadmap(id, roadmapDetails);
        RoadmapResponseDTO roadmapResponseDTO = MapperUtils.toRoadmapResponseDTO(updatedRoadmap);
        return ResponseEntity.status(HttpStatus.OK).body(roadmapResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Roadmap")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable Long id) throws IdInvalidException {
        Optional<Roadmap> roadmapById = this.roadmapService.getRoadmapById(id);
        if (!roadmapById.isPresent()) {
            throw new IdInvalidException("Roadmap with Id " + id + " does not exist");
        }
        this.roadmapService.deleteRoadmap(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
