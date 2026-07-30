package com.web_tutorial.javabackend.exception;

public class UnsupportedUploadTypeException extends RuntimeException {
    public UnsupportedUploadTypeException(String message) {
        super(message);
    }
}
