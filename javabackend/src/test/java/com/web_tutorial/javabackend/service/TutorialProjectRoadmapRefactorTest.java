package com.web_tutorial.javabackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.tutorial.impl.TutorialServiceImpl;
import com.web_tutorial.javabackend.service.project.impl.ProjectServiceImpl;
import com.web_tutorial.javabackend.service.roadmap.impl.RoadmapServiceImpl;

@ExtendWith(MockitoExtension.class)
class TutorialProjectRoadmapRefactorTest {

    @Mock
    private TutorialRepository tutorialRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private RoadmapRepository roadmapRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void testTutorialGetResponseById_IncrementViewAndEnrichAuthor() {
        TutorialServiceImpl tutorialService = new TutorialServiceImpl(tutorialRepository, categoryRepository, userRepository);
        Tutorial tutorial = new Tutorial();
        tutorial.setId(10L);
        tutorial.setTitle("Test Tutorial");
        tutorial.setViews(5L);
        tutorial.setCreateBy("author@example.com");

        when(tutorialRepository.findByIdAndStatusAndIsDeletedFalse(10L, TutorialStatus.PUBLISHED))
                .thenReturn(Optional.of(tutorial));

        TutorialResponseDTO dto = tutorialService.getTutorialResponseById(10L);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Test Tutorial", dto.getTitle());
        verify(tutorialRepository, times(1)).incrementViews(10L);
        verify(userRepository, times(1)).findByEmail("author@example.com");
    }

    @Test
    void testTutorialGetResponseById_NotFound_ThrowsExceptionNoIncrement() {
        TutorialServiceImpl tutorialService = new TutorialServiceImpl(tutorialRepository, categoryRepository, userRepository);
        when(tutorialRepository.findByIdAndStatusAndIsDeletedFalse(99L, TutorialStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tutorialService.getTutorialResponseById(99L));
        verify(tutorialRepository, never()).incrementViews(any());
    }

    @Test
    void testProjectDelete_NotFound_ThrowsException() {
        ProjectServiceImpl projectService = new ProjectServiceImpl(projectRepository, categoryRepository, userRepository);
        when(projectRepository.existsById(88L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(88L));
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void testRoadmapGetResponseById_NotFound_ThrowsException() {
        RoadmapServiceImpl roadmapService = new RoadmapServiceImpl(roadmapRepository, userRepository);
        when(roadmapRepository.findByIdAndIsDeletedFalse(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roadmapService.getRoadmapResponseById(77L));
    }
}
