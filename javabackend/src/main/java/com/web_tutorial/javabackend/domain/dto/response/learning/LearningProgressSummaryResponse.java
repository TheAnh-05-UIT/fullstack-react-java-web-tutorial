package com.web_tutorial.javabackend.domain.dto.response.learning;

public class LearningProgressSummaryResponse {

    private long completedCount;
    private long inProgressCount;
    private long totalTracked;
    private double completionRate;

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getTotalTracked() {
        return totalTracked;
    }

    public void setTotalTracked(long totalTracked) {
        this.totalTracked = totalTracked;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }
}
