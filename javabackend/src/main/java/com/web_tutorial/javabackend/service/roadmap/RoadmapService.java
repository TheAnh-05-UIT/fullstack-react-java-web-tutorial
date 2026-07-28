package com.web_tutorial.javabackend.service.roadmap;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;

import org.springframework.data.domain.Pageable;

public interface RoadmapService {
    List<Roadmap> getAllRoadmaps();
    ResultPaginationDTO getAllRoadmaps(Pageable pageable);
    ResultPaginationDTO getAllRoadmapsForAdmin(Pageable pageable);
    String getAuthorNameByEmail(String email);

    Optional<Roadmap> getRoadmapById(Long id);

    Optional<Roadmap> getRoadmapBySlug(String slug);

    Roadmap createRoadmap(Roadmap roadmap);

    Roadmap updateRoadmap(Long id, Roadmap roadmapDetails);

    void deleteRoadmap(Long id);

    // DTO response methods cho Phase 3
    RoadmapResponseDTO getRoadmapResponseById(Long id);

    RoadmapResponseDTO getRoadmapResponseBySlug(String slug);
    RoadmapResponseDTO getRoadmapResponseByIdForAdmin(Long id);

    RoadmapResponseDTO createRoadmapFromDTO(CreateRoadmapRequestDTO requestDTO);

    RoadmapResponseDTO updateRoadmapFromDTO(Long id, UpdateRoadmapRequestDTO requestDTO);
}
