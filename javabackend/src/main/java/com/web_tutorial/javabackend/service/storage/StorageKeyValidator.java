package com.web_tutorial.javabackend.service.storage;

import java.util.Locale;
import java.util.regex.Pattern;

import com.web_tutorial.javabackend.exception.InvalidUploadException;

public final class StorageKeyValidator {

    private static final Pattern MANAGED_KEY = Pattern.compile(
            "^[a-z0-9_-]+/[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\.(jpg|png)$");

    private StorageKeyValidator() {
    }

    public static String requireManagedKey(String key) {
        if (key == null
                || !key.equals(key.toLowerCase(Locale.ROOT))
                || !MANAGED_KEY.matcher(key).matches()) {
            throw new InvalidUploadException("Upload path is invalid.");
        }
        return key;
    }
}
