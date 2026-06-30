package com.web_tutorial.javabackend.service.roadmap;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.roadmap.Roadmap;

import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;

public interface RoadmapService {
    List<Roadmap> getAllRoadmaps();
    ResultPaginationDTO getAllRoadmaps(Pageable pageable);
    String getAuthorNameByEmail(String email);

    Optional<Roadmap> getRoadmapById(Long id);

    Optional<Roadmap> getRoadmapBySlug(String slug);

    Roadmap createRoadmap(Roadmap roadmap);

    Roadmap updateRoadmap(Long id, Roadmap roadmapDetails);

    void deleteRoadmap(Long id);
}
