package com.web_tutorial.javabackend.domain.dto.response.learning;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;

public class LearningProgressResponse {

    private LearningContentType contentType;
    private String contentKey;
    private LearningProgressStatus status;
    private Integer progressPercent;
    private Instant lastAccessedAt;
    private Instant completedAt;

    // Optional metadata
    private String title;

    public LearningContentType getContentType() {
        return contentType;
    }

    public void setContentType(LearningContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public LearningProgressStatus getStatus() {
        return status;
    }

    public void setStatus(LearningProgressStatus status) {
        this.status = status;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(Instant lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
