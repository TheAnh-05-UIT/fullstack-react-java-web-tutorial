package com.web_tutorial.javabackend.service.roadmap.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

import com.web_tutorial.javabackend.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.mapper.MapperUtils;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;

    public RoadmapServiceImpl(RoadmapRepository roadmapRepository, UserRepository userRepository) {
        this.roadmapRepository = roadmapRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getAuthorNameByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).map(u -> u.getUsername()).orElse(email);
    }

    @Override
    public List<Roadmap> getAllRoadmaps() {
        return this.roadmapRepository.findAllByOrderByIdDesc();
    }

    @Override
    public ResultPaginationDTO getAllRoadmaps(Pageable pageable) {
        Page<Roadmap> page = this.roadmapRepository.findAllByOrderByIdDesc(pageable);
        return MapperUtils.toResultPaginationDTO(page, roadmap -> {
            RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(roadmap);
            if (dto.getCreateBy() != null) {
                dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
            }
            return dto;
        });
    }

    @Override
    public Optional<Roadmap> getRoadmapById(Long id) {
        return this.roadmapRepository.findById(id);
    }

    @Override
    public Optional<Roadmap> getRoadmapBySlug(String slug) {
        return this.roadmapRepository.findBySlug(slug);
    }

    @Override
    public Roadmap createRoadmap(Roadmap roadmap) {
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        roadmap.setCreateBy(currentUser);
        roadmap.setCreatedAt(Instant.now());
        return this.roadmapRepository.save(roadmap);
    }

    @Override
    public Roadmap updateRoadmap(Long id, Roadmap roadmapDetails) {
        return this.roadmapRepository.findById(id).map(roadmap -> {
            if (roadmapDetails.getTitle() != null)
                roadmap.setTitle(roadmapDetails.getTitle());
            if (roadmapDetails.getSlug() != null)
                roadmap.setSlug(roadmapDetails.getSlug());
            if (roadmapDetails.getDescription() != null)
                roadmap.setDescription(roadmapDetails.getDescription());
            if (roadmapDetails.getCoverImage() != null)
                roadmap.setCoverImage(roadmapDetails.getCoverImage());
            if (roadmapDetails.getDifficulty() != null)
                roadmap.setDifficulty(roadmapDetails.getDifficulty());
            if (roadmapDetails.getContent() != null)
                roadmap.setContent(roadmapDetails.getContent());
            if (roadmapDetails.getIcon() != null)
                roadmap.setIcon(roadmapDetails.getIcon());
            if (roadmapDetails.getColor() != null)
                roadmap.setColor(roadmapDetails.getColor());
            return this.roadmapRepository.save(roadmap);
        }).orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id " + id));
    }

    @Override
    public void deleteRoadmap(Long id) {
        this.roadmapRepository.deleteById(id);
    }

}
