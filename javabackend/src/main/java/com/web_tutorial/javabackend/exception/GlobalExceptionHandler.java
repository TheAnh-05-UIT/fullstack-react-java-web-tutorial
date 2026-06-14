package com.web_tutorial.javabackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.web_tutorial.javabackend.domain.dto.response.RestResponse;

// Xử lý Exception toàn cục

@RestControllerAdvice
public class GlobalExceptionHandler {

    // lỗi 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    // lỗi 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
