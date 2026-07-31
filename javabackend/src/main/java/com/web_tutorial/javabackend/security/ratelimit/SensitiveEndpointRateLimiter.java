package com.web_tutorial.javabackend.security.ratelimit;

import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.RateLimitProperties;
import com.web_tutorial.javabackend.exception.RateLimitExceededException;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SensitiveEndpointRateLimiter {

    private static final String GENERIC_LOGIN_MESSAGE =
            "Invalid credentials or too many attempts.";

    private final RateLimitProperties properties;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyFactory keyFactory;
    private final ClientIpResolver clientIpResolver;
    private final SecurityAuditLogger auditLogger;

    public SensitiveEndpointRateLimiter(
            RateLimitProperties properties,
            RateLimitService rateLimitService,
            RateLimitKeyFactory keyFactory,
            ClientIpResolver clientIpResolver,
            SecurityAuditLogger auditLogger) {
        this.properties = properties;
        this.rateLimitService = rateLimitService;
        this.keyFactory = keyFactory;
        this.clientIpResolver = clientIpResolver;
        this.auditLogger = auditLogger;
    }

    public void beforeLogin(HttpServletRequest request, String email) {
        enforce("login-ip", ipKey(request), properties.loginIp(),
                "Too many login attempts. Please try again later.");
        RateLimitDecision account = rateLimitService.inspect(
                "login-account", keyFactory.email(email), properties.loginAccount());
        if (!account.allowed()) {
            auditLogger.warn(SecurityAuditEvent.AUTH_LOGIN_THROTTLED,
                    shortKey(keyFactory.email(email)), "DENIED",
                    "policy=login-account retryAfter=" + account.retryAfterSeconds());
            throw new RateLimitExceededException(GENERIC_LOGIN_MESSAGE, account);
        }
    }

    public void loginFailed(String email) {
        rateLimitService.consume(
                "login-account", keyFactory.email(email), properties.loginAccount());
    }

    public void loginSucceeded(String email) {
        rateLimitService.reset("login-account", keyFactory.email(email));
    }

    public void beforeRegister(HttpServletRequest request) {
        enforce("register-ip", ipKey(request), properties.register(),
                "Too many registration attempts. Please try again later.");
    }

    public void beforeRefresh(HttpServletRequest request) {
        enforce("refresh-ip", ipKey(request), properties.refresh(),
                "Too many refresh attempts. Please try again later.");
    }

    public void beforeUpload(String principal) {
        enforce("upload-user", keyFactory.principal(principal), properties.upload(),
                "Too many upload attempts. Please try again later.");
    }

    private String ipKey(HttpServletRequest request) {
        return keyFactory.ipAddress(clientIpResolver.resolve(request));
    }

    private void enforce(
            String namespace,
            String key,
            RateLimitProperties.Policy policy,
            String message) {
        RateLimitDecision decision = rateLimitService.consume(namespace, key, policy);
        if (!decision.allowed()) {
            SecurityAuditEvent event = namespace.startsWith("upload")
                    ? SecurityAuditEvent.UPLOAD_RATE_LIMITED
                    : SecurityAuditEvent.RATE_LIMIT_TRIGGERED;
            auditLogger.warn(event, shortKey(key), "DENIED",
                    "policy=" + namespace + " retryAfter=" + decision.retryAfterSeconds());
            throw new RateLimitExceededException(message, decision);
        }
    }

    private String shortKey(String key) {
        return "key:" + key.substring(0, Math.min(key.length(), 16));
    }
}
