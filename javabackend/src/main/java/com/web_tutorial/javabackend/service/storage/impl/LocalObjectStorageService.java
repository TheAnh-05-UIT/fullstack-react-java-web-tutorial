package com.web_tutorial.javabackend.service.storage.impl;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.exception.UploadStorageException;
import com.web_tutorial.javabackend.service.storage.ObjectStorageService;
import com.web_tutorial.javabackend.service.storage.StorageKeyValidator;
import com.web_tutorial.javabackend.service.storage.StoredObject;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {

    private static final Set<PosixFilePermission> OWNER_ONLY = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE);

    private final UploadProperties properties;
    private final Path storageRoot;

    public LocalObjectStorageService(UploadProperties properties) {
        this.properties = properties;
        this.storageRoot = Path.of(properties.root()).toAbsolutePath().normalize();
        rejectSourceOrStaticRoot();
        initializeStorage();
    }

    @Override
    public void store(String key, byte[] bytes, String contentType) {
        String managedKey = StorageKeyValidator.requireManagedKey(key);
        Path finalPath = resolveInsideRoot(managedKey);
        Path folderPath = finalPath.getParent();
        Path temporary = null;
        boolean finalFileCreated = false;
        try {
            Files.createDirectories(folderPath);
            rejectSymlink(folderPath);
            temporary = Files.createTempFile(folderPath, ".upload-", ".tmp");
            Files.write(temporary, bytes);
            applyRestrictedPermissions(temporary);
            moveAtomically(temporary, finalPath);
            finalFileCreated = true;
            applyRestrictedPermissions(finalPath);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            if (finalFileCreated) {
                deleteQuietly(finalPath);
            }
            throw new UploadStorageException("Image could not be stored.", exception);
        }
    }

    @Override
    public StoredObject load(String key) {
        Path path = resolveInsideRoot(StorageKeyValidator.requireManagedKey(key));
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(path)
                    || Files.size(path) > properties.maxFileSize().toBytes()) {
                throw new ResourceNotFoundException("Image not found.");
            }
            return new StoredObject(Files.readAllBytes(path), Files.probeContentType(path));
        } catch (ResourceNotFoundException exception) {
            throw exception;
        } catch (RuntimeException | IOException exception) {
            throw new ResourceNotFoundException("Image not found.");
        }
    }

    @Override
    public void delete(String key) {
        Path path = resolveInsideRoot(StorageKeyValidator.requireManagedKey(key));
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new UploadStorageException("Image could not be deleted.", exception);
        }
    }

    @Override
    public boolean exists(String key) {
        Path path = resolveInsideRoot(StorageKeyValidator.requireManagedKey(key));
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path);
    }

    private Path resolveInsideRoot(String key) {
        Path resolved = storageRoot.resolve(key).normalize();
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
        String normalized = storageRoot.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
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
