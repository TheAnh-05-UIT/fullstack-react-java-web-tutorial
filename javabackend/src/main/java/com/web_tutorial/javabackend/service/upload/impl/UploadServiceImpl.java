package com.web_tutorial.javabackend.service.upload.impl;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.exception.UnsupportedUploadTypeException;
import com.web_tutorial.javabackend.service.storage.ObjectStorageService;
import com.web_tutorial.javabackend.service.storage.StoredObject;
import com.web_tutorial.javabackend.service.upload.ImageUploadValidator;
import com.web_tutorial.javabackend.service.upload.StoredImage;
import com.web_tutorial.javabackend.service.upload.UploadService;
import com.web_tutorial.javabackend.service.upload.ValidatedImage;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Pattern SERVER_FILENAME = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\.(jpg|png)$");
    private static final Pattern SAFE_FOLDER = Pattern.compile("^[a-z0-9_-]+$");

    private final UploadProperties properties;
    private final ImageUploadValidator validator;
    private final ObjectStorageService storage;

    public UploadServiceImpl(
            UploadProperties properties,
            ImageUploadValidator validator,
            ObjectStorageService storage) {
        this.properties = properties;
        this.validator = validator;
        this.storage = storage;
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        String safeFolder = validateFolder(folder);
        ValidatedImage image = validator.validate(file);
        String filename = UUID.randomUUID() + "." + image.extension();
        storage.store(safeFolder + "/" + filename, image.bytes(), image.mediaType());
        return "/uploads/images/" + safeFolder + "/" + filename;
    }

    @Override
    public StoredImage loadImage(String folder, String filename) {
        String safeFolder = validateFolder(folder);
        if (filename == null
                || !SERVER_FILENAME.matcher(filename.toLowerCase(Locale.ROOT)).matches()
                || !filename.equals(filename.toLowerCase(Locale.ROOT))) {
            throw new ResourceNotFoundException("Image not found.");
        }
        try {
            StoredObject stored = storage.load(safeFolder + "/" + filename);
            ValidatedImage image = validator.validateStored(stored.bytes(), filename);
            return new StoredImage(stored.bytes(), image.mediaType(), filename);
        } catch (ResourceNotFoundException exception) {
            throw exception;
        } catch (InvalidUploadException | UnsupportedUploadTypeException exception) {
            throw new ResourceNotFoundException("Image not found.");
        }
    }

    private String validateFolder(String folder) {
        String candidate = folder == null || folder.isBlank() ? "general" : folder;
        if (!SAFE_FOLDER.matcher(candidate).matches()
                || !properties.allowedFolders().contains(candidate)) {
            throw new InvalidUploadException("Upload folder is invalid.");
        }
        return candidate;
    }
}
