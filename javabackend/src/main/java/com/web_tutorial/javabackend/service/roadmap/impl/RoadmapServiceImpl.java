package com.web_tutorial.javabackend.service.roadmap.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.service.roadmap.RoadmapService;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.CreateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.roadmap.UpdateRoadmapRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.roadmap.RoadmapResponseDTO;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
        return this.roadmapRepository.findByIsDeletedFalseOrderByIdDesc();
    }

    @Override
    public ResultPaginationDTO getAllRoadmaps(Pageable pageable) {
        Page<Roadmap> page = this.roadmapRepository.findByIsDeletedFalseOrderByIdDesc(pageable);
        return toRoadmapPage(page);
    }

    @Override
    public ResultPaginationDTO getAllRoadmapsForAdmin(Pageable pageable) {
        return toRoadmapPage(this.roadmapRepository.findAllByOrderByIdDesc(pageable));
    }

    private ResultPaginationDTO toRoadmapPage(Page<Roadmap> page) {
        Set<String> emails = page.getContent().stream()
                .map(Roadmap::getCreateBy)
                .filter(email -> email != null && !email.trim().isEmpty())
                .collect(Collectors.toSet());

        Map<String, String> authorMap;
        if (emails.isEmpty()) {
            authorMap = Collections.emptyMap();
        } else {
            authorMap = userRepository.findAllByEmailIn(emails).stream()
                    .filter(u -> u.getEmail() != null && u.getUsername() != null)
                    .collect(Collectors.toMap(
                            User::getEmail,
                            User::getUsername,
                            (existing, replacement) -> existing
                    ));
        }

        return MapperUtils.toResultPaginationDTO(page, roadmap -> {
            RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(roadmap);
            if (dto.getCreateBy() != null) {
                String authorName = authorMap.getOrDefault(dto.getCreateBy(), dto.getCreateBy());
                dto.setAuthorName(authorName);
            }
            return dto;
        });
    }

    @Override
    public Optional<Roadmap> getRoadmapById(Long id) {
        return this.roadmapRepository.findByIdAndIsDeletedFalse(id);
    }

    @Override
    public Optional<Roadmap> getRoadmapBySlug(String slug) {
        return this.roadmapRepository.findBySlugAndIsDeletedFalse(slug);
    }

    @Override
    @Transactional
    public Roadmap createRoadmap(Roadmap roadmap) {
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        roadmap.setCreateBy(currentUser);
        roadmap.setCreatedAt(Instant.now());
        return this.roadmapRepository.save(roadmap);
    }

    @Override
    @Transactional
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
    @Transactional
    public void deleteRoadmap(Long id) {
        if (!this.roadmapRepository.existsById(id)) {
            throw new ResourceNotFoundException("Roadmap with Id " + id + " does not exist");
        }
        this.roadmapRepository.deleteById(id);
    }

    @Override
    public RoadmapResponseDTO getRoadmapResponseById(Long id) {
        Roadmap roadmap = this.getRoadmapById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap with Id " + id + " does not exist"));
        RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(roadmap);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    public RoadmapResponseDTO getRoadmapResponseBySlug(String slug) {
        Roadmap roadmap = this.getRoadmapBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap with slug " + slug + " does not exist"));
        RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(roadmap);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    public RoadmapResponseDTO getRoadmapResponseByIdForAdmin(Long id) {
        Roadmap roadmap = this.roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found"));
        RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(roadmap);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    @Transactional
    public RoadmapResponseDTO createRoadmapFromDTO(CreateRoadmapRequestDTO requestDTO) {
        Roadmap roadmap = MapperUtils.toRoadmap(requestDTO);
        Roadmap createdRoadmap = this.createRoadmap(roadmap);
        RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(createdRoadmap);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }

    @Override
    @Transactional
    public RoadmapResponseDTO updateRoadmapFromDTO(Long id, UpdateRoadmapRequestDTO requestDTO) {
        Roadmap roadmapDetails = new Roadmap();
        MapperUtils.updateRoadmapFromDTO(requestDTO, roadmapDetails);
        Roadmap updatedRoadmap = this.updateRoadmap(id, roadmapDetails);
        RoadmapResponseDTO dto = MapperUtils.toRoadmapResponseDTO(updatedRoadmap);
        if (dto.getCreateBy() != null) {
            dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
        }
        return dto;
    }
}
