package com.web_tutorial.javabackend.domain.dto.response.project;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String thumbnail;
    private String githubLink;
    private String liveLink;
    private Long viewCount;
    private Instant createdAt;
    private String createBy;
    private String content;
    private CategoryDTO category;
}
