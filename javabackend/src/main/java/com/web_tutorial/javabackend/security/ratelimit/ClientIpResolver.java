package com.web_tutorial.javabackend.security.ratelimit;

import java.net.InetAddress;

import org.springframework.stereotype.Component;

import com.web_tutorial.javabackend.config.RateLimitProperties;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpResolver {

    private final RateLimitProperties properties;

    public ClientIpResolver(RateLimitProperties properties) {
        this.properties = properties;
    }

    public String resolve(HttpServletRequest request) {
        if (!properties.trustForwardedHeaders()) {
            return request.getRemoteAddr();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        String[] addresses = forwardedFor.split(",");
        int index = addresses.length - properties.trustedProxyHops();
        if (index < 0) {
            return request.getRemoteAddr();
        }
        String candidate = addresses[index].trim();
        return isIpAddress(candidate) ? candidate : request.getRemoteAddr();
    }

    private boolean isIpAddress(String candidate) {
        try {
            InetAddress.getByName(candidate);
            return candidate.matches("[0-9a-fA-F:.]+");
        } catch (Exception ignored) {
            return false;
        }
    }
}
