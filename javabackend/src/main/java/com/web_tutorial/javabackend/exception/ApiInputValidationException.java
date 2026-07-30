package com.web_tutorial.javabackend.exception;

public class ApiInputValidationException extends RuntimeException {

    public ApiInputValidationException(String message) {
        super(message);
    }
}
