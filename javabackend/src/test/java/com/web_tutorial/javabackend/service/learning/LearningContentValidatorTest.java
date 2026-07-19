package com.web_tutorial.javabackend.service.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;

@ExtendWith(MockitoExtension.class)
public class LearningContentValidatorTest {

    @Mock
    private TutorialRepository tutorialRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private RoadmapRepository roadmapRepository;
    @Mock
    private DevopsPhaseRepository devopsPhaseRepository;

    @InjectMocks
    private LearningContentValidator validator;

    @Test
    void normalizeAndValidateKey_NullKey_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> validator.normalizeAndValidateKey(null));
    }

    @Test
    void normalizeAndValidateKey_BlankKey_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> validator.normalizeAndValidateKey("   "));
    }

    @Test
    void normalizeAndValidateKey_TooLongKey_ThrowsException() {
        String longKey = "a".repeat(191);
        assertThrows(IdInvalidException.class, () -> validator.normalizeAndValidateKey(longKey));
    }

    @Test
    void normalizeAndValidateKey_ValidKey_ReturnsTrimmed() throws Exception {
        String key = "  valid-key  ";
        assertEquals("valid-key", validator.normalizeAndValidateKey(key));
    }

    @Test
    void validateExists_NullType_ThrowsException() {
        assertThrows(IdInvalidException.class, () -> validator.validateExists(null, "key"));
    }

    @Test
    void validateExists_Tutorial_Valid() throws Exception {
        when(tutorialRepository.existsBySlugAndIsDeletedFalse("react")).thenReturn(true);
        validator.validateExists(LearningContentType.TUTORIAL, "react");
        // No exception
    }

    @Test
    void validateExists_Tutorial_SoftDeleted_ThrowsNotFound() {
        when(tutorialRepository.existsBySlugAndIsDeletedFalse("react")).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> validator.validateExists(LearningContentType.TUTORIAL, "react"));
    }

    @Test
    void validateExists_Project_Valid() throws Exception {
        when(projectRepository.existsBySlug("proj-1")).thenReturn(true);
        validator.validateExists(LearningContentType.PROJECT, "proj-1");
    }

    @Test
    void validateExists_Roadmap_Valid() throws Exception {
        when(roadmapRepository.existsBySlug("road-1")).thenReturn(true);
        validator.validateExists(LearningContentType.ROADMAP, "road-1");
    }

    @Test
    void validateExists_DevopsPhase_Valid() throws Exception {
        when(devopsPhaseRepository.existsByPhaseKey("planning")).thenReturn(true);
        validator.validateExists(LearningContentType.DEVOPS_PHASE, "planning");
    }
}
