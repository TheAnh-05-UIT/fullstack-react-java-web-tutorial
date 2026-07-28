package com.web_tutorial.javabackend.service.learning;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

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

@Service
public class LearningProgressMetadataResolver {

    private final TutorialRepository tutorialRepository;
    private final ProjectRepository projectRepository;
    private final RoadmapRepository roadmapRepository;
    private final DevopsPhaseRepository devopsPhaseRepository;

    public LearningProgressMetadataResolver(
            TutorialRepository tutorialRepository,
            ProjectRepository projectRepository,
            RoadmapRepository roadmapRepository,
            DevopsPhaseRepository devopsPhaseRepository) {
        this.tutorialRepository = tutorialRepository;
        this.projectRepository = projectRepository;
        this.roadmapRepository = roadmapRepository;
        this.devopsPhaseRepository = devopsPhaseRepository;
    }

    public static record LearningContentMetadata(
            String title,
            String route,
            String thumbnail,
            boolean available
    ) {}

    public Map<String, LearningContentMetadata> resolveMetadata(List<UserLearningProgress> pageContent) {
        Map<String, LearningContentMetadata> metadataMap = new HashMap<>();

        Map<LearningContentType, Set<String>> keysByType = pageContent.stream()
                .filter(p -> StringUtils.hasText(p.getContentKey()))
                .collect(Collectors.groupingBy(
                        UserLearningProgress::getContentType,
                        Collectors.mapping(p -> p.getContentKey().trim(), Collectors.toSet())
                ));

        for (Map.Entry<LearningContentType, Set<String>> entry : keysByType.entrySet()) {
            LearningContentType type = entry.getKey();
            Set<String> keys = entry.getValue();
            if (keys.isEmpty()) continue;

            switch (type) {
                case TUTORIAL -> resolveTutorials(keys, metadataMap);
                case PROJECT -> resolveProjects(keys, metadataMap);
                case ROADMAP -> resolveRoadmaps(keys, metadataMap);
                case DEVOPS_PHASE -> resolveDevopsPhases(keys, metadataMap);
            }
        }

        return metadataMap;
    }

    private void resolveTutorials(Set<String> keys, Map<String, LearningContentMetadata> metadataMap) {
        List<Tutorial> tutorials = tutorialRepository.findBySlugInAndStatusAndIsDeletedFalse(
                keys,
                TutorialStatus.PUBLISHED);
        Map<String, Tutorial> tutorialMap = tutorials.stream()
                .collect(Collectors.toMap(Tutorial::getSlug, t -> t, (t1, t2) -> t1));

        for (String key : keys) {
            Tutorial tutorial = tutorialMap.get(key);
            String mapKey = LearningContentType.TUTORIAL.name() + ":" + key;
            if (tutorial != null) {
                metadataMap.put(mapKey, new LearningContentMetadata(
                        tutorial.getTitle(),
                        "/tutorials/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8),
                        tutorial.getCoverImage(),
                        true
                ));
            } else {
                metadataMap.put(mapKey, getOrphanMetadata());
            }
        }
    }

    private void resolveProjects(Set<String> keys, Map<String, LearningContentMetadata> metadataMap) {
        List<Project> projects = projectRepository.findBySlugInAndIsDeletedFalseAndStatus(keys, ProjectStatus.PUBLISHED);
        Map<String, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getSlug, p -> p, (p1, p2) -> p1));

        for (String key : keys) {
            Project project = projectMap.get(key);
            String mapKey = LearningContentType.PROJECT.name() + ":" + key;
            if (project != null) {
                metadataMap.put(mapKey, new LearningContentMetadata(
                        project.getTitle(),
                        "/projects/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8),
                        project.getCoverImage(),
                        true
                ));
            } else {
                metadataMap.put(mapKey, getOrphanMetadata());
            }
        }
    }

    private void resolveRoadmaps(Set<String> keys, Map<String, LearningContentMetadata> metadataMap) {
        List<Roadmap> roadmaps = roadmapRepository.findBySlugInAndIsDeletedFalse(keys);
        Map<String, Roadmap> roadmapMap = roadmaps.stream()
                .collect(Collectors.toMap(Roadmap::getSlug, r -> r, (r1, r2) -> r1));

        for (String key : keys) {
            Roadmap roadmap = roadmapMap.get(key);
            String mapKey = LearningContentType.ROADMAP.name() + ":" + key;
            if (roadmap != null) {
                metadataMap.put(mapKey, new LearningContentMetadata(
                        roadmap.getTitle(),
                        "/roadmaps/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8),
                        roadmap.getCoverImage(),
                        true
                ));
            } else {
                metadataMap.put(mapKey, getOrphanMetadata());
            }
        }
    }

    private void resolveDevopsPhases(Set<String> keys, Map<String, LearningContentMetadata> metadataMap) {
        List<DevopsPhase> phases = devopsPhaseRepository.findByPhaseKeyInAndActiveTrue(keys);
        Map<String, DevopsPhase> phaseMap = phases.stream()
                .collect(Collectors.toMap(DevopsPhase::getPhaseKey, p -> p, (p1, p2) -> p1));

        for (String key : keys) {
            DevopsPhase phase = phaseMap.get(key);
            String mapKey = LearningContentType.DEVOPS_PHASE.name() + ":" + key;
            if (phase != null) {
                metadataMap.put(mapKey, new LearningContentMetadata(
                        phase.getTitle(),
                        "/devops/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8),
                        null,
                        true
                ));
            } else {
                metadataMap.put(mapKey, getOrphanMetadata());
            }
        }
    }

    private LearningContentMetadata getOrphanMetadata() {
        return new LearningContentMetadata(
                "Nội dung không còn tồn tại",
                null,
                null,
                false
        );
    }
}
