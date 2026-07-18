package com.web_tutorial.javabackend.service.upload;

import com.web_tutorial.javabackend.service.upload.impl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UploadServiceTest {

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl();
    }

    @Test
    void uploadImage_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "dummy image content".getBytes()
        );

        String url = uploadService.uploadImage(file, "tutorials");
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/images/tutorials/"));
        assertTrue(url.endsWith(".png"));
    }

    @Test
    void uploadImage_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                uploadService.uploadImage(file, "tutorials")
        );
        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void uploadImage_invalidExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                uploadService.uploadImage(file, "tutorials")
        );
        assertEquals("Only image files (PNG, JPG, JPEG, GIF, WEBP) are allowed", ex.getMessage());
    }

    @Test
    void uploadImage_folderPathTraversal_sanitized() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "dummy content".getBytes()
        );

        String url = uploadService.uploadImage(file, "../../malicious");
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/images/malicious/"));
    }
}
