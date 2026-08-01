package com.web_tutorial.javabackend.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.service.storage.impl.LocalObjectStorageService;

class LocalObjectStorageServiceTest {

    private static final String MANAGED_KEY =
            "users/550e8400-e29b-41d4-a716-446655440000.png";

    @TempDir
    Path temporaryDirectory;

    private LocalObjectStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new LocalObjectStorageService(new UploadProperties(
                temporaryDirectory.toString(),
                DataSize.ofMegabytes(5),
                4096,
                4096,
                16_000_000,
                List.of("image/jpeg", "image/png"),
                List.of("general", "tutorials", "projects", "roadmaps", "users")));
    }

    @Test
    void storeLoadExistsAndDeleteUseOnlyManagedKeys() {
        byte[] bytes = {1, 2, 3};

        storage.store(MANAGED_KEY, bytes, "image/png");

        assertThat(storage.exists(MANAGED_KEY)).isTrue();
        assertThat(storage.load(MANAGED_KEY).bytes()).isEqualTo(bytes);

        storage.delete(MANAGED_KEY);
        assertThat(storage.exists(MANAGED_KEY)).isFalse();
    }

    @Test
    void missingAndClientControlledKeysAreRejectedSafely() {
        assertThatThrownBy(() -> storage.load(MANAGED_KEY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Image not found.");
        assertThatThrownBy(() -> storage.store("../../secret.png", new byte[] {1}, "image/png"))
                .isInstanceOf(InvalidUploadException.class);
        assertThatThrownBy(() -> storage.store("users/original-name.png", new byte[] {1}, "image/png"))
                .isInstanceOf(InvalidUploadException.class);
    }
}
