package com.web_tutorial.javabackend.exception;

// Xử lý phần ID không hợp lệ
public class IdInvalidException extends Exception {
    public IdInvalidException(String message) {
        super(message);
    }
}
