package com.web_tutorial.javabackend.domain.dto.response.project;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import com.web_tutorial.javabackend.domain.project.Difficulty;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;

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
    private Difficulty difficulty;
    private ProjectStatus status;
    private List<String> techStack;
    private String authorName;
}
