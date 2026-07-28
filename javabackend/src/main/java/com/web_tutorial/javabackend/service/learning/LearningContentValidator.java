package com.web_tutorial.javabackend.service.learning;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;

@Component
public class LearningContentValidator {

    private final TutorialRepository tutorialRepository;
    private final ProjectRepository projectRepository;
    private final RoadmapRepository roadmapRepository;
    private final DevopsPhaseRepository devopsPhaseRepository;

    public LearningContentValidator(
            TutorialRepository tutorialRepository,
            ProjectRepository projectRepository,
            RoadmapRepository roadmapRepository,
            DevopsPhaseRepository devopsPhaseRepository) {
        this.tutorialRepository = tutorialRepository;
        this.projectRepository = projectRepository;
        this.roadmapRepository = roadmapRepository;
        this.devopsPhaseRepository = devopsPhaseRepository;
    }

    public String normalizeAndValidateKey(String contentKey) throws IdInvalidException {
        if (!StringUtils.hasText(contentKey)) {
            throw new IdInvalidException("Content key cannot be null or blank");
        }
        String normalizedKey = contentKey.trim();
        if (normalizedKey.length() > 190) {
            throw new IdInvalidException("Content key exceeds maximum length of 190 characters");
        }
        return normalizedKey;
    }

    public void validateExists(LearningContentType contentType, String normalizedKey) throws IdInvalidException {
        if (contentType == null) {
            throw new IdInvalidException("Content type cannot be null");
        }
        if (!StringUtils.hasText(normalizedKey)) {
            throw new IdInvalidException("Content key cannot be null or blank");
        }

        boolean exists;
        switch (contentType) {
            case TUTORIAL:
                exists = tutorialRepository.existsBySlugAndStatusAndIsDeletedFalse(
                        normalizedKey,
                        TutorialStatus.PUBLISHED);
                break;
            case PROJECT:
                exists = projectRepository.existsBySlugAndStatusAndIsDeletedFalse(
                        normalizedKey,
                        ProjectStatus.PUBLISHED);
                break;
            case ROADMAP:
                exists = roadmapRepository.existsBySlugAndIsDeletedFalse(normalizedKey);
                break;
            case DEVOPS_PHASE:
                exists = devopsPhaseRepository.existsByPhaseKeyAndActiveTrue(normalizedKey);
                break;
            default:
                throw new IdInvalidException("Unsupported learning content type: " + contentType);
        }

        if (!exists) {
            throw new ResourceNotFoundException("Content not found for type " + contentType + " and key " + normalizedKey);
        }
    }
}
