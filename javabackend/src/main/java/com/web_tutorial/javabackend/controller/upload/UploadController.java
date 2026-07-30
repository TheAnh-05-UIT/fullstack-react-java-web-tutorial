package com.web_tutorial.javabackend.controller.upload;

import com.web_tutorial.javabackend.service.upload.UploadService;
import com.web_tutorial.javabackend.security.ratelimit.SensitiveEndpointRateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadService uploadService;
    private final SensitiveEndpointRateLimiter rateLimiter;

    public UploadController(
            UploadService uploadService,
            SensitiveEndpointRateLimiter rateLimiter) {
        this.uploadService = uploadService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            Authentication authentication) {
        rateLimiter.beforeUpload(authentication.getName());
        String fileUrl = uploadService.uploadImage(file, folder);
        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);
        return ResponseEntity.ok(response);
    }
}
