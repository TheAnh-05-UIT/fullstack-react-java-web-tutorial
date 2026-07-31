package com.web_tutorial.javabackend.controller.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
import com.web_tutorial.javabackend.security.ratelimit.RateLimitKeyFactory;
import com.web_tutorial.javabackend.service.auth.AuthCookieService;
import com.web_tutorial.javabackend.service.auth.AuthService;
import com.web_tutorial.javabackend.security.ratelimit.SensitiveEndpointRateLimiter;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final SensitiveEndpointRateLimiter rateLimiter;
    private final SecurityAuditLogger auditLogger;
    private final RateLimitKeyFactory keyFactory;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            SensitiveEndpointRateLimiter rateLimiter,
            SecurityAuditLogger auditLogger,
            RateLimitKeyFactory keyFactory) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.rateLimiter = rateLimiter;
        this.auditLogger = auditLogger;
        this.keyFactory = keyFactory;
    }

    @PostMapping("/register")
    @ApiMessage("Register Success")
    public ResponseEntity<LoginResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerDTO,
            HttpServletRequest request) throws IdInvalidException {
        rateLimiter.beforeRegister(request);
        try {
            LoginResponseDTO response = authService.register(registerDTO);
            auditLogger.info(SecurityAuditEvent.AUTH_REGISTER_SUCCEEDED,
                    "user:" + response.getUserLogin().getId(), "SUCCESS", "REGISTERED");
            return sessionResponse(response, HttpStatus.CREATED);
        } catch (IdInvalidException exception) {
            auditLogger.info(SecurityAuditEvent.AUTH_REGISTER_REJECTED,
                    subject(registerDTO.getEmail()), "DENIED", "DUPLICATE");
            throw exception;
        }
    }

    @PostMapping("/login")
    @ApiMessage("Login Success")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginDTO,
            HttpServletRequest request) {
        rateLimiter.beforeLogin(request, loginDTO.getEmail());
        try {
            LoginResponseDTO response = authService.login(loginDTO);
            rateLimiter.loginSucceeded(loginDTO.getEmail());
            auditLogger.info(SecurityAuditEvent.AUTH_LOGIN_SUCCEEDED,
                    "user:" + response.getUserLogin().getId(), "SUCCESS", "AUTHENTICATED");
            return sessionResponse(response, HttpStatus.OK);
        } catch (AuthenticationException exception) {
            rateLimiter.loginFailed(loginDTO.getEmail());
            auditLogger.warn(SecurityAuditEvent.AUTH_LOGIN_FAILED,
                    subject(loginDTO.getEmail()), "DENIED", "INVALID_CREDENTIALS");
            throw new BadCredentialsException(
                    "Invalid credentials or too many attempts.", exception);
        }
    }

    @PostMapping("/refresh")
    @ApiMessage("Get new Access Token by Refresh Token")
    public ResponseEntity<LoginResponseDTO> refreshToken(HttpServletRequest request) {
        rateLimiter.beforeRefresh(request);
        String refreshToken = readRefreshToken(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            auditLogger.info(SecurityAuditEvent.AUTH_REFRESH_FAILED,
                    "anonymous", "DENIED", "MISSING_COOKIE");
            return unauthorizedAndClearCookie();
        }
        try {
            LoginResponseDTO response = authService.refreshToken(refreshToken);
            auditLogger.info(SecurityAuditEvent.AUTH_REFRESH_SUCCEEDED,
                    "user:" + response.getUserLogin().getId(), "SUCCESS", "ROTATED");
            return sessionResponse(response, HttpStatus.OK);
        } catch (RuntimeException exception) {
            auditLogger.warn(SecurityAuditEvent.AUTH_REFRESH_FAILED,
                    "anonymous", "DENIED", "INVALID_TOKEN");
            return unauthorizedAndClearCookie();
        }
    }

    @PostMapping("/logout")
    @ApiMessage("Logout Success")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = readRefreshToken(request);
        boolean activeSessionRevoked = false;
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                authService.logout(refreshToken);
                activeSessionRevoked = true;
            } catch (RuntimeException ignored) {
                // Logout is idempotent and always clears the browser cookie.
            }
        }
        if (activeSessionRevoked) {
            auditLogger.info(SecurityAuditEvent.AUTH_LOGOUT_SUCCEEDED,
                    "authenticated-session", "SUCCESS", "SESSION_REVOKED");
        } else {
            auditLogger.debug(SecurityAuditEvent.AUTH_LOGOUT_SUCCEEDED,
                    "anonymous", "SUCCESS", "NO_ACTIVE_SESSION");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshCookie().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    private ResponseEntity<LoginResponseDTO> sessionResponse(
            LoginResponseDTO response, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE,
                        authCookieService.createRefreshCookie(response.getRefreshToken()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(response);
    }

    private ResponseEntity<LoginResponseDTO> unauthorizedAndClearCookie() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshCookie().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    private String readRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (authCookieService.getRefreshName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String subject(String email) {
        return "subject:" + keyFactory.email(email).substring(0, 16);
    }
}
