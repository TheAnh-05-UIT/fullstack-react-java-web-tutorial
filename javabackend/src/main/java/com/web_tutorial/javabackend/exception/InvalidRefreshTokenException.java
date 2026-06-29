package com.web_tutorial.javabackend.exception;

/**
 * Custom exception cho Refresh Token không hợp lệ hoặc hết hạn.
 * Extends RuntimeException để Spring MVC có thể bắt qua @ExceptionHandler.
 * GlobalExceptionHandler sẽ map exception này thành HTTP 401 Unauthorized,
 * giúp frontend interceptor nhận đúng status và tự động logout.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
