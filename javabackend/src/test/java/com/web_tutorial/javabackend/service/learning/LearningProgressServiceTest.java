package com.web_tutorial.javabackend.service.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.web_tutorial.javabackend.domain.dto.request.learning.UpdateLearningProgressRequest;
import com.web_tutorial.javabackend.domain.dto.response.learning.ContinueLearningResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressResponseStatus;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressSummaryResponse;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.learning.UserLearningProgressRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.security.SecurityService;

@ExtendWith(MockitoExtension.class)
public class LearningProgressServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserLearningProgressRepository progressRepository;
    @Mock
    private LearningContentValidator contentValidator;

    @InjectMocks
    private LearningProgressService service;

    private MockedStatic<SecurityService> mockedSecurityService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockedSecurityService = mockStatic(SecurityService.class);
        
        mockUser = new User();
        mockUser.setId(10L);
        mockUser.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        mockedSecurityService.close();
    }

    private void mockAuth(String email) {
        mockedSecurityService.when(SecurityService::getCurrentUserLogin).thenReturn(Optional.ofNullable(email));
        if (email != null) {
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        }
    }

    @Test
    void testAuth_Unauthenticated_ThrowsException() throws Exception {
        mockedSecurityService.when(SecurityService::getCurrentUserLogin).thenReturn(Optional.empty());
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.getMySummary());
    }

    @Test
    void testAuth_UserNotFound_ThrowsException() throws Exception {
        mockedSecurityService.when(SecurityService::getCurrentUserLogin).thenReturn(Optional.of("notfound@example.com"));
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMySummary());
    }

    @Test
    void getMyProgress_Existing_ReturnsMapped() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");

        UserLearningProgress entity = new UserLearningProgress();
        entity.setContentType(LearningContentType.TUTORIAL);
        entity.setContentKey("react");
        entity.setStatus(LearningProgressStatus.COMPLETED);
        entity.setProgressPercent(100);
        entity.setLastAccessedAt(Instant.now());
        entity.setCompletedAt(Instant.now());

        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.of(entity));

        LearningProgressResponse res = service.getMyProgress(LearningContentType.TUTORIAL, "react");
        assertEquals(LearningProgressResponseStatus.COMPLETED, res.getStatus());
        assertEquals(100, res.getProgressPercent());
        assertNotNull(res.getCompletedAt());
    }

    @Test
    void getMyProgress_NotExisting_ReturnsNotStarted() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.empty());

        LearningProgressResponse res = service.getMyProgress(LearningContentType.TUTORIAL, "react");
        assertEquals(LearningProgressResponseStatus.NOT_STARTED, res.getStatus());
        assertEquals(0, res.getProgressPercent());
        assertNull(res.getLastAccessedAt());
        verify(progressRepository, never()).save(any());
    }

    @Test
    void touchMyContent_NotExisting_CreatesInProgress() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.empty());
        
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearningProgressResponse res = service.touchMyContent(LearningContentType.TUTORIAL, "react");
        assertEquals(LearningProgressResponseStatus.IN_PROGRESS, res.getStatus());
        assertEquals(0, res.getProgressPercent());
        assertNotNull(res.getLastAccessedAt());
        assertNull(res.getCompletedAt());
    }

    @Test
    void touchMyContent_Existing_UpdatesAccessedOnly() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        
        UserLearningProgress entity = new UserLearningProgress();
        entity.setStatus(LearningProgressStatus.COMPLETED);
        entity.setProgressPercent(100);
        entity.setCompletedAt(Instant.now());
        
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.of(entity));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearningProgressResponse res = service.touchMyContent(LearningContentType.TUTORIAL, "react");
        assertEquals(LearningProgressResponseStatus.COMPLETED, res.getStatus());
        assertEquals(100, res.getProgressPercent());
        assertNotNull(res.getCompletedAt());
    }

    @Test
    void updateMyProgress_0_To_InProgress() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(0);

        LearningProgressResponse res = service.updateMyProgress(LearningContentType.TUTORIAL, "react", req);
        assertEquals(LearningProgressResponseStatus.IN_PROGRESS, res.getStatus());
        assertEquals(0, res.getProgressPercent());
        assertNull(res.getCompletedAt());
    }

    @Test
    void updateMyProgress_100_To_Completed() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(100);

        LearningProgressResponse res = service.updateMyProgress(LearningContentType.TUTORIAL, "react", req);
        assertEquals(LearningProgressResponseStatus.COMPLETED, res.getStatus());
        assertEquals(100, res.getProgressPercent());
        assertNotNull(res.getCompletedAt());
    }

    @Test
    void updateMyProgress_100_DownTo_50() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        
        UserLearningProgress entity = new UserLearningProgress();
        entity.setStatus(LearningProgressStatus.COMPLETED);
        entity.setProgressPercent(100);
        entity.setCompletedAt(Instant.now());
        
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.of(entity));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(50);

        LearningProgressResponse res = service.updateMyProgress(LearningContentType.TUTORIAL, "react", req);
        assertEquals(LearningProgressResponseStatus.IN_PROGRESS, res.getStatus());
        assertEquals(50, res.getProgressPercent());
        assertNull(res.getCompletedAt());
    }

    @Test
    void updateMyProgress_ValidationFails_SaveNotCalled() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenThrow(new ResourceNotFoundException("Not found"));
        
        UpdateLearningProgressRequest req = new UpdateLearningProgressRequest();
        req.setProgressPercent(50);

        assertThrows(ResourceNotFoundException.class, () -> service.updateMyProgress(LearningContentType.TUTORIAL, "react", req));
        verify(progressRepository, never()).save(any());
    }

    @Test
    void completeMyContent_Existing_SetsTo100() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");
        
        UserLearningProgress entity = new UserLearningProgress();
        entity.setStatus(LearningProgressStatus.IN_PROGRESS);
        entity.setProgressPercent(50);
        
        when(progressRepository.findByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react"))
                .thenReturn(Optional.of(entity));
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearningProgressResponse res = service.completeMyContent(LearningContentType.TUTORIAL, "react");
        assertEquals(LearningProgressResponseStatus.COMPLETED, res.getStatus());
        assertEquals(100, res.getProgressPercent());
        assertNotNull(res.getCompletedAt());
    }

    @Test
    void resetMyProgress_CallsDelete() throws Exception {
        mockAuth("test@example.com");
        when(contentValidator.normalizeAndValidateKey("react")).thenReturn("react");

        service.resetMyProgress(LearningContentType.TUTORIAL, "react");
        
        verify(contentValidator, never()).validateExists(any(), any());
        verify(progressRepository).deleteByUserIdAndContentTypeAndContentKey(10L, LearningContentType.TUTORIAL, "react");
    }

    @Test
    void getMySummary_CalculatesCorrectly() throws Exception {
        mockAuth("test@example.com");
        when(progressRepository.countByUserIdAndStatus(10L, LearningProgressStatus.IN_PROGRESS)).thenReturn(5L);
        when(progressRepository.countByUserIdAndStatus(10L, LearningProgressStatus.COMPLETED)).thenReturn(15L);

        LearningProgressSummaryResponse res = service.getMySummary();
        assertEquals(5, res.getInProgressCount());
        assertEquals(15, res.getCompletedCount());
        assertEquals(20, res.getTotalTracked());
        assertEquals(75.0, res.getCompletionRate());
    }

    @Test
    void getMySummary_TotalZero_RateZero() throws Exception {
        mockAuth("test@example.com");
        when(progressRepository.countByUserIdAndStatus(10L, LearningProgressStatus.IN_PROGRESS)).thenReturn(0L);
        when(progressRepository.countByUserIdAndStatus(10L, LearningProgressStatus.COMPLETED)).thenReturn(0L);

        LearningProgressSummaryResponse res = service.getMySummary();
        assertEquals(0, res.getTotalTracked());
        assertEquals(0.0, res.getCompletionRate());
    }

    @Test
    void getContinueLearning_Found_ReturnsMapped() throws Exception {
        mockAuth("test@example.com");
        
        UserLearningProgress entity = new UserLearningProgress();
        entity.setContentType(LearningContentType.TUTORIAL);
        entity.setContentKey("react");
        entity.setProgressPercent(30);
        entity.setLastAccessedAt(Instant.now());

        when(progressRepository.findTopByUserIdAndStatusOrderByLastAccessedAtDesc(10L, LearningProgressStatus.IN_PROGRESS))
                .thenReturn(Optional.of(entity));

        ContinueLearningResponse res = service.getContinueLearning();
        assertNotNull(res);
        assertEquals(LearningContentType.TUTORIAL, res.getContentType());
        assertEquals("react", res.getContentKey());
        assertEquals(30, res.getProgressPercent());
        assertNotNull(res.getLastAccessedAt());
    }

    @Test
    void getContinueLearning_NotFound_ReturnsNull() throws Exception {
        mockAuth("test@example.com");
        when(progressRepository.findTopByUserIdAndStatusOrderByLastAccessedAtDesc(10L, LearningProgressStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        assertNull(service.getContinueLearning());
    }
}
