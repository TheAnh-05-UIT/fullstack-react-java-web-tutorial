package com.web_tutorial.javabackend.service.upload;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadImage(MultipartFile file, String folder);

    StoredImage loadImage(String folder, String filename);
}
