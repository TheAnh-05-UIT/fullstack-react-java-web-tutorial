package com.web_tutorial.javabackend.service.upload;

public record ValidatedImage(byte[] bytes, String mediaType, String extension) {
}
