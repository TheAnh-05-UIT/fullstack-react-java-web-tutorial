package com.web_tutorial.javabackend.domain.dto.request.tutorial;

import jakarta.validation.constraints.NotBlank;
import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;

public class CreateTutorialRequestDTO {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Slug cannot be blank")
    private String slug;

    private String description;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String coverImage;

    private TutorialStatus status;

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
