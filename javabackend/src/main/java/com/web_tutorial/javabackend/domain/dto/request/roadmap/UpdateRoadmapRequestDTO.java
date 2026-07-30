package com.web_tutorial.javabackend.domain.dto.request.roadmap;

import com.web_tutorial.javabackend.domain.project.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.RICH_CONTENT_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

public class UpdateRoadmapRequestDTO {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Slug cannot be blank")
    @Size(max = VARCHAR_MAX, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = SLUG_PATTERN, message = "Slug contains invalid characters")
    private String slug;

    @Size(max = RICH_CONTENT_MAX, message = "Description is too long")
    private String description;
    @Size(max = RICH_CONTENT_MAX, message = "Content is too long")
    private String content;
    @Size(max = VARCHAR_MAX, message = "Cover image must not exceed 255 characters")
    private String coverImage;
    private Difficulty difficulty;
    @Size(max = VARCHAR_MAX, message = "Icon must not exceed 255 characters")
    private String icon;
    @Size(max = VARCHAR_MAX, message = "Color must not exceed 255 characters")
    private String color;

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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
