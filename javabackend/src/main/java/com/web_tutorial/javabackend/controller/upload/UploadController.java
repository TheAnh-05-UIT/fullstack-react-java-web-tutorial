package com.web_tutorial.javabackend.controller.upload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final String BASE_UPLOAD_DIR = "uploads/images/";

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is empty"));
        }

        try {
            // Generate a unique filename and validate extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            // Validate image extensions
            if (!extension.matches("^\\.(png|jpe?g|gif|webp)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only image files (PNG, JPG, JPEG, GIF, WEBP) are allowed"));
            }

            // Create the directory if it does not exist
            // Clean the folder parameter to prevent directory traversal
            String safeFolder = folder.replaceAll("[^a-zA-Z0-9_-]", "");
            String uploadDir = BASE_UPLOAD_DIR + safeFolder + "/";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save the file locally
            Path filepath = Paths.get(uploadDir, uniqueFilename);
            Files.copy(file.getInputStream(), filepath);

            // Construct the public URL dynamically based on the request URL
            String baseUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath().build().toUriString();
            String fileUrl = baseUrl + "/" + uploadDir + uniqueFilename;

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(UploadController.class).error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file"));
        }
    }
}
