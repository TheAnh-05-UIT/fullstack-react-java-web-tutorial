package com.web_tutorial.javabackend.service.roadmap;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.model.roadmap.Roadmap;

public interface RoadmapService {
    List<Roadmap> getAllRoadmaps();

    Optional<Roadmap> getRoadmapById(Long id);

    Optional<Roadmap> getRoadmapBySlug(String slug);

    Roadmap createRoadmap(Roadmap roadmap);

    Roadmap updateRoadmap(Long id, Roadmap roadmapDetails);

    void deleteRoadmap(Long id);
}
