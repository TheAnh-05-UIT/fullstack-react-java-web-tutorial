package com.web_tutorial.javabackend.security.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        int limit,
        int remaining,
        long retryAfterSeconds,
        long resetEpochSeconds) {
}
