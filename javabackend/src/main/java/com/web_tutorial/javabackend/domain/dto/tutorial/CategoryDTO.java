package com.web_tutorial.javabackend.domain.dto.tutorial;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Positive(message = "Category id must be positive")
    private Long id;
    @Size(max = VARCHAR_MAX, message = "Category name must not exceed 255 characters")
    private String name;
    @Size(max = VARCHAR_MAX, message = "Category slug must not exceed 255 characters")
    private String slug;
}
