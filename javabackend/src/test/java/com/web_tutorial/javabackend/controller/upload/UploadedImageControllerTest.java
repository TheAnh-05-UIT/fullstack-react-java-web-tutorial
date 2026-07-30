package com.web_tutorial.javabackend.controller.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.web_tutorial.javabackend.service.upload.StoredImage;
import com.web_tutorial.javabackend.service.upload.UploadService;

class UploadedImageControllerTest {

    @Test
    void getImage_shouldReturnControlledImageHeaders() {
        UploadService uploadService = org.mockito.Mockito.mock(UploadService.class);
        String filename = "550e8400-e29b-41d4-a716-446655440000.png";
        when(uploadService.loadImage("tutorials", filename))
                .thenReturn(new StoredImage(new byte[] {1, 2, 3}, "image/png", filename));

        ResponseEntity<byte[]> response =
                new UploadedImageController(uploadService).getImage("tutorials", filename);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getContentDisposition().getType()).isEqualTo("inline");
        assertThat(response.getHeaders().getCacheControl()).contains("max-age=604800");
        assertThat(response.getBody()).containsExactly(1, 2, 3);
    }
}
