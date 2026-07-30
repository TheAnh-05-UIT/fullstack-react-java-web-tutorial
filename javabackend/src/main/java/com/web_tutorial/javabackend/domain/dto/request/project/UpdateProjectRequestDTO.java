package com.web_tutorial.javabackend.domain.dto.request.project;

import com.web_tutorial.javabackend.domain.project.Difficulty;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.RICH_CONTENT_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.TECH_STACK_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

public class UpdateProjectRequestDTO {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Slug cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = SLUG_PATTERN, message = "Slug contains invalid characters")
    private String slug;

    @Size(max = RICH_CONTENT_MAX, message = "Description is too long")
    private String description;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = RICH_CONTENT_MAX, message = "Content is too long")
    private String content;

    @Size(max = VARCHAR_MAX, message = "Thumbnail must not exceed 255 characters")
    private String thumbnail;
    @Size(max = VARCHAR_MAX, message = "GitHub URL must not exceed 255 characters")
    private String githubUrl;
    @Size(max = VARCHAR_MAX, message = "Demo URL must not exceed 255 characters")
    private String demoUrl;
    private Difficulty difficulty;
    private ProjectStatus status;
    @Size(max = TECH_STACK_MAX, message = "Tech stack must not contain more than 50 items")
    private List<@Size(max = VARCHAR_MAX, message = "Tech stack item must not exceed 255 characters") String> techStack;
    @Valid
    private CategoryDTO category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public void setTechStack(List<String> techStack) {
        this.techStack = techStack;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }
}
