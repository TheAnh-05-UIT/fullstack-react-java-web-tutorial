package com.web_tutorial.javabackend.repository.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserLearningProgressRepositoryTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private UserLearningProgressRepository progressRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("testlearning@example.com");
        testUser.setPassword("password");
        testUser.setUsername("testlearning");
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    void testSaveAndFindByUserIdAndContentTypeAndContentKey() {
        UserLearningProgress progress = new UserLearningProgress();
        progress.setUser(testUser);
        progress.setContentType(LearningContentType.TUTORIAL);
        progress.setContentKey("react-hooks");
        progress.setStatus(LearningProgressStatus.IN_PROGRESS);
        progress.setProgressPercent(50);
        progress.setLastAccessedAt(Instant.now());

        progressRepository.saveAndFlush(progress);

        Optional<UserLearningProgress> found = progressRepository.findByUserIdAndContentTypeAndContentKey(
                testUser.getId(), LearningContentType.TUTORIAL, "react-hooks");

        assertThat(found).isPresent();
        assertThat(found.get().getProgressPercent()).isEqualTo(50);
        assertThat(found.get().getStatus()).isEqualTo(LearningProgressStatus.IN_PROGRESS);
        
        // Ensure enums are mapped as strings in DB (by checking the native query if possible, or just trusting JPA)
        // Spring Data JPA uses EnumType.STRING as specified in the entity.
        // Also verify createdAt is populated
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void testUniqueConstraintOnUserTypeAndKey() {
        UserLearningProgress progress1 = new UserLearningProgress();
        progress1.setUser(testUser);
        progress1.setContentType(LearningContentType.DEVOPS_PHASE);
        progress1.setContentKey("planning");
        progress1.setStatus(LearningProgressStatus.IN_PROGRESS);
        progress1.setProgressPercent(10);
        progressRepository.saveAndFlush(progress1);

        UserLearningProgress progress2 = new UserLearningProgress();
        progress2.setUser(testUser);
        progress2.setContentType(LearningContentType.DEVOPS_PHASE);
        progress2.setContentKey("planning");
        progress2.setStatus(LearningProgressStatus.COMPLETED);
        progress2.setProgressPercent(100);

        assertThrows(DataIntegrityViolationException.class, () -> {
            progressRepository.saveAndFlush(progress2);
        });
    }

    @Test
    void testFindByUserIdOrderByLastAccessedAtDesc() {
        Instant now = Instant.now();

        UserLearningProgress p1 = new UserLearningProgress();
        p1.setUser(testUser);
        p1.setContentType(LearningContentType.PROJECT);
        p1.setContentKey("proj-1");
        p1.setStatus(LearningProgressStatus.IN_PROGRESS);
        p1.setProgressPercent(20);
        p1.setLastAccessedAt(now.minus(2, ChronoUnit.DAYS));
        progressRepository.save(p1);

        UserLearningProgress p2 = new UserLearningProgress();
        p2.setUser(testUser);
        p2.setContentType(LearningContentType.TUTORIAL);
        p2.setContentKey("tut-1");
        p2.setStatus(LearningProgressStatus.IN_PROGRESS);
        p2.setProgressPercent(80);
        p2.setLastAccessedAt(now.minus(1, ChronoUnit.DAYS));
        progressRepository.save(p2);

        progressRepository.flush();

        Page<UserLearningProgress> page = progressRepository.findByUserIdOrderByLastAccessedAtDesc(testUser.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        // p2 was accessed more recently (minus 1 day vs minus 2 days)
        assertThat(page.getContent().get(0).getContentKey()).isEqualTo("tut-1");
        assertThat(page.getContent().get(1).getContentKey()).isEqualTo("proj-1");
    }

    @Test
    void testFindTopByUserIdAndStatusOrderByLastAccessedAtDesc() {
        Instant now = Instant.now();

        UserLearningProgress p1 = new UserLearningProgress();
        p1.setUser(testUser);
        p1.setContentType(LearningContentType.ROADMAP);
        p1.setContentKey("road-1");
        p1.setStatus(LearningProgressStatus.COMPLETED);
        p1.setProgressPercent(100);
        p1.setLastAccessedAt(now.minus(5, ChronoUnit.MINUTES));
        progressRepository.save(p1);

        UserLearningProgress p2 = new UserLearningProgress();
        p2.setUser(testUser);
        p2.setContentType(LearningContentType.ROADMAP);
        p2.setContentKey("road-2");
        p2.setStatus(LearningProgressStatus.IN_PROGRESS);
        p2.setProgressPercent(50);
        p2.setLastAccessedAt(now.minus(1, ChronoUnit.MINUTES)); // Most recent
        progressRepository.save(p2);

        progressRepository.flush();

        Optional<UserLearningProgress> found = progressRepository.findTopByUserIdAndStatusOrderByLastAccessedAtDesc(
                testUser.getId(), LearningProgressStatus.IN_PROGRESS);

        assertThat(found).isPresent();
        assertThat(found.get().getContentKey()).isEqualTo("road-2");
    }

    @Test
    void testCountByUserIdAndStatus() {
        UserLearningProgress p1 = new UserLearningProgress();
        p1.setUser(testUser);
        p1.setContentType(LearningContentType.TUTORIAL);
        p1.setContentKey("tut-1");
        p1.setStatus(LearningProgressStatus.COMPLETED);
        p1.setProgressPercent(100);
        progressRepository.save(p1);

        progressRepository.flush();

        long completedCount = progressRepository.countByUserIdAndStatus(testUser.getId(), LearningProgressStatus.COMPLETED);
        long inProgressCount = progressRepository.countByUserIdAndStatus(testUser.getId(), LearningProgressStatus.IN_PROGRESS);

        assertThat(completedCount).isEqualTo(1);
        assertThat(inProgressCount).isEqualTo(0);
    }

    @Test
    void testFindMyProgress() {
        Instant now = Instant.now();

        UserLearningProgress p1 = new UserLearningProgress();
        p1.setUser(testUser);
        p1.setContentType(LearningContentType.TUTORIAL);
        p1.setContentKey("tut-my-1");
        p1.setStatus(LearningProgressStatus.IN_PROGRESS);
        p1.setProgressPercent(20);
        p1.setLastAccessedAt(now.minus(2, ChronoUnit.DAYS));
        progressRepository.save(p1);

        UserLearningProgress p2 = new UserLearningProgress();
        p2.setUser(testUser);
        p2.setContentType(LearningContentType.PROJECT);
        p2.setContentKey("proj-my-1");
        p2.setStatus(LearningProgressStatus.COMPLETED);
        p2.setProgressPercent(100);
        p2.setLastAccessedAt(now.minus(1, ChronoUnit.DAYS));
        progressRepository.save(p2);

        progressRepository.flush();

        // 1. Without filters
        Page<UserLearningProgress> page1 = progressRepository.findMyProgress(
                testUser.getId(), null, null, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("lastAccessedAt"), org.springframework.data.domain.Sort.Order.desc("id"))));
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getContent().get(0).getContentKey()).isEqualTo("proj-my-1");

        // 2. With status filter
        Page<UserLearningProgress> page2 = progressRepository.findMyProgress(
                testUser.getId(), LearningProgressStatus.IN_PROGRESS, null, PageRequest.of(0, 10));
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.getContent().get(0).getContentKey()).isEqualTo("tut-my-1");

        // 3. With contentType filter
        Page<UserLearningProgress> page3 = progressRepository.findMyProgress(
                testUser.getId(), null, LearningContentType.PROJECT, PageRequest.of(0, 10));
        assertThat(page3.getContent()).hasSize(1);
        assertThat(page3.getContent().get(0).getContentKey()).isEqualTo("proj-my-1");

        // 4. With both filters
        Page<UserLearningProgress> page4 = progressRepository.findMyProgress(
                testUser.getId(), LearningProgressStatus.IN_PROGRESS, LearningContentType.TUTORIAL, PageRequest.of(0, 10));
        assertThat(page4.getContent()).hasSize(1);
    }
}
