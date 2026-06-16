package com.web_tutorial.javabackend.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.web_tutorial.javabackend.domain.dto.response.RestResponse;

// Xử lý Exception toàn cục
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {
            IdInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Exception occurred");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

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

    // Xử lý lỗi Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
