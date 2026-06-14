package com.web_tutorial.javabackend.repository.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.roadmap.RoadmapStep;

@Repository
public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
}
