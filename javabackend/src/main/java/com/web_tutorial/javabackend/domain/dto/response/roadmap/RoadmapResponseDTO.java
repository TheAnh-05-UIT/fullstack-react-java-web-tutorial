package com.web_tutorial.javabackend.domain.dto.response.roadmap;

import com.web_tutorial.javabackend.domain.project.Difficulty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private Difficulty difficulty;
    private Boolean isActive;
    private Instant createdAt;
    private String createBy;
    private String content;
    private String icon;
    private String color;
    private List<RoadmapStepResponseDTO> steps;
}
