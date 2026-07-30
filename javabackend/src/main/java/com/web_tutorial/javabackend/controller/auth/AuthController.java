package com.web_tutorial.javabackend.controller.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.dto.request.auth.LoginRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.auth.RegisterRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.auth.LoginResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.service.auth.AuthCookieService;
import com.web_tutorial.javabackend.service.auth.AuthService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/register")
    @ApiMessage("Register Success")
    public ResponseEntity<LoginResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerDTO) throws IdInvalidException {
        return sessionResponse(authService.register(registerDTO), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @ApiMessage("Login Success")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return sessionResponse(authService.login(loginDTO), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @ApiMessage("Get new Access Token by Refresh Token")
    public ResponseEntity<LoginResponseDTO> refreshToken(HttpServletRequest request) {
        String refreshToken = readRefreshToken(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            return unauthorizedAndClearCookie();
        }
        try {
            return sessionResponse(authService.refreshToken(refreshToken), HttpStatus.OK);
        } catch (RuntimeException exception) {
            return unauthorizedAndClearCookie();
        }
    }

    @PostMapping("/logout")
    @ApiMessage("Logout Success")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = readRefreshToken(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                authService.logout(refreshToken);
            } catch (RuntimeException ignored) {
                // Logout is idempotent and always clears the browser cookie.
            }
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
}
