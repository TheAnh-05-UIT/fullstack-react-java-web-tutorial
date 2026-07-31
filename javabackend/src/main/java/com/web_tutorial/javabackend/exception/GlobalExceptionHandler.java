package com.web_tutorial.javabackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.validation.BindException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.web_tutorial.javabackend.domain.dto.response.RestResponse;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Xử lý Exception toàn cục – thứ tự handler quan trọng: cụ thể trước, tổng quát sau
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final SecurityAuditLogger auditLogger;

    public GlobalExceptionHandler(SecurityAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

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

    @ExceptionHandler(InvalidUploadException.class)
    public ResponseEntity<RestResponse<Object>> handleInvalidUploadException(InvalidUploadException ex) {
        auditLogger.warn(SecurityAuditEvent.UPLOAD_REJECTED,
                auditLogger.currentActor(), "DENIED", "INVALID_IMAGE");
        return uploadError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnsupportedUploadTypeException.class)
    public ResponseEntity<RestResponse<Object>> handleUnsupportedUploadTypeException(
            UnsupportedUploadTypeException ex) {
        auditLogger.warn(SecurityAuditEvent.UPLOAD_REJECTED,
                auditLogger.currentActor(), "DENIED", "MIME");
        return uploadError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler({UploadTooLargeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<RestResponse<Object>> handleUploadTooLargeException(Exception ex) {
        auditLogger.warn(SecurityAuditEvent.UPLOAD_REJECTED,
                auditLogger.currentActor(), "DENIED", "SIZE");
        return uploadError(HttpStatus.PAYLOAD_TOO_LARGE, "Image file exceeds the configured size limit.");
    }

    @ExceptionHandler(UploadStorageException.class)
    public ResponseEntity<RestResponse<Object>> handleUploadStorageException(
            UploadStorageException ex,
            HttpServletRequest request) {
        log.error("event=UPLOAD_STORAGE_FAILED method={}", request.getMethod(), ex);
        return uploadError(HttpStatus.INTERNAL_SERVER_ERROR, "Image storage operation failed.");
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
        auditLogger.warn(SecurityAuditEvent.AUTHZ_ACCESS_DENIED,
                auditLogger.currentActor(), "DENIED",
                "INSUFFICIENT_ROLE method=" + request.getMethod());
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        res.setMessage("Bạn không có quyền truy cập tài nguyên này.");
        res.setData(null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    // Xử lý lỗi Validation (form input không hợp lệ)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.debug("event={} outcome=DENIED", SecurityAuditEvent.INPUT_VALIDATION_REJECTED);
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();
        if ("/api/v1/register".equals(request.getRequestURI())) {
            auditLogger.info(SecurityAuditEvent.AUTH_REGISTER_REJECTED,
                    "anonymous", "DENIED", "VALIDATION");
        }
        if (fieldErrors.stream().anyMatch(error -> "role".equals(error.getField()))) {
            auditLogger.warn(SecurityAuditEvent.AUTHZ_ROLE_ESCALATION_REJECTED,
                    auditLogger.currentActor(), "DENIED", "INVALID_ROLE");
        }

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        List<String> errors = fieldErrors.stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(error -> String.valueOf(error.getDefaultMessage())))
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.toList());
        // Nếu chỉ có 1 lỗi thì trả về String, nhiều lỗi thì trả về List<String>
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(ApiInputValidationException.class)
    public ResponseEntity<RestResponse<Object>> handleApiInputValidation(ApiInputValidationException ex) {
        return inputError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<RestResponse<Object>> handleMalformedInput(Exception ex) {
        log.debug("event={} outcome=DENIED", SecurityAuditEvent.INPUT_VALIDATION_REJECTED);
        return inputError(HttpStatus.BAD_REQUEST, "Request input is invalid.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RestResponse<Object>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex) {
        return inputError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type is not supported.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RestResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        IntegrityViolationCategory category = classifyIntegrityViolation(ex);
        auditLogger.warn(SecurityAuditEvent.DATA_INTEGRITY_REJECTED,
                auditLogger.currentActor(), "DENIED", category.name());
        if (category == IntegrityViolationCategory.OTHER) {
            log.error("event=DATABASE_INTEGRITY_FAILED category=OTHER");
        }
        return switch (category) {
            case DUPLICATE -> inputError(
                    HttpStatus.CONFLICT, "A resource with the same unique value already exists.");
            case FOREIGN_KEY -> inputError(
                    HttpStatus.CONFLICT, "The operation conflicts with related data.");
            case CHECK, NOT_NULL -> inputError(
                    HttpStatus.BAD_REQUEST, "The supplied data violates a data integrity rule.");
            case OTHER -> inputError(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed.");
        };
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String message = "Giá trị của tham số " + name + " không hợp lệ.";

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        res.setMessage(message);
        res.setData(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(com.web_tutorial.javabackend.exception.DevopsContentSerializationException.class)
    public ResponseEntity<RestResponse<Object>> handleDevopsContentSerializationException(
            com.web_tutorial.javabackend.exception.DevopsContentSerializationException ex, HttpServletRequest request) {

        log.error("event=DEVOPS_CONTENT_PROCESSING_FAILED method={} operation={} field={}",
                request.getMethod(), ex.getOperation(), ex.getFieldName(), ex);

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        res.setMessage("Lỗi xử lý dữ liệu nội dung DevOps.");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    // lỗi 500 Internal Server Error – handler cuối cùng, chỉ bắt những gì chưa được xử lý ở trên
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("event=UNHANDLED_SERVER_ERROR method={}", request.getMethod(), ex);

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        res.setMessage("Đã xảy ra lỗi nội bộ máy chủ. Vui lòng thử lại sau.");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RestResponse<Object>> handleRateLimitExceeded(
            RateLimitExceededException ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setError(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        response.setMessage(ex.getMessage());
        response.setData(null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(ex.getDecision().retryAfterSeconds()))
                .header("RateLimit-Limit", Integer.toString(ex.getDecision().limit()))
                .header("RateLimit-Remaining", Integer.toString(ex.getDecision().remaining()))
                .header("RateLimit-Reset", Long.toString(ex.getDecision().resetEpochSeconds()))
                .body(response);
    }

    private ResponseEntity<RestResponse<Object>> uploadError(HttpStatus status, String message) {
        return inputError(status, message);
    }

    private ResponseEntity<RestResponse<Object>> inputError(HttpStatus status, Object message) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        response.setData(null);
        return ResponseEntity.status(status).body(response);
    }

    private IntegrityViolationCategory classifyIntegrityViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return switch (sqlException.getErrorCode()) {
                    case 1062 -> IntegrityViolationCategory.DUPLICATE;
                    case 1451, 1452 -> IntegrityViolationCategory.FOREIGN_KEY;
                    case 3819 -> IntegrityViolationCategory.CHECK;
                    case 1048 -> IntegrityViolationCategory.NOT_NULL;
                    default -> IntegrityViolationCategory.OTHER;
                };
            }
            current = current.getCause();
        }
        return IntegrityViolationCategory.OTHER;
    }

    private enum IntegrityViolationCategory {
        DUPLICATE,
        FOREIGN_KEY,
        CHECK,
        NOT_NULL,
        OTHER
    }
}
