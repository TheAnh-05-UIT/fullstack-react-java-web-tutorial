package com.web_tutorial.javabackend.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.web_tutorial.javabackend.domain.dto.response.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Xử lý Exception toàn cục – thứ tự handler quan trọng: cụ thể trước, tổng quát sau
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // lỗi 400 Bad Request – nghiệp vụ không hợp lệ (id không tồn tại, email trùng, ...)
    @ExceptionHandler(value = {
            IdInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleIdInvalidException(IdInvalidException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        res.setMessage(ex.getMessage());
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

    // lỗi 401 Unauthorized – Refresh Token không hợp lệ hoặc hết hạn.
    // Trả về 401 để Axios interceptor bên FE nhận đúng status và chuyển về /login.
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<RestResponse<Object>> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        res.setMessage(ex.getMessage());
        res.setData(null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // lỗi 401 Unauthorized – Spring Security ném khi chưa xác thực
    // (vd: token thiếu, token sai). Nếu không có handler này, Exception.class bên
    // dưới sẽ bắt và trả 500 thay vì 401 đúng chuẩn.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        res.setMessage(ex.getMessage());
        res.setData(null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // lỗi 403 Forbidden – Spring Security ném khi đã xác thực nhưng không
    // đủ quyền truy cập resource. Tương tự, nếu không xử lý riêng sẽ bị nuốt thành 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        res.setMessage("Bạn không có quyền truy cập tài nguyên này.");
        res.setData(null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    // Xử lý lỗi Validation (form input không hợp lệ)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        List<String> errors = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        // Nếu chỉ có 1 lỗi thì trả về String, nhiều lỗi thì trả về List<String>
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // lỗi 500 Internal Server Error – handler cuối cùng, chỉ bắt những gì chưa được xử lý ở trên
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        res.setMessage("Đã xảy ra lỗi nội bộ máy chủ. Vui lòng thử lại sau.");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
