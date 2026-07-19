package com.web_tutorial.javabackend.exception;

public class DevopsDataSeedingException extends RuntimeException {
    
    public DevopsDataSeedingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DevopsDataSeedingException(String message) {
        super(message);
    }
}
