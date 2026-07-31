package com.web_tutorial.javabackend.observability;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityAuditLogger auditLogger;

    public AuditAccessDeniedHandler(SecurityAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String path = request.getRequestURI();
        if ("/api/v1/refresh".equals(path) || "/api/v1/logout".equals(path)) {
            auditLogger.warn(SecurityAuditEvent.AUTH_REFRESH_FAILED,
                    "anonymous", "DENIED", "CSRF");
        }
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String roles = authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .sorted()
                    .toList()
                    .toString();
            auditLogger.warn(SecurityAuditEvent.AUTHZ_ACCESS_DENIED,
                    auditLogger.currentActor(), "DENIED",
                    "INSUFFICIENT_ROLE method=" + request.getMethod() + " roles=" + roles);
        }
        response.sendError(HttpStatus.FORBIDDEN.value());
    }
}
