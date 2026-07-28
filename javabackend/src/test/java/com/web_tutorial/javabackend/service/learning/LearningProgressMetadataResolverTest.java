package com.web_tutorial.javabackend.service.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;

@ExtendWith(MockitoExtension.class)
public class LearningProgressMetadataResolverTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private DevopsPhaseRepository devopsPhaseRepository;

    @InjectMocks
    private LearningProgressMetadataResolver resolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testResolveMetadata_AllTypes_Deduplication_And_BatchCallOnce() {
        // Prepare progress records
        UserLearningProgress p1 = new UserLearningProgress();
        p1.setContentType(LearningContentType.TUTORIAL);
        p1.setContentKey("tut-1");

        UserLearningProgress p2 = new UserLearningProgress();
        p2.setContentType(LearningContentType.TUTORIAL);
        p2.setContentKey("tut-1"); // Duplicate key

        UserLearningProgress p3 = new UserLearningProgress();
        p3.setContentType(LearningContentType.PROJECT);
        p3.setContentKey("proj space"); // Needs encoding

        UserLearningProgress p4 = new UserLearningProgress();
        p4.setContentType(LearningContentType.ROADMAP);
        p4.setContentKey("road-1");

        UserLearningProgress p5 = new UserLearningProgress();
        p5.setContentType(LearningContentType.DEVOPS_PHASE);
        p5.setContentKey("phase-1");

        UserLearningProgress pOrphan = new UserLearningProgress();
        pOrphan.setContentType(LearningContentType.TUTORIAL);
        pOrphan.setContentKey("orphan-tut");

        // Mocks
        Tutorial tut1 = new Tutorial();
        tut1.setSlug("tut-1");
        tut1.setTitle("Tutorial 1");
        tut1.setCoverImage("img.jpg");
        when(tutorialRepository.findBySlugInAndStatusAndIsDeletedFalse(
                anyCollection(),
                eq(TutorialStatus.PUBLISHED)))
                .thenReturn(List.of(tut1));

        Project proj1 = new Project();
        proj1.setSlug("proj space");
        proj1.setTitle("Project 1");
        proj1.setCoverImage("proj.jpg");
        when(projectRepository.findBySlugInAndIsDeletedFalseAndStatus(anyCollection(), eq(ProjectStatus.PUBLISHED)))
                .thenReturn(List.of(proj1));

        Roadmap road1 = new Roadmap();
        road1.setSlug("road-1");
        road1.setTitle("Roadmap 1");
        road1.setCoverImage("road.jpg");
        when(roadmapRepository.findBySlugInAndIsDeletedFalse(anyCollection()))
                .thenReturn(List.of(road1));

        DevopsPhase phase1 = new DevopsPhase();
        phase1.setPhaseKey("phase-1");
        phase1.setTitle("Phase 1");
        when(devopsPhaseRepository.findByPhaseKeyInAndActiveTrue(anyCollection()))
                .thenReturn(List.of(phase1));

        // Call
        Map<String, LearningProgressMetadataResolver.LearningContentMetadata> metadataMap = resolver.resolveMetadata(List.of(p1, p2, p3, p4, p5, pOrphan));

        // Verify batch calls (max 1 per type)
        ArgumentCaptor<java.util.Collection> tutCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(tutorialRepository, times(1)).findBySlugInAndStatusAndIsDeletedFalse(
                tutCaptor.capture(),
                eq(TutorialStatus.PUBLISHED));
        assertThat(tutCaptor.getValue()).containsExactlyInAnyOrder("tut-1", "orphan-tut"); // Deduplicated!

        verify(projectRepository, times(1)).findBySlugInAndIsDeletedFalseAndStatus(anyCollection(), eq(ProjectStatus.PUBLISHED));
        verify(roadmapRepository, times(1)).findBySlugInAndIsDeletedFalse(anyCollection());
        verify(devopsPhaseRepository, times(1)).findByPhaseKeyInAndActiveTrue(anyCollection());

        // Assert Tutorial
        var tutMeta = metadataMap.get("TUTORIAL:tut-1");
        assertThat(tutMeta.available()).isTrue();
        assertThat(tutMeta.title()).isEqualTo("Tutorial 1");
        assertThat(tutMeta.route()).isEqualTo("/tutorials/tut-1");
        assertThat(tutMeta.thumbnail()).isEqualTo("img.jpg");

        // Assert Project (encoding check)
        var projMeta = metadataMap.get("PROJECT:proj space");
        assertThat(projMeta.available()).isTrue();
        assertThat(projMeta.route()).isEqualTo("/projects/proj%20space");

        // Assert DevOps (null thumbnail check)
        var devopsMeta = metadataMap.get("DEVOPS_PHASE:phase-1");
        assertThat(devopsMeta.available()).isTrue();
        assertThat(devopsMeta.thumbnail()).isNull();

        // Assert Orphan
        var orphanMeta = metadataMap.get("TUTORIAL:orphan-tut");
        assertThat(orphanMeta.available()).isFalse();
        assertThat(orphanMeta.title()).isEqualTo("Nội dung không còn tồn tại");
        assertThat(orphanMeta.route()).isNull();
        assertThat(orphanMeta.thumbnail()).isNull();
    }
}
