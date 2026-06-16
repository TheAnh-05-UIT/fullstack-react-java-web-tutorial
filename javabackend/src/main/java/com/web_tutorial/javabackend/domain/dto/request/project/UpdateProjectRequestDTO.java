package com.web_tutorial.javabackend.domain.dto.request.project;

import com.web_tutorial.javabackend.domain.project.Difficulty;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

public class UpdateProjectRequestDTO {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Slug cannot be blank")
    private String slug;

    private String description;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String coverImage;
    private String githubUrl;
    private String demoUrl;
    private Difficulty difficulty;
    private ProjectStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
}
