package com.web_tutorial.javabackend.controller.upload;

import com.web_tutorial.javabackend.service.upload.UploadService;
import com.web_tutorial.javabackend.security.ratelimit.SensitiveEndpointRateLimiter;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
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
    private final SecurityAuditLogger auditLogger;

    public UploadController(
            UploadService uploadService,
            SensitiveEndpointRateLimiter rateLimiter,
            SecurityAuditLogger auditLogger) {
        this.uploadService = uploadService;
        this.rateLimiter = rateLimiter;
        this.auditLogger = auditLogger;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            Authentication authentication) {
        rateLimiter.beforeUpload(authentication.getName());
        String fileUrl = uploadService.uploadImage(file, folder);
        auditLogger.info(SecurityAuditEvent.UPLOAD_SUCCEEDED,
                auditLogger.currentActor(), "SUCCESS",
                "folder=" + folder + " size=" + file.getSize());
        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);
        return ResponseEntity.ok(response);
    }
}
