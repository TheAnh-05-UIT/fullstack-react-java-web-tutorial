package com.web_tutorial.javabackend.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.web_tutorial.javabackend.config.RateLimitProperties;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Clock clock;
    private final Cache<String, WindowCounter> counters;

    public RateLimitService(RateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.counters = Caffeine.newBuilder()
                .maximumSize(properties.maximumSize())
                .expireAfterAccess(properties.cacheTtl())
                .build();
    }

    public RateLimitDecision consume(
            String namespace, String key, RateLimitProperties.Policy policy) {
        if (!properties.enabled()) {
            return allowed(policy.capacity(), policy.capacity());
        }
        Instant now = clock.instant();
        WindowCounter counter = counters.get(
                namespace + ':' + key,
                ignored -> new WindowCounter(now.plus(policy.window())));
        return counter.consume(now, policy);
    }

    public RateLimitDecision inspect(
            String namespace, String key, RateLimitProperties.Policy policy) {
        if (!properties.enabled()) {
            return allowed(policy.capacity(), policy.capacity());
        }
        Instant now = clock.instant();
        WindowCounter counter = counters.getIfPresent(namespace + ':' + key);
        return counter == null
                ? allowed(policy.capacity(), policy.capacity())
                : counter.inspect(now, policy);
    }

    public void reset(String namespace, String key) {
        counters.invalidate(namespace + ':' + key);
    }

    private RateLimitDecision allowed(int limit, int remaining) {
        return new RateLimitDecision(true, limit, remaining, 0, clock.instant().getEpochSecond());
    }

    private static final class WindowCounter {
        private Instant windowEnd;
        private int count;

        private WindowCounter(Instant windowEnd) {
            this.windowEnd = windowEnd;
        }

        private synchronized RateLimitDecision consume(
                Instant now, RateLimitProperties.Policy policy) {
            resetExpiredWindow(now, policy.window());
            if (count >= policy.capacity()) {
                return decision(false, policy.capacity(), now);
            }
            count++;
            return decision(true, policy.capacity(), now);
        }

        private synchronized RateLimitDecision inspect(
                Instant now, RateLimitProperties.Policy policy) {
            resetExpiredWindow(now, policy.window());
            return decision(count < policy.capacity(), policy.capacity(), now);
        }

        private void resetExpiredWindow(Instant now, Duration window) {
            if (!now.isBefore(windowEnd)) {
                count = 0;
                windowEnd = now.plus(window);
            }
        }

        private RateLimitDecision decision(boolean allowed, int limit, Instant now) {
            long retryAfter = allowed
                    ? 0
                    : Math.max(1, Duration.between(now, windowEnd).toSeconds());
            return new RateLimitDecision(
                    allowed,
                    limit,
                    Math.max(0, limit - count),
                    retryAfter,
                    windowEnd.getEpochSecond());
        }
    }
}
