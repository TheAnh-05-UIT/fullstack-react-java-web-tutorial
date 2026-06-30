package com.web_tutorial.javabackend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapStepResponseDTO;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.roadmap.RoadmapStep;

public class RoadmapMapper {

    public static RoadmapStepResponseDTO toRoadmapStepResponseDTO(RoadmapStep step) {
        if (step == null) {
            return null;
        }
        return new RoadmapStepResponseDTO(
                step.getId(),
                step.getTitle(),
                step.getDescription());
    }

    public static RoadmapResponseDTO toRoadmapResponseDTO(Roadmap roadmap) {
        if (roadmap == null) {
            return null;
        }

        List<RoadmapStepResponseDTO> steps = roadmap.getSteps() != null
                ? roadmap.getSteps().stream().map(RoadmapMapper::toRoadmapStepResponseDTO).collect(Collectors.toList())
                : null;

        return new RoadmapResponseDTO(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getSlug(),
                roadmap.getDescription(),
                roadmap.getDifficulty(),
                !roadmap.isDeleted(),
                roadmap.getCreatedAt(),
                roadmap.getCreateBy(),
                roadmap.getContent(),
                roadmap.getCoverImage(),
                roadmap.getIcon(),
                roadmap.getColor(),
                steps,
                null);
    }

    public static List<RoadmapResponseDTO> toRoadmapResponseDTOList(List<Roadmap> roadmaps) {
        if (roadmaps == null) {
            return null;
        }
        return roadmaps.stream().map(RoadmapMapper::toRoadmapResponseDTO).collect(Collectors.toList());
    }

    public static Roadmap toRoadmap(CreateRoadmapRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(dto.getTitle());
        roadmap.setSlug(dto.getSlug());
        roadmap.setDescription(dto.getDescription());
        roadmap.setContent(dto.getContent());
        roadmap.setCoverImage(dto.getCoverImage());
        roadmap.setDifficulty(dto.getDifficulty());
        roadmap.setIcon(dto.getIcon());
        roadmap.setColor(dto.getColor());
        return roadmap;
    }

    public static void updateRoadmapFromDTO(UpdateRoadmapRequestDTO dto, Roadmap roadmap) {
        if (dto == null || roadmap == null) {
            return;
        }
        roadmap.setTitle(dto.getTitle());
        roadmap.setSlug(dto.getSlug());
        roadmap.setDescription(dto.getDescription());
        roadmap.setContent(dto.getContent());
        roadmap.setCoverImage(dto.getCoverImage());
        roadmap.setDifficulty(dto.getDifficulty());
        roadmap.setIcon(dto.getIcon());
        roadmap.setColor(dto.getColor());
    }
}
