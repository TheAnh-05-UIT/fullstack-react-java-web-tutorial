package com.web_tutorial.javabackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        @NotNull Type type,
        String s3Bucket,
        String s3Prefix,
        String awsRegion) {

    public enum Type {
        LOCAL,
        S3
    }

    public String requiredS3Bucket() {
        if (isMissing(s3Bucket)) {
            throw new IllegalStateException("APP_STORAGE_S3_BUCKET is required when APP_STORAGE_TYPE=s3.");
        }
        return s3Bucket.trim();
    }

    public String requiredAwsRegion() {
        if (isMissing(awsRegion)) {
            throw new IllegalStateException("AWS_REGION is required when APP_STORAGE_TYPE=s3.");
        }
        return awsRegion.trim();
    }

    public String normalizedS3Prefix() {
        if (s3Prefix == null || s3Prefix.isBlank()) {
            return "";
        }
        String candidate = s3Prefix.trim().replace('\\', '/');
        candidate = candidate.replaceAll("^/+", "").replaceAll("/+$", "");
        if (candidate.contains("..") || !candidate.matches("[a-zA-Z0-9/_-]+")) {
            throw new IllegalStateException("APP_STORAGE_S3_PREFIX is invalid.");
        }
        return candidate;
    }

    private boolean isMissing(String value) {
        return value == null
                || value.isBlank()
                || (value.startsWith("${") && value.endsWith("}"));
    }
}
