package com.web_tutorial.javabackend.controller.upload;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.service.upload.StoredImage;
import com.web_tutorial.javabackend.service.upload.UploadService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/uploads/images")
public class UploadedImageController {

    private final UploadService uploadService;

    public UploadedImageController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String folder,
            @PathVariable String filename) {
        StoredImage image = uploadService.loadImage(folder, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.mediaType()))
                .contentLength(image.bytes().length)
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(image.filename()).build().toString())
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().immutable())
                .body(image.bytes());
    }
}
