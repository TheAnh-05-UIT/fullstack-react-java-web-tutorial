package com.web_tutorial.javabackend.service.learning;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.dto.request.learning.UpdateLearningProgressRequest;
import com.web_tutorial.javabackend.domain.dto.response.learning.ContinueLearningResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressResponseStatus;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressSummaryResponse;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.learning.UserLearningProgressRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.security.SecurityService;

@Service
public class LearningProgressService {

    private final UserRepository userRepository;
    private final UserLearningProgressRepository progressRepository;
    private final LearningContentValidator contentValidator;

    public LearningProgressService(
            UserRepository userRepository,
            UserLearningProgressRepository progressRepository,
            LearningContentValidator contentValidator) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.contentValidator = contentValidator;
    }

    private User getCurrentUser() {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("User not authenticated"));
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public LearningProgressResponse getMyProgress(LearningContentType contentType, String contentKey) throws IdInvalidException {
        User user = getCurrentUser();
        String normalizedKey = contentValidator.normalizeAndValidateKey(contentKey);
        contentValidator.validateExists(contentType, normalizedKey);

        Optional<UserLearningProgress> progressOpt = progressRepository.findByUserIdAndContentTypeAndContentKey(
                user.getId(), contentType, normalizedKey);

        return progressOpt.map(this::mapToResponse)
                .orElseGet(() -> buildNotStartedResponse(contentType, normalizedKey));
    }

    @Transactional
    public LearningProgressResponse touchMyContent(LearningContentType contentType, String contentKey) throws IdInvalidException {
        User user = getCurrentUser();
        String normalizedKey = contentValidator.normalizeAndValidateKey(contentKey);
        contentValidator.validateExists(contentType, normalizedKey);

        Optional<UserLearningProgress> progressOpt = progressRepository.findByUserIdAndContentTypeAndContentKey(
                user.getId(), contentType, normalizedKey);

        UserLearningProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            progress.setLastAccessedAt(Instant.now());
            // percent and status remain unchanged, completedAt remains unchanged
        } else {
            progress = new UserLearningProgress();
            progress.setUser(user);
            progress.setContentType(contentType);
            progress.setContentKey(normalizedKey);
            progress.setStatus(LearningProgressStatus.IN_PROGRESS);
            progress.setProgressPercent(0);
            progress.setLastAccessedAt(Instant.now());
            progress.setCompletedAt(null);
        }

        progress = progressRepository.save(progress);
        return mapToResponse(progress);
    }

    @Transactional
    public LearningProgressResponse updateMyProgress(LearningContentType contentType, String contentKey, UpdateLearningProgressRequest request) throws IdInvalidException {
        User user = getCurrentUser();
        String normalizedKey = contentValidator.normalizeAndValidateKey(contentKey);
        contentValidator.validateExists(contentType, normalizedKey);

        int percent = request.getProgressPercent();

        Optional<UserLearningProgress> progressOpt = progressRepository.findByUserIdAndContentTypeAndContentKey(
                user.getId(), contentType, normalizedKey);

        UserLearningProgress progress = progressOpt.orElseGet(() -> {
            UserLearningProgress newProgress = new UserLearningProgress();
            newProgress.setUser(user);
            newProgress.setContentType(contentType);
            newProgress.setContentKey(normalizedKey);
            return newProgress;
        });

        progress.setProgressPercent(percent);
        progress.setLastAccessedAt(Instant.now());

        if (percent == 100) {
            progress.setStatus(LearningProgressStatus.COMPLETED);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(Instant.now());
            }
        } else {
            progress.setStatus(LearningProgressStatus.IN_PROGRESS);
            progress.setCompletedAt(null);
        }

        progress = progressRepository.save(progress);
        return mapToResponse(progress);
    }

    @Transactional
    public LearningProgressResponse completeMyContent(LearningContentType contentType, String contentKey) throws IdInvalidException {
        User user = getCurrentUser();
        String normalizedKey = contentValidator.normalizeAndValidateKey(contentKey);
        contentValidator.validateExists(contentType, normalizedKey);

        Optional<UserLearningProgress> progressOpt = progressRepository.findByUserIdAndContentTypeAndContentKey(
                user.getId(), contentType, normalizedKey);

        UserLearningProgress progress = progressOpt.orElseGet(() -> {
            UserLearningProgress newProgress = new UserLearningProgress();
            newProgress.setUser(user);
            newProgress.setContentType(contentType);
            newProgress.setContentKey(normalizedKey);
            return newProgress;
        });

        progress.setStatus(LearningProgressStatus.COMPLETED);
        progress.setProgressPercent(100);
        progress.setLastAccessedAt(Instant.now());
        if (progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        progress = progressRepository.save(progress);
        return mapToResponse(progress);
    }

    @Transactional
    public void resetMyProgress(LearningContentType contentType, String contentKey) throws IdInvalidException {
        User user = getCurrentUser();
        // Do not call validateExists, but still normalize
        String normalizedKey = contentValidator.normalizeAndValidateKey(contentKey);
        
        progressRepository.deleteByUserIdAndContentTypeAndContentKey(user.getId(), contentType, normalizedKey);
    }

    @Transactional(readOnly = true)
    public LearningProgressSummaryResponse getMySummary() {
        User user = getCurrentUser();

        long inProgress = progressRepository.countByUserIdAndStatus(user.getId(), LearningProgressStatus.IN_PROGRESS);
        long completed = progressRepository.countByUserIdAndStatus(user.getId(), LearningProgressStatus.COMPLETED);
        long totalTracked = inProgress + completed;

        double completionRate = totalTracked == 0 ? 0.0 : (completed * 100.0 / totalTracked);

        LearningProgressSummaryResponse summary = new LearningProgressSummaryResponse();
        summary.setInProgressCount(inProgress);
        summary.setCompletedCount(completed);
        summary.setTotalTracked(totalTracked);
        summary.setCompletionRate(completionRate);
        return summary;
    }

    @Transactional(readOnly = true)
    public ContinueLearningResponse getContinueLearning() {
        User user = getCurrentUser();

        Optional<UserLearningProgress> progressOpt = progressRepository.findTopByUserIdAndStatusOrderByLastAccessedAtDesc(
                user.getId(), LearningProgressStatus.IN_PROGRESS);

        if (progressOpt.isEmpty()) {
            return null;
        }

        UserLearningProgress progress = progressOpt.get();
        ContinueLearningResponse response = new ContinueLearningResponse();
        response.setContentType(progress.getContentType());
        response.setContentKey(progress.getContentKey());
        response.setProgressPercent(progress.getProgressPercent());
        response.setLastAccessedAt(progress.getLastAccessedAt());
        // Title, route, thumbnail are left null for LP-3
        return response;
    }

    private LearningProgressResponse mapToResponse(UserLearningProgress entity) {
        LearningProgressResponse response = new LearningProgressResponse();
        response.setContentType(entity.getContentType());
        response.setContentKey(entity.getContentKey());
        response.setProgressPercent(entity.getProgressPercent());
        response.setLastAccessedAt(entity.getLastAccessedAt());
        response.setCompletedAt(entity.getCompletedAt());

        if (entity.getStatus() == LearningProgressStatus.COMPLETED) {
            response.setStatus(LearningProgressResponseStatus.COMPLETED);
        } else {
            response.setStatus(LearningProgressResponseStatus.IN_PROGRESS);
        }

        return response;
    }

    private LearningProgressResponse buildNotStartedResponse(LearningContentType contentType, String normalizedKey) {
        LearningProgressResponse response = new LearningProgressResponse();
        response.setContentType(contentType);
        response.setContentKey(normalizedKey);
        response.setStatus(LearningProgressResponseStatus.NOT_STARTED);
        response.setProgressPercent(0);
        response.setLastAccessedAt(null);
        response.setCompletedAt(null);
        return response;
    }
}
