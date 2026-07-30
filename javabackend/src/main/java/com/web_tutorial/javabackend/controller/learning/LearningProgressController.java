package com.web_tutorial.javabackend.controller.learning;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.learning.UpdateLearningProgressRequest;
import com.web_tutorial.javabackend.domain.dto.response.learning.ContinueLearningResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressPageResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressResponse;
import com.web_tutorial.javabackend.domain.dto.response.learning.LearningProgressSummaryResponse;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.service.learning.LearningProgressService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/learning-progress/me")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    public LearningProgressController(LearningProgressService learningProgressService) {
        this.learningProgressService = learningProgressService;
    }

    @GetMapping("/summary")
    @ApiMessage("Get learning progress summary")
    public ResponseEntity<LearningProgressSummaryResponse> getMySummary() {
        return ResponseEntity.ok(learningProgressService.getMySummary());
    }

    @GetMapping("/continue")
    @ApiMessage("Get continue learning recommendation")
    public ResponseEntity<ContinueLearningResponse> getContinueLearning() {
        ContinueLearningResponse response = learningProgressService.getContinueLearning();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @ApiMessage("Get my learning progress")
    public ResponseEntity<LearningProgressPageResponse> getMyProgressPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LearningProgressStatus status,
            @RequestParam(required = false) LearningContentType contentType) throws IdInvalidException {
        
        LearningProgressPageResponse response = learningProgressService.getMyProgressPage(page, size, status, contentType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{contentType}/{contentKey}")
    @ApiMessage("Get learning progress")
    public ResponseEntity<LearningProgressResponse> getMyProgress(
            @PathVariable LearningContentType contentType,
            @PathVariable String contentKey) throws IdInvalidException {
        return ResponseEntity.ok(learningProgressService.getMyProgress(contentType, contentKey));
    }

    @PostMapping("/{contentType}/{contentKey}/touch")
    @ApiMessage("Touch learning progress")
    public ResponseEntity<LearningProgressResponse> touchMyContent(
            @PathVariable LearningContentType contentType,
            @PathVariable String contentKey) throws IdInvalidException {
        return ResponseEntity.ok(learningProgressService.touchMyContent(contentType, contentKey));
    }

    @PutMapping("/{contentType}/{contentKey}")
    @ApiMessage("Update learning progress")
    public ResponseEntity<LearningProgressResponse> updateMyProgress(
            @PathVariable LearningContentType contentType,
            @PathVariable String contentKey,
            @Valid @RequestBody UpdateLearningProgressRequest request) throws IdInvalidException {
        return ResponseEntity.ok(learningProgressService.updateMyProgress(contentType, contentKey, request));
    }

    @PostMapping("/{contentType}/{contentKey}/complete")
    @ApiMessage("Complete learning content")
    public ResponseEntity<LearningProgressResponse> completeMyContent(
            @PathVariable LearningContentType contentType,
            @PathVariable String contentKey) throws IdInvalidException {
        return ResponseEntity.ok(learningProgressService.completeMyContent(contentType, contentKey));
    }

    @DeleteMapping("/{contentType}/{contentKey}")
    @ApiMessage("Reset learning progress")
    public ResponseEntity<Void> resetMyProgress(
            @PathVariable LearningContentType contentType,
            @PathVariable String contentKey) throws IdInvalidException {
        learningProgressService.resetMyProgress(contentType, contentKey);
        return ResponseEntity.noContent().build();
    }
}
