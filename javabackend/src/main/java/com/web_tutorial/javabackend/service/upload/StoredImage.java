package com.web_tutorial.javabackend.service.upload;

public record StoredImage(byte[] bytes, String mediaType, String filename) {
}
