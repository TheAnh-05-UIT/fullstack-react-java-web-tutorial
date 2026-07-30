package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class UploadSecurityIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final Path STORAGE_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"),
            "webtutorial-upload-security-test").toAbsolutePath().normalize();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureUploadStorage(DynamicPropertyRegistry registry) {
        registry.add("app.upload.root", STORAGE_ROOT::toString);
    }

    @AfterEach
    void cleanStorage() throws IOException {
        if (!Files.exists(STORAGE_ROOT)) {
            return;
        }
        try (var paths = Files.walk(STORAGE_ROOT)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(STORAGE_ROOT))
                    .forEach(UploadSecurityIntegrationTest::delete);
        }
    }

    @Test
    void upload_shouldEnforceAnonymousUserAdminMatrix() throws Exception {
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(validPng())
                        .param("folder", "tutorials"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(validPng())
                        .param("folder", "tutorials")
                        .with(user("reader").authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(validPng())
                        .param("folder", "tutorials")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        try (var files = Files.walk(STORAGE_ROOT)) {
            assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(1);
        }
    }

    @Test
    void uploadAndServe_shouldKeepPathPrivateAndReturnControlledHeaders() throws Exception {
        String response = mockMvc.perform(multipart("/api/v1/upload")
                        .file(validPng())
                        .param("folder", "projects")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String url = response.substring(
                response.indexOf("/uploads/images/projects/"),
                response.indexOf(".png") + 4);
        assertThat(response).doesNotContain(STORAGE_ROOT.toString());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.startsWith("inline")));

        mockMvc.perform(get("/uploads/images/projects/550e8400-e29b-41d4-a716-446655440000.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidUpload_shouldReturnSpecificStatusAndCreateNoFile() throws Exception {
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file",
                "payload.png",
                "image/png",
                "<html><script>alert(1)</script></html>".getBytes());

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(fakeImage)
                        .param("folder", "tutorials")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isUnsupportedMediaType());

        try (var files = Files.walk(STORAGE_ROOT)) {
            assertThat(files.noneMatch(Files::isRegularFile)).isTrue();
        }
    }

    private MockMultipartFile validPng() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "safe.png", "image/png", output.toByteArray());
    }

    private static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not clean test upload storage.", exception);
        }
    }
}
