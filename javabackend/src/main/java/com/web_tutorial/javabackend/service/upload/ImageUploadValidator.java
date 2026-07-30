package com.web_tutorial.javabackend.service.upload;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.exception.InvalidUploadException;
import com.web_tutorial.javabackend.exception.UnsupportedUploadTypeException;
import com.web_tutorial.javabackend.exception.UploadTooLargeException;

@Component
public class ImageUploadValidator {

    private static final Map<String, ImageType> TYPES_BY_EXTENSION = Map.of(
            "jpg", new ImageType("image/jpeg", "jpg"),
            "jpeg", new ImageType("image/jpeg", "jpg"),
            "png", new ImageType("image/png", "png"));

    private final UploadProperties properties;

    public ImageUploadValidator(UploadProperties properties) {
        this.properties = properties;
    }

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadException("Image file must not be empty.");
        }
        if (file.getSize() > properties.maxFileSize().toBytes()) {
            throw new UploadTooLargeException("Image file exceeds the configured size limit.");
        }

        String filename = file.getOriginalFilename();
        String extension = validatedExtension(filename);
        ImageType expected = TYPES_BY_EXTENSION.get(extension);
        if (expected == null || !properties.allowedTypes().contains(expected.mediaType())) {
            throw new UnsupportedUploadTypeException("Only JPEG and PNG images are supported.");
        }
        if (!expected.mediaType().equals(normalizeMediaType(file.getContentType()))) {
            throw new UnsupportedUploadTypeException("Declared image type does not match its extension.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new InvalidUploadException("Image content could not be read.");
        }
        validateBytes(bytes, expected);
        return new ValidatedImage(bytes, expected.mediaType(), expected.canonicalExtension());
    }

    public ValidatedImage validateStored(byte[] bytes, String filename) {
        String extension = validatedServerExtension(filename);
        ImageType expected = TYPES_BY_EXTENSION.get(extension);
        if (expected == null || !properties.allowedTypes().contains(expected.mediaType())) {
            throw new UnsupportedUploadTypeException("Stored file type is not supported.");
        }
        validateBytes(bytes, expected);
        return new ValidatedImage(bytes, expected.mediaType(), expected.canonicalExtension());
    }

    private void validateBytes(byte[] bytes, ImageType expected) {
        if (bytes.length == 0) {
            throw new InvalidUploadException("Image file must not be empty.");
        }
        if (bytes.length > properties.maxFileSize().toBytes()) {
            throw new UploadTooLargeException("Image file exceeds the configured size limit.");
        }
        if (!hasExpectedSignature(bytes, expected.mediaType())
                || !hasExpectedTerminator(bytes, expected.mediaType())) {
            throw new UnsupportedUploadTypeException("File content is not a supported image.");
        }

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new InvalidUploadException("Image content is corrupt or cannot be decoded.");
            }
            int width = image.getWidth();
            int height = image.getHeight();
            long pixels = Math.multiplyExact((long) width, (long) height);
            if (width <= 0 || height <= 0) {
                throw new InvalidUploadException("Image dimensions must be positive.");
            }
            if (width > properties.maxWidth()
                    || height > properties.maxHeight()
                    || pixels > properties.maxPixels()) {
                throw new InvalidUploadException("Image dimensions exceed the configured limit.");
            }
        } catch (ArithmeticException | IOException exception) {
            throw new InvalidUploadException("Image content is corrupt or cannot be decoded.");
        }
    }

    private String validatedExtension(String filename) {
        if (filename == null || filename.isBlank()
                || filename.indexOf('\0') >= 0
                || filename.chars().anyMatch(Character::isISOControl)
                || filename.contains("/") || filename.contains("\\")
                || filename.contains("..")) {
            throw new InvalidUploadException("Image filename is invalid.");
        }
        int firstDot = filename.indexOf('.');
        int lastDot = filename.lastIndexOf('.');
        if (firstDot <= 0 || firstDot != lastDot || lastDot == filename.length() - 1) {
            throw new InvalidUploadException("Image filename must have one supported extension.");
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private String validatedServerExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0 || dot == filename.length() - 1) {
            throw new UnsupportedUploadTypeException("Stored file type is not supported.");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeMediaType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int parameter = contentType.indexOf(';');
        return (parameter >= 0 ? contentType.substring(0, parameter) : contentType)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean hasExpectedSignature(byte[] bytes, String mediaType) {
        if ("image/png".equals(mediaType)) {
            byte[] signature = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
            return startsWith(bytes, signature);
        }
        return bytes.length >= 4
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff;
    }

    private boolean hasExpectedTerminator(byte[] bytes, String mediaType) {
        if ("image/png".equals(mediaType)) {
            byte[] iend = {0x49, 0x45, 0x4e, 0x44, (byte) 0xae, 0x42, 0x60, (byte) 0x82};
            return endsWith(bytes, iend);
        }
        return bytes.length >= 2
                && (bytes[bytes.length - 2] & 0xff) == 0xff
                && (bytes[bytes.length - 1] & 0xff) == 0xd9;
    }

    private boolean startsWith(byte[] bytes, byte[] expected) {
        if (bytes.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (bytes[index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean endsWith(byte[] bytes, byte[] expected) {
        if (bytes.length < expected.length) {
            return false;
        }
        int offset = bytes.length - expected.length;
        for (int index = 0; index < expected.length; index++) {
            if (bytes[offset + index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private record ImageType(String mediaType, String canonicalExtension) {
    }
}
