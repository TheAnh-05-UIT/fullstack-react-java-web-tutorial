package com.web_tutorial.javabackend.controller.learning;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.domain.dto.request.learning.UpdateLearningProgressRequest;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.repository.learning.UserLearningProgressRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LearningProgressControllerTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLearningProgressRepository progressRepository;

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private Tutorial tutorial;

    @BeforeEach
    void setUp() {
        progressRepository.deleteAll();
        tutorialRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setEmail("user-a@example.com");
        userA.setUsername("usera");
        userA.setPassword("encoded");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setEmail("user-b@example.com");
        userB.setUsername("userb");
        userB.setPassword("encoded");
        userB = userRepository.save(userB);

        tutorial = new Tutorial();
        tutorial.setTitle("Test Tutorial");
        tutorial.setSlug("test-tutorial");
        tutorial.setDescription("Desc");
        tutorial.setStatus(TutorialStatus.PUBLISHED);
        tutorial.setDeleted(false);
        tutorial = tutorialRepository.save(tutorial);
    }

    @AfterEach
    void tearDown() {
        progressRepository.deleteAll();
        tutorialRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Authentication Tests ---

    @Test
    void whenAnonymousGetSummary_then401() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAnonymousGetProgress_then401() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/TUTORIAL/test-tutorial"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAnonymousGetMyProgressPage_then401() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAnonymousPutProgress_then401() throws Exception {
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(50);
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAnonymousDeleteProgress_then401() throws Exception {
        mockMvc.perform(delete("/api/v1/learning-progress/me/TUTORIAL/test-tutorial"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user-a@example.com", roles = "USER")
    void whenUserGetSummary_then200() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/summary"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user-a@example.com", roles = "ADMIN")
    void whenAdminGetSummary_then200() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/summary"))
                .andExpect(status().isOk());
    }

    // --- GET Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenContentExistsButNoProgress_then200NotStarted() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/TUTORIAL/test-tutorial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("NOT_STARTED")))
                .andExpect(jsonPath("$.data.progressPercent", is(0)));
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenContentDoesNotExist_then404() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/TUTORIAL/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenInvalidContentType_then400() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/INVALID/test-tutorial"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Giá trị của tham số contentType không hợp lệ.")));
    }

    // --- Touch Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenTouchNew_thenInProgressZero() throws Exception {
        mockMvc.perform(post("/api/v1/learning-progress/me/TUTORIAL/test-tutorial/touch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.progressPercent", is(0)));
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenTouchDraftContent_then404AndDoesNotCreateProgress() throws Exception {
        tutorial.setStatus(TutorialStatus.DRAFT);
        tutorialRepository.saveAndFlush(tutorial);

        mockMvc.perform(post("/api/v1/learning-progress/me/TUTORIAL/test-tutorial/touch"))
                .andExpect(status().isNotFound());

        assertEquals(0, progressRepository.count());
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenTouchCompleted_thenDoesNotReset() throws Exception {
        UserLearningProgress p = new UserLearningProgress();
        p.setUser(userA);
        p.setContentType(LearningContentType.TUTORIAL);
        p.setContentKey("test-tutorial");
        p.setStatus(LearningProgressStatus.COMPLETED);
        p.setProgressPercent(100);
        progressRepository.save(p);

        mockMvc.perform(post("/api/v1/learning-progress/me/TUTORIAL/test-tutorial/touch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.progressPercent", is(100)));
    }

    // --- Update Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenUpdateValid_thenSuccess() throws Exception {
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(50);
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.progressPercent", is(50)));
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenUpdateTo100_thenCompleted() throws Exception {
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(100);
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.progressPercent", is(100)));
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenUpdatePercentNull_then400() throws Exception {
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenUpdatePercentNegative_then400() throws Exception {
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(-1);
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenUpdatePercentTooHigh_then400() throws Exception {
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(101);
        mockMvc.perform(put("/api/v1/learning-progress/me/TUTORIAL/test-tutorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // --- Complete Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenCompleteFirstTime_then200() throws Exception {
        mockMvc.perform(post("/api/v1/learning-progress/me/TUTORIAL/test-tutorial/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.progressPercent", is(100)));
    }

    // --- Reset Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenReset_then204() throws Exception {
        UserLearningProgress p = new UserLearningProgress();
        p.setUser(userA);
        p.setContentType(LearningContentType.TUTORIAL);
        p.setContentKey("test-tutorial");
        p.setStatus(LearningProgressStatus.COMPLETED);
        p.setProgressPercent(100);
        progressRepository.save(p);

        mockMvc.perform(delete("/api/v1/learning-progress/me/TUTORIAL/test-tutorial"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist()); // body should be empty, testing 204 behavior
    }

    // --- Summary Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenSummaryEmpty_thenAllZeros() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTracked", is(0)))
                .andExpect(jsonPath("$.data.completionRate", is(0.0)));
    }

    // --- Continue Tests ---

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenContinueEmpty_then200DataNull() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/continue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "user-a@example.com")
    void whenAuthenticatedGetMyProgressPage_then200() throws Exception {
        // Just test that the endpoint responds properly with 200 and paginated structure
        mockMvc.perform(get("/api/v1/learning-progress/me")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(10)))
                .andExpect(jsonPath("$.data.totalElements", is(0)));
    }

    // --- IDOR Tests ---

    @Test
    @WithMockUser(username = "user-b@example.com") // Using user B
    void whenUserBGetsContentTouchedByUserA_thenNotStarted() throws Exception {
        // User A has progress
        UserLearningProgress p = new UserLearningProgress();
        p.setUser(userA);
        p.setContentType(LearningContentType.TUTORIAL);
        p.setContentKey("test-tutorial");
        p.setStatus(LearningProgressStatus.IN_PROGRESS);
        p.setProgressPercent(50);
        progressRepository.save(p);

        // User B gets progress - should not see A's progress
        mockMvc.perform(get("/api/v1/learning-progress/me/TUTORIAL/test-tutorial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("NOT_STARTED")))
                .andExpect(jsonPath("$.data.progressPercent", is(0)));
    }
}
