package com.web_tutorial.javabackend.service.roadmap.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.model.roadmap.Roadmap;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;

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
        return this.roadmapRepository.save(roadmap);
    }

    @Override
    public Roadmap updateRoadmap(Long id, Roadmap roadmapDetails) {
        return this.roadmapRepository.findById(id).map(roadmap -> {
            roadmap.setTitle(roadmapDetails.getTitle());
            roadmap.setSlug(roadmapDetails.getSlug());
            roadmap.setDescription(roadmapDetails.getDescription());
            return this.roadmapRepository.save(roadmap);
        }).orElseThrow(() -> new RuntimeException("roadmap not found with id " + id));
    }

    @Override
    public void deleteRoadmap(Long id) {
        this.roadmapRepository.deleteById(id);
    }

}
