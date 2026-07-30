package com.web_tutorial.javabackend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.security.headers")
public record SecurityHeadersProperties(
        @NotBlank String contentSecurityPolicy,
        @NotBlank String permissionsPolicy,
        boolean hstsEnabled,
        @NotNull Duration hstsMaxAge) {

    public SecurityHeadersProperties {
        if (hstsMaxAge != null && (hstsMaxAge.isZero() || hstsMaxAge.isNegative())) {
            throw new IllegalArgumentException("hstsMaxAge must be greater than zero");
        }
    }
}
