package com.web_tutorial.javabackend.mapper;

import com.web_tutorial.javabackend.domain.dto.tutorial.CategoryDTO;
import com.web_tutorial.javabackend.domain.tutorial.Category;

public class CategoryMapper {

    public static CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDTO(category.getId(), category.getName(), category.getSlug());
    }
}
