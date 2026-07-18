package com.web_tutorial.javabackend.service.upload.impl;

import com.web_tutorial.javabackend.service.upload.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private final String BASE_UPLOAD_DIR = "uploads/images/";

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\"))) {
            originalFilename = Paths.get(originalFilename).getFileName().toString();
        }

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // Validate image extensions exactly matching original validation
        if (!extension.matches("^\\.(png|jpe?g|gif|webp)$")) {
            throw new IllegalArgumentException("Only image files (PNG, JPG, JPEG, GIF, WEBP) are allowed");
        }

        // Clean the folder parameter to prevent directory traversal
        String safeFolder = (folder != null) ? folder.replaceAll("[^a-zA-Z0-9_-]", "") : "general";
        if (safeFolder.isEmpty()) {
            safeFolder = "general";
        }
        String uploadDir = BASE_UPLOAD_DIR + safeFolder + "/";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save the file locally with path traversal check
        Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filepath = Paths.get(uploadDir, uniqueFilename).toAbsolutePath().normalize();
        if (!filepath.startsWith(uploadDirPath)) {
            throw new IllegalArgumentException("Invalid file path: path traversal detected");
        }

        Files.copy(file.getInputStream(), filepath);

        // Return relative URL so it works seamlessly in Docker (behind Nginx proxy) and Local environment
        return "/" + uploadDir + uniqueFilename;
    }
}
