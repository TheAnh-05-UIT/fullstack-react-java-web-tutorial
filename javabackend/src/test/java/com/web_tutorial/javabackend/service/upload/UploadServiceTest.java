package com.web_tutorial.javabackend.service.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.exception.UnsupportedUploadTypeException;
import com.web_tutorial.javabackend.exception.UploadTooLargeException;
import com.web_tutorial.javabackend.service.upload.impl.UploadServiceImpl;

class UploadServiceTest {

    @TempDir
    Path temporaryDirectory;

    private UploadService uploadService;
    private ImageUploadValidator validator;

    @BeforeEach
    void setUp() {
        UploadProperties properties = properties(temporaryDirectory, DataSize.ofMegabytes(5), 4096, 4096, 16_000_000);
        validator = new ImageUploadValidator(properties);
        uploadService = new UploadServiceImpl(properties, validator);
    }

    @Test
    void uploadImage_shouldStoreDecodedPngWithServerGeneratedName() throws IOException {
        MockMultipartFile file = image("same-name.png", "image/png", "png", 4, 3);

        String firstUrl = uploadService.uploadImage(file, "tutorials");
        String secondUrl = uploadService.uploadImage(file, "tutorials");

        assertThat(firstUrl).matches("^/uploads/images/tutorials/[0-9a-f-]{36}\\.png$");
        assertThat(secondUrl).isNotEqualTo(firstUrl);
        assertThat(Files.isRegularFile(pathFor(firstUrl))).isTrue();
        assertThat(uploadService.loadImage("tutorials", filename(firstUrl)).mediaType())
                .isEqualTo("image/png");
    }

    @Test
    void uploadImage_shouldCanonicalizeJpegExtension() throws IOException {
        String url = uploadService.uploadImage(
                image("photo.jpeg", "image/jpeg", "jpg", 2, 2),
                "projects");

        assertThat(url).endsWith(".jpg");
        assertThat(uploadService.loadImage("projects", filename(url)).mediaType())
                .isEqualTo("image/jpeg");
    }

    @Test
    void validate_shouldRejectEmptyAndOversizedFiles() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        UploadProperties tiny = properties(temporaryDirectory, DataSize.ofBytes(10), 4096, 4096, 16_000_000);
        ImageUploadValidator tinyValidator = new ImageUploadValidator(tiny);
        MockMultipartFile oversized = new MockMultipartFile(
                "file", "large.png", "image/png", new byte[11]);

        assertThatThrownBy(() -> validator.validate(empty))
                .isInstanceOf(InvalidUploadException.class);
        assertThatThrownBy(() -> tinyValidator.validate(oversized))
                .isInstanceOf(UploadTooLargeException.class);
    }

    @Test
    void validate_shouldRejectSpoofedAndActiveContent() {
        assertRejected("fake.jpg", "image/jpeg", "<html><script>alert(1)</script></html>".getBytes());
        assertRejected("fake.png", "image/png", "<svg onload='alert(1)'></svg>".getBytes());
        assertRejected("binary.jpg", "image/jpeg", new byte[] {0x4d, 0x5a, 0x00, 0x01});
    }

    @Test
    void validate_shouldRejectClientMimeMismatchAndCorruptImage() throws IOException {
        MockMultipartFile wrongMime = new MockMultipartFile(
                "file", "valid.png", "image/jpeg", png(2, 2));
        MockMultipartFile truncated = new MockMultipartFile(
                "file", "broken.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xd9});

        assertThatThrownBy(() -> validator.validate(wrongMime))
                .isInstanceOf(UnsupportedUploadTypeException.class);
        assertThatThrownBy(() -> validator.validate(truncated))
                .isInstanceOf(InvalidUploadException.class);
    }

    @Test
    void validate_shouldRejectUnsafeOrAmbiguousFilenames() {
        List<String> filenames = List.of(
                "../../application.png",
                "..\\..\\secret.png",
                "C:\\secret.png",
                "avatar.jpg.exe",
                "no-extension",
                "bad\u0001name.png");

        for (String filename : filenames) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", filename, "image/png", new byte[] {1});
            assertThatThrownBy(() -> validator.validate(file))
                    .as(filename)
                    .isInstanceOf(InvalidUploadException.class);
        }
    }

    @Test
    void validate_shouldRejectExcessiveDimensionsAndPixels() throws IOException {
        UploadProperties constrained = properties(temporaryDirectory, DataSize.ofMegabytes(5), 2, 2, 4);
        ImageUploadValidator constrainedValidator = new ImageUploadValidator(constrained);
        MockMultipartFile image = new MockMultipartFile(
                "file", "wide.png", "image/png", png(3, 2));

        assertThatThrownBy(() -> constrainedValidator.validate(image))
                .isInstanceOf(InvalidUploadException.class)
                .hasMessageContaining("dimensions");
    }

    @Test
    void uploadAndLoad_shouldRejectFolderTraversalAndArbitraryStoredNames() throws IOException {
        MockMultipartFile image = image("safe.png", "image/png", "png", 2, 2);

        assertThatThrownBy(() -> uploadService.uploadImage(image, "../../outside"))
                .isInstanceOf(InvalidUploadException.class);
        assertThatThrownBy(() -> uploadService.uploadImage(image, "unknown"))
                .isInstanceOf(InvalidUploadException.class);
        assertThatThrownBy(() -> uploadService.loadImage("tutorials", "../../application.properties"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> uploadService.loadImage("tutorials", "not-managed.png"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void loadImage_shouldRejectUnsupportedFilePlacedInsideStorage() throws IOException {
        Path folder = temporaryDirectory.resolve("tutorials");
        Files.createDirectories(folder);
        String filename = "550e8400-e29b-41d4-a716-446655440000.png";
        Files.writeString(folder.resolve(filename), "<html>not an image</html>");

        assertThatThrownBy(() -> uploadService.loadImage("tutorials", filename))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void assertRejected(String filename, String contentType, byte[] bytes) {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, bytes);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(UnsupportedUploadTypeException.class);
    }

    private MockMultipartFile image(
            String filename, String contentType, String format, int width, int height) throws IOException {
        return new MockMultipartFile("file", filename, contentType, encoded(format, width, height));
    }

    private byte[] png(int width, int height) throws IOException {
        return encoded("png", width, height);
    }

    private byte[] encoded(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, format, output)).isTrue();
        return output.toByteArray();
    }

    private UploadProperties properties(
            Path root, DataSize maxSize, int maxWidth, int maxHeight, long maxPixels) {
        return new UploadProperties(
                root.toString(),
                maxSize,
                maxWidth,
                maxHeight,
                maxPixels,
                List.of("image/jpeg", "image/png"),
                List.of("general", "tutorials", "projects", "roadmaps", "users"));
    }

    private Path pathFor(String url) {
        return temporaryDirectory.resolve(url.substring("/uploads/images/".length()));
    }

    private String filename(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
