package com.web_tutorial.javabackend.service.roadmap.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;

    public RoadmapServiceImpl(RoadmapRepository roadmapRepository) {
        this.roadmapRepository = roadmapRepository;
    }

    @Override
    public List<Roadmap> getAllRoadmaps() {
        return this.roadmapRepository.findAll();
    }

    @Override
    public Optional<Roadmap> getRoadmapById(Long id) {
        return this.roadmapRepository.findById(id);
    }

    @Override
    public Optional<Roadmap> getRoadmapBySlug(String slug) {
        return this.roadmapRepository.findBySlug(slug);
    }

    @Override
    public Roadmap createRoadmap(Roadmap roadmap) {
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        roadmap.setCreateBy(currentUser);
        roadmap.setCreatedAt(Instant.now());
        return this.roadmapRepository.save(roadmap);
    }

    @Override
    public Roadmap updateRoadmap(Long id, Roadmap roadmapDetails) {
        return this.roadmapRepository.findById(id).map(roadmap -> {
            if (roadmapDetails.getTitle() != null)
                roadmap.setTitle(roadmapDetails.getTitle());
            if (roadmapDetails.getSlug() != null)
                roadmap.setSlug(roadmapDetails.getSlug());
            if (roadmapDetails.getDescription() != null)
                roadmap.setDescription(roadmapDetails.getDescription());
            if (roadmapDetails.getCoverImage() != null)
                roadmap.setCoverImage(roadmapDetails.getCoverImage());
            if (roadmapDetails.getDifficulty() != null)
                roadmap.setDifficulty(roadmapDetails.getDifficulty());
            return this.roadmapRepository.save(roadmap);
        }).orElseThrow(() -> new RuntimeException("roadmap not found with id " + id));
    }

    @Override
    public void deleteRoadmap(Long id) {
        this.roadmapRepository.deleteById(id);
    }

}
