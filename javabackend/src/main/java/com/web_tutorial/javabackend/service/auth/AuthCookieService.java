package com.web_tutorial.javabackend.service.auth;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthCookieService {

    private final String refreshName;
    private final boolean secure;
    private final String sameSite;
    private final String path;
    private final String domain;
    private final Duration refreshTtl;

    public AuthCookieService(
            @Value("${app.security.cookies.refresh-name:refresh_token}") String refreshName,
            @Value("${app.security.cookies.secure:true}") boolean secure,
            @Value("${app.security.cookies.same-site:Lax}") String sameSite,
            @Value("${app.security.cookies.path:/api/v1}") String path,
            @Value("${app.security.cookies.domain:}") String domain,
            @Value("${javabackend.jwt.refresh-token-validity-in-seconds:2592000}") long refreshTtlSeconds) {
        this.refreshName = refreshName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.path = path;
        this.domain = domain;
        this.refreshTtl = Duration.ofSeconds(refreshTtlSeconds);
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return cookie(refreshToken, refreshTtl);
    }

    public ResponseCookie clearRefreshCookie() {
        return cookie("", Duration.ZERO);
    }

    public String getRefreshName() {
        return refreshName;
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge);
        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }
        return builder.build();
    }
}
