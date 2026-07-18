package com.web_tutorial.javabackend.service.upload;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface UploadService {
    String uploadImage(MultipartFile file, String folder) throws IOException;
}
