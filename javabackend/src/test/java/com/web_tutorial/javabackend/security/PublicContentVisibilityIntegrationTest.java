package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import com.web_tutorial.javabackend.domain.roadmap.Roadmap;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.roadmap.RoadmapRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.service.learning.LearningContentValidator;
import com.web_tutorial.javabackend.service.learning.LearningProgressMetadataResolver;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PublicContentVisibilityIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private DevopsPhaseRepository devopsPhaseRepository;

    @Autowired
    private LearningContentValidator contentValidator;

    @Autowired
    private LearningProgressMetadataResolver metadataResolver;

    @BeforeEach
    void clearContent() {
        tutorialRepository.deleteAll();
        projectRepository.deleteAll();
        roadmapRepository.deleteAll();
        devopsPhaseRepository.deleteAll();
    }

    @Test
    void tutorialPublicReadsOnlyExposePublishedNonDeletedContent() throws Exception {
        Tutorial published = tutorialRepository.saveAndFlush(tutorial("tutorial-public", TutorialStatus.PUBLISHED, false));
        Tutorial draft = tutorialRepository.saveAndFlush(tutorial("tutorial-draft", TutorialStatus.DRAFT, false));
        Tutorial archived = tutorialRepository.saveAndFlush(tutorial("tutorial-archived", TutorialStatus.ARCHIVED, false));
        Tutorial deleted = tutorialRepository.saveAndFlush(tutorial("tutorial-deleted", TutorialStatus.PUBLISHED, true));
        tutorialRepository.saveAndFlush(tutorial("tutorial-null-status", null, false));

        String listBody = mockMvc.perform(get("/api/v1/tutorials").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody)
                .contains("tutorial-public")
                .doesNotContain("tutorial-draft", "tutorial-archived", "tutorial-deleted", "tutorial-null-status");
        assertPublicDetailVisibility("/api/v1/tutorials", "/api/v1/tutorials/slug", published, draft, deleted);

        mockMvc.perform(get("/api/v1/tutorials/admin")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tutorial-draft")));
        mockMvc.perform(get("/api/v1/tutorials/admin/{id}", draft.getId())
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tutorial-draft")));
    }

    @Test
    void projectPublicReadsOnlyExposePublishedNonDeletedContent() throws Exception {
        Project published = projectRepository.saveAndFlush(project("project-public", ProjectStatus.PUBLISHED, false));
        Project draft = projectRepository.saveAndFlush(project("project-draft", ProjectStatus.DRAFT, false));
        projectRepository.saveAndFlush(project("project-archived", ProjectStatus.ARCHIVED, false));
        Project deleted = projectRepository.saveAndFlush(project("project-deleted", ProjectStatus.PUBLISHED, true));
        projectRepository.saveAndFlush(project("project-null-status", null, false));

        String listBody = mockMvc.perform(get("/api/v1/projects").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody)
                .contains("project-public")
                .doesNotContain("project-draft", "project-archived", "project-deleted", "project-null-status");
        assertPublicDetailVisibility("/api/v1/projects", "/api/v1/projects/slug", published, draft, deleted);

        mockMvc.perform(get("/api/v1/projects/admin")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("project-draft")));
        mockMvc.perform(get("/api/v1/projects/admin/{id}", draft.getId())
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void roadmapPublicReadsExcludeDeletedContentWithoutInventingPublicationStatus() throws Exception {
        Roadmap visible = roadmapRepository.saveAndFlush(roadmap("roadmap-public", false));
        Roadmap deleted = roadmapRepository.saveAndFlush(roadmap("roadmap-deleted", true));

        String listBody = mockMvc.perform(get("/api/v1/roadmaps").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody).contains("roadmap-public").doesNotContain("roadmap-deleted");
        mockMvc.perform(get("/api/v1/roadmaps/{id}", visible.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/roadmaps/slug/{slug}", visible.getSlug())).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/roadmaps/{id}", deleted.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/roadmaps/slug/{slug}", deleted.getSlug())).andExpect(status().isNotFound());
    }

    @Test
    void devopsPublicReadsOnlyExposeActivePhasesWhileAdminStillSeesInactive() throws Exception {
        DevopsPhase active = devopsPhaseRepository.saveAndFlush(phase("active-phase", true, 1));
        DevopsPhase inactive = devopsPhaseRepository.saveAndFlush(phase("inactive-phase", false, 2));

        String listBody = mockMvc.perform(get("/api/v1/devops/phases"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody).contains(active.getPhaseKey()).doesNotContain(inactive.getPhaseKey());
        mockMvc.perform(get("/api/v1/devops/phases/{phaseKey}", active.getPhaseKey()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/devops/phases/{phaseKey}", inactive.getPhaseKey()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/devops/admin/phases")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(inactive.getPhaseKey())));
    }

    @Test
    void adminReadEndpointsEnforceAnonymousUserAdminMatrix() throws Exception {
        Tutorial tutorial = tutorialRepository.saveAndFlush(
                tutorial("admin-security-tutorial", TutorialStatus.DRAFT, false));
        Project project = projectRepository.saveAndFlush(
                project("admin-security-project", ProjectStatus.DRAFT, false));
        Roadmap roadmap = roadmapRepository.saveAndFlush(roadmap("admin-security-roadmap", false));

        assertAdminSecurity("/api/v1/tutorials/admin");
        assertAdminSecurity("/api/v1/tutorials/admin/" + tutorial.getId());
        assertAdminSecurity("/api/v1/projects/admin");
        assertAdminSecurity("/api/v1/projects/admin/" + project.getId());
        assertAdminSecurity("/api/v1/roadmaps/admin");
        assertAdminSecurity("/api/v1/roadmaps/admin/" + roadmap.getId());
    }

    @Test
    void learningProgressPolicyRejectsNonPublicContentAndMarksExistingProgressUnavailable() throws Exception {
        tutorialRepository.saveAndFlush(tutorial("learning-public", TutorialStatus.PUBLISHED, false));
        tutorialRepository.saveAndFlush(tutorial("learning-draft", TutorialStatus.DRAFT, false));
        projectRepository.saveAndFlush(project("learning-deleted-project", ProjectStatus.PUBLISHED, true));
        devopsPhaseRepository.saveAndFlush(phase("learning-inactive-phase", false, 1));

        contentValidator.validateExists(LearningContentType.TUTORIAL, "learning-public");
        assertThrows(
                ResourceNotFoundException.class,
                () -> contentValidator.validateExists(LearningContentType.TUTORIAL, "learning-draft"));
        assertThrows(
                ResourceNotFoundException.class,
                () -> contentValidator.validateExists(LearningContentType.PROJECT, "learning-deleted-project"));
        assertThrows(
                ResourceNotFoundException.class,
                () -> contentValidator.validateExists(LearningContentType.DEVOPS_PHASE, "learning-inactive-phase"));

        UserLearningProgress publishedProgress = progress(LearningContentType.TUTORIAL, "learning-public");
        UserLearningProgress draftProgress = progress(LearningContentType.TUTORIAL, "learning-draft");
        Map<String, LearningProgressMetadataResolver.LearningContentMetadata> metadata =
                metadataResolver.resolveMetadata(List.of(publishedProgress, draftProgress));

        assertThat(metadata.get("TUTORIAL:learning-public").available()).isTrue();
        assertThat(metadata.get("TUTORIAL:learning-draft").available()).isFalse();
        assertThat(metadata.get("TUTORIAL:learning-draft").route()).isNull();
    }

    private void assertPublicDetailVisibility(
            String idPath,
            String slugPath,
            Object published,
            Object draft,
            Object deleted) throws Exception {
        Long publishedId = idOf(published);
        Long draftId = idOf(draft);
        Long deletedId = idOf(deleted);
        String publishedSlug = slugOf(published);
        String draftSlug = slugOf(draft);
        String deletedSlug = slugOf(deleted);

        mockMvc.perform(get(idPath + "/{id}", publishedId)).andExpect(status().isOk());
        mockMvc.perform(get(slugPath + "/{slug}", publishedSlug)).andExpect(status().isOk());
        mockMvc.perform(get(idPath + "/{id}", draftId)).andExpect(status().isNotFound());
        mockMvc.perform(get(slugPath + "/{slug}", draftSlug)).andExpect(status().isNotFound());
        mockMvc.perform(get(idPath + "/{id}", deletedId)).andExpect(status().isNotFound());
        mockMvc.perform(get(slugPath + "/{slug}", deletedSlug)).andExpect(status().isNotFound());
    }

    private void assertAdminSecurity(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(path).with(user("reader").authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(path).with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    private Long idOf(Object content) {
        if (content instanceof Tutorial tutorial) {
            return tutorial.getId();
        }
        return ((Project) content).getId();
    }

    private String slugOf(Object content) {
        if (content instanceof Tutorial tutorial) {
            return tutorial.getSlug();
        }
        return ((Project) content).getSlug();
    }

    private Tutorial tutorial(String slug, TutorialStatus status, boolean deleted) {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(slug);
        tutorial.setSlug(slug);
        tutorial.setContent("content");
        tutorial.setViews(0L);
        tutorial.setStatus(status);
        tutorial.setDeleted(deleted);
        return tutorial;
    }

    private Project project(String slug, ProjectStatus status, boolean deleted) {
        Project project = new Project();
        project.setTitle(slug);
        project.setSlug(slug);
        project.setContent("content");
        project.setViews(0L);
        project.setStatus(status);
        project.setDeleted(deleted);
        return project;
    }

    private Roadmap roadmap(String slug, boolean deleted) {
        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(slug);
        roadmap.setSlug(slug);
        roadmap.setDeleted(deleted);
        return roadmap;
    }

    private DevopsPhase phase(String phaseKey, boolean active, int displayOrder) {
        DevopsPhase phase = new DevopsPhase();
        phase.setPhaseKey(phaseKey);
        phase.setTitle(phaseKey);
        phase.setDisplayOrder(displayOrder);
        phase.setActive(active);
        return phase;
    }

    private UserLearningProgress progress(LearningContentType type, String key) {
        UserLearningProgress progress = new UserLearningProgress();
        progress.setContentType(type);
        progress.setContentKey(key);
        return progress;
    }
}
