package com.web_tutorial.javabackend.repository.roadmap;

import com.web_tutorial.javabackend.model.roadmap.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
}
