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
    public ProjectResponseDTO(Long id2, String title2, String slug2, String description2, String coverImage,
            String githubUrl, String demoUrl, Long views, Instant createdAt2, String createBy2, String content2,
            CategoryDTO categoryDTO, Difficulty difficulty2, ProjectStatus status2, List<String> tags, Object object,
            Object object2) {
        // TODO Auto-generated constructor stub
    }

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
