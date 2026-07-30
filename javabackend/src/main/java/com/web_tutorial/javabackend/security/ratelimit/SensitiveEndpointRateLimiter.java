package com.web_tutorial.javabackend.security.ratelimit;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web_tutorial.javabackend.config.RateLimitProperties;
import com.web_tutorial.javabackend.exception.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SensitiveEndpointRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveEndpointRateLimiter.class);
    private static final String GENERIC_LOGIN_MESSAGE =
            "Invalid credentials or too many attempts.";

    private final RateLimitProperties properties;
    private final RateLimitService rateLimitService;
    private final RateLimitKeyFactory keyFactory;
    private final ClientIpResolver clientIpResolver;

    public SensitiveEndpointRateLimiter(
            RateLimitProperties properties,
            RateLimitService rateLimitService,
            RateLimitKeyFactory keyFactory,
            ClientIpResolver clientIpResolver) {
        this.properties = properties;
        this.rateLimitService = rateLimitService;
        this.keyFactory = keyFactory;
        this.clientIpResolver = clientIpResolver;
    }

    public void beforeLogin(HttpServletRequest request, String email) {
        enforce("login-ip", ipKey(request), properties.loginIp(),
                "Too many login attempts. Please try again later.");
        RateLimitDecision account = rateLimitService.inspect(
                "login-account", keyFactory.email(email), properties.loginAccount());
        if (!account.allowed()) {
            log.warn("Security rate limit triggered for policy login-account");
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
            log.warn("Security rate limit triggered for policy {}", namespace);
            throw new RateLimitExceededException(message, decision);
        }
    }
}
