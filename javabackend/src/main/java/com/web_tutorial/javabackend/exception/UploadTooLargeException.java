package com.web_tutorial.javabackend.exception;

public class UploadTooLargeException extends RuntimeException {
    public UploadTooLargeException(String message) {
        super(message);
    }
}
