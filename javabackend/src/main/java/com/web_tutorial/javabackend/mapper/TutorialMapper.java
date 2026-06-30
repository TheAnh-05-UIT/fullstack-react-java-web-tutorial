package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

public class TutorialMapper {

    public static TutorialResponseDTO toTutorialResponseDTO(Tutorial tutorial) {
        if (tutorial == null) {
            return null;
        }
        return new TutorialResponseDTO(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getSlug(),
                tutorial.getDescription(),
                tutorial.getCoverImage(),
                tutorial.getStatus(),
                tutorial.getViews(),
                tutorial.getCreatedAt(),
                tutorial.getCreateBy(),
                tutorial.getContent(),
                CategoryMapper.toCategoryDTO(tutorial.getCategory()),
                null);
    }

    public static List<TutorialResponseDTO> toTutorialResponseDTOList(List<Tutorial> tutorials) {
        if (tutorials == null) {
            return null;
        }
        return tutorials.stream().map(TutorialMapper::toTutorialResponseDTO).collect(Collectors.toList());
    }

    public static Tutorial toTutorial(CreateTutorialRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(dto.getTitle());
        tutorial.setSlug(dto.getSlug());
        tutorial.setDescription(dto.getDescription());
        tutorial.setContent(dto.getContent());
        tutorial.setCoverImage(dto.getCoverImage());
        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            tutorial.setCategory(category);
        }
        return tutorial;
    }

    public static void updateTutorialFromDTO(UpdateTutorialRequestDTO dto, Tutorial tutorial) {
        if (dto == null || tutorial == null) {
            return;
        }
        tutorial.setTitle(dto.getTitle());
        tutorial.setSlug(dto.getSlug());
        tutorial.setDescription(dto.getDescription());
        tutorial.setContent(dto.getContent());
        tutorial.setCoverImage(dto.getCoverImage());
        tutorial.setStatus(dto.getStatus());
        if (dto.getCategory() != null && dto.getCategory().getName() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            tutorial.setCategory(category);
        }
    }
}
