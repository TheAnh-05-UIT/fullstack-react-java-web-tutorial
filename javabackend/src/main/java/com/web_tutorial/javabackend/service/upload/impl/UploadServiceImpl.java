package com.web_tutorial.javabackend.service.upload.impl;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.exception.UploadStorageException;
import com.web_tutorial.javabackend.service.upload.ImageUploadValidator;
import com.web_tutorial.javabackend.service.upload.StoredImage;
import com.web_tutorial.javabackend.service.upload.UploadService;
import com.web_tutorial.javabackend.service.upload.ValidatedImage;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Pattern SERVER_FILENAME = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\.(jpg|png)$");
    private static final Pattern SAFE_FOLDER = Pattern.compile("^[a-z0-9_-]+$");
    private static final Set<PosixFilePermission> OWNER_ONLY = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE);

    private final UploadProperties properties;
    private final ImageUploadValidator validator;
    private final Path storageRoot;

    public UploadServiceImpl(UploadProperties properties, ImageUploadValidator validator) {
        this.properties = properties;
        this.validator = validator;
        this.storageRoot = Path.of(properties.root()).toAbsolutePath().normalize();
        rejectSourceOrStaticRoot();
        initializeStorage();
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        String safeFolder = validateFolder(folder);
        ValidatedImage image = validator.validate(file);
        String filename = UUID.randomUUID() + "." + image.extension();
        Path folderPath = resolveInsideRoot(safeFolder);
        Path finalPath = resolveInsideRoot(safeFolder, filename);
        Path temporary = null;
        boolean finalFileCreated = false;
        try {
            Files.createDirectories(folderPath);
            rejectSymlink(folderPath);
            temporary = Files.createTempFile(folderPath, ".upload-", ".tmp");
            Files.write(temporary, image.bytes());
            applyRestrictedPermissions(temporary);
            moveAtomically(temporary, finalPath);
            finalFileCreated = true;
            applyRestrictedPermissions(finalPath);
            return "/uploads/images/" + safeFolder + "/" + filename;
        } catch (IOException exception) {
            deleteQuietly(temporary);
            if (finalFileCreated) {
                deleteQuietly(finalPath);
            }
            throw new UploadStorageException("Image could not be stored.", exception);
        }
    }

    @Override
    public StoredImage loadImage(String folder, String filename) {
        String safeFolder = validateFolder(folder);
        if (filename == null
                || !SERVER_FILENAME.matcher(filename.toLowerCase(Locale.ROOT)).matches()
                || !filename.equals(filename.toLowerCase(Locale.ROOT))) {
            throw new ResourceNotFoundException("Image not found.");
        }
        Path path = resolveInsideRoot(safeFolder, filename);
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(path)
                    || Files.size(path) > properties.maxFileSize().toBytes()) {
                throw new ResourceNotFoundException("Image not found.");
            }
            byte[] bytes = Files.readAllBytes(path);
            ValidatedImage image = validator.validateStored(bytes, filename);
            return new StoredImage(bytes, image.mediaType(), filename);
        } catch (ResourceNotFoundException exception) {
            throw exception;
        } catch (RuntimeException | IOException exception) {
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

    private Path resolveInsideRoot(String... segments) {
        Path resolved = storageRoot;
        for (String segment : segments) {
            resolved = resolved.resolve(segment);
        }
        resolved = resolved.normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new InvalidUploadException("Upload path is invalid.");
        }
        return resolved;
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(storageRoot);
            rejectSymlink(storageRoot);
        } catch (IOException exception) {
            throw new UploadStorageException("Upload storage is unavailable.", exception);
        }
    }

    private void rejectSourceOrStaticRoot() {
        String normalized = storageRoot.toString()
                .replace('\\', '/')
                .toLowerCase(Locale.ROOT);
        if (normalized.contains("/src/main/resources/static")
                || normalized.contains("/frontend/public")
                || normalized.contains("/src/main/resources/public")) {
            throw new InvalidUploadException("Upload root must be outside web-accessible source directories.");
        }
    }

    private void rejectSymlink(Path path) {
        if (Files.isSymbolicLink(path)) {
            throw new InvalidUploadException("Upload path is invalid.");
        }
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private void applyRestrictedPermissions(Path path) throws IOException {
        try {
            Files.setPosixFilePermissions(path, OWNER_ONLY);
        } catch (UnsupportedOperationException ignored) {
            // Windows and non-POSIX filesystems do not expose POSIX permissions.
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Preserve the original storage failure.
        }
    }
}
