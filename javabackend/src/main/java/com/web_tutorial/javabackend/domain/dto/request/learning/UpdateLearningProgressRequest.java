package com.web_tutorial.javabackend.domain.dto.request.learning;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateLearningProgressRequest {

    @NotNull(message = "Progress percent is required")
    @Min(value = 0, message = "Progress percent must be at least 0")
    @Max(value = 100, message = "Progress percent must be at most 100")
    private Integer progressPercent;

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }
}
