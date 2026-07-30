package com.web_tutorial.javabackend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app.security.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        @Positive long maximumSize,
        @NotNull Duration cacheTtl,
        boolean trustForwardedHeaders,
        @Positive int trustedProxyHops,
        @Valid Policy loginIp,
        @Valid Policy loginAccount,
        @Valid Policy register,
        @Valid Policy refresh,
        @Valid Policy upload) {

    public RateLimitProperties {
        requirePositive(cacheTtl, "cacheTtl");
    }

    public record Policy(@Positive int capacity, @NotNull Duration window) {
        public Policy {
            requirePositive(window, "window");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value != null && (value.isZero() || value.isNegative())) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
    }
}
