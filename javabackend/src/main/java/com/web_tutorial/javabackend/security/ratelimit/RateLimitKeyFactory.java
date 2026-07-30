package com.web_tutorial.javabackend.security.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.InetAddress;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyFactory {

    public String email(String email) {
        return hash(email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    public String principal(String principal) {
        return hash(principal == null ? "" : principal.trim().toLowerCase(Locale.ROOT));
    }

    public String ipAddress(String ipAddress) {
        return hash(normalizeIpAddress(ipAddress));
    }

    private String normalizeIpAddress(String ipAddress) {
        String value = ipAddress == null ? "" : ipAddress.trim();
        if (!value.matches("[0-9a-fA-F:.]+")) {
            return value;
        }
        try {
            return InetAddress.getByName(value).getHostAddress();
        } catch (Exception ignored) {
            return value;
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
