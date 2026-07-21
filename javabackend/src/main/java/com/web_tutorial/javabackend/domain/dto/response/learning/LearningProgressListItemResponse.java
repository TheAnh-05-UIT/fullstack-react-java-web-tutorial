package com.web_tutorial.javabackend.domain.dto.response.learning;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;

public class LearningProgressListItemResponse {
    
    private LearningContentType contentType;
    private String contentKey;
    private String title;
    private String route;
    private String thumbnail;
    private boolean contentAvailable;
    private LearningProgressResponseStatus status;
    private Integer progressPercent;
    private Instant lastAccessedAt;
    private Instant completedAt;

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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public boolean isContentAvailable() {
        return contentAvailable;
    }
    public void setContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }
    public LearningProgressResponseStatus getStatus() {
        return status;
    }
    public void setStatus(LearningProgressResponseStatus status) {
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
}
