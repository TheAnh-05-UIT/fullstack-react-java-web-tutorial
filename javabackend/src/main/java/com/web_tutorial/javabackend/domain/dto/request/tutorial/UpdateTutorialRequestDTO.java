package com.web_tutorial.javabackend.domain.dto.request.tutorial;

import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.RICH_CONTENT_MAX;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

public class UpdateTutorialRequestDTO {

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

    @Size(max = VARCHAR_MAX, message = "Cover image must not exceed 255 characters")
    private String coverImage;
    private TutorialStatus status;
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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public TutorialStatus getStatus() {
        return status;
    }

    public void setStatus(TutorialStatus status) {
        this.status = status;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }
}
