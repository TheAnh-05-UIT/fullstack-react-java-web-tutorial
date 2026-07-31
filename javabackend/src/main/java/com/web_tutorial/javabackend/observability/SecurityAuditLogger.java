package com.web_tutorial.javabackend.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.web_tutorial.javabackend.security.ratelimit.RateLimitKeyFactory;

@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");
    private final RateLimitKeyFactory keyFactory;

    public SecurityAuditLogger(RateLimitKeyFactory keyFactory) {
        this.keyFactory = keyFactory;
    }

    public void info(SecurityAuditEvent event, String actor, String outcome, String reason) {
        log.info("event={} requestId={} actor={} outcome={} reason={}",
                event, requestId(), safe(actor), safe(outcome), safe(reason));
    }

    public void debug(SecurityAuditEvent event, String actor, String outcome, String reason) {
        log.debug("event={} requestId={} actor={} outcome={} reason={}",
                event, requestId(), safe(actor), safe(outcome), safe(reason));
    }

    public void warn(SecurityAuditEvent event, String actor, String outcome, String reason) {
        log.warn("event={} requestId={} actor={} outcome={} reason={}",
                event, requestId(), safe(actor), safe(outcome), safe(reason));
    }

    public void admin(
            SecurityAuditEvent event,
            String actor,
            String targetType,
            Object targetId,
            String reason) {
        log.info("event={} requestId={} actor={} targetType={} targetId={} outcome=SUCCESS reason={}",
                event, requestId(), safe(actor), safe(targetType), safe(targetId), safe(reason));
    }

    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "anonymous";
        }
        return "principal:" + keyFactory.principal(authentication.getName()).substring(0, 16);
    }

    private String requestId() {
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        return requestId == null ? "none" : requestId;
    }

    private String safe(Object value) {
        if (value == null) {
            return "none";
        }
        String sanitized = value.toString().replaceAll("[\\p{Cntrl}]", "_");
        return sanitized.substring(0, Math.min(sanitized.length(), 128));
    }
}
