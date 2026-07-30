package com.web_tutorial.javabackend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        @NotBlank String root,
        @NotNull DataSize maxFileSize,
        @Positive int maxWidth,
        @Positive int maxHeight,
        @Positive long maxPixels,
        @NotEmpty List<String> allowedTypes,
        @NotEmpty List<String> allowedFolders) {
}
