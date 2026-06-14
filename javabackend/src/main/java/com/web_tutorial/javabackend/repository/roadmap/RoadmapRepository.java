package com.web_tutorial.javabackend.repository.roadmap;

import com.web_tutorial.javabackend.model.roadmap.Roadmap;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    Optional<Roadmap> findBySlug(String slug);
}
