package com.web_tutorial.javabackend.domain.dto.response.learning;

import java.time.Instant;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;

public class ContinueLearningResponse {

    private LearningContentType contentType;
    private String contentKey;
    private Integer progressPercent;
    private Instant lastAccessedAt;
    
    // UI Metadata for rendering the continue card
    private String title;
    private String route;
    private String thumbnail;

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
}
