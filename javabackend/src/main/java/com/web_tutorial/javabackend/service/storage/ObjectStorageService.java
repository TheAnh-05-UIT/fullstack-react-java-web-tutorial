package com.web_tutorial.javabackend.service.storage;

public interface ObjectStorageService {
    void store(String key, byte[] bytes, String contentType);

    StoredObject load(String key);

    void delete(String key);

    boolean exists(String key);
}
