package com.web_tutorial.javabackend.domain.dto.response.roadmap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapStepResponseDTO {
    private Long id;
    private String title;
    private String description;
}
