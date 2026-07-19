package com.web_tutorial.javabackend.exception;

public class DevopsContentSerializationException extends RuntimeException {
    
    private final String operation;
    private final String fieldName;

    public DevopsContentSerializationException(String operation, String fieldName, Throwable cause) {
        super(String.format("Failed to %s DevOps field: %s", operation.toLowerCase(), fieldName), cause);
        this.operation = operation;
        this.fieldName = fieldName;
    }

    public String getOperation() {
        return operation;
    }

    public String getFieldName() {
        return fieldName;
    }
}
