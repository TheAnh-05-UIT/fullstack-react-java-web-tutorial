package com.web_tutorial.javabackend.controller.roadmap;

import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    public ResponseEntity<List<Roadmap>> getAllRoadmaps() {
        return ResponseEntity.status(HttpStatus.OK).body(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Roadmap> getRoadmapById(@PathVariable Long id) {
        return roadmapService.getRoadmapById(id)
                .map(roadmap -> ResponseEntity.status(HttpStatus.OK).body(roadmap))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Roadmap> getRoadmapBySlug(@PathVariable String slug) {
        return roadmapService.getRoadmapBySlug(slug)
                .map(roadmap -> ResponseEntity.status(HttpStatus.OK).body(roadmap))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping
    public ResponseEntity<Roadmap> createRoadmap(@RequestBody Roadmap roadmap) {
        Roadmap createdRoadmap = roadmapService.createRoadmap(roadmap);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoadmap);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Roadmap> updateRoadmap(@PathVariable Long id, @RequestBody Roadmap roadmapDetails) {
        try {
            Roadmap updatedRoadmap = roadmapService.updateRoadmap(id, roadmapDetails);
            return ResponseEntity.status(HttpStatus.OK).body(updatedRoadmap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable Long id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
