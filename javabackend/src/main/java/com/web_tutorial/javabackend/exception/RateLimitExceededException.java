package com.web_tutorial.javabackend.exception;

import com.web_tutorial.javabackend.security.ratelimit.RateLimitDecision;

public class RateLimitExceededException extends RuntimeException {

    private final transient RateLimitDecision decision;

    public RateLimitExceededException(String message, RateLimitDecision decision) {
        super(message);
        this.decision = decision;
    }

    public RateLimitDecision getDecision() {
        return decision;
    }
}
