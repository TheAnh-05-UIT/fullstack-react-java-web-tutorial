package com.web_tutorial.javabackend.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.web_tutorial.javabackend.security.ratelimit.RateLimitKeyFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class SecurityAuditLoggerTest {

    private static final String RAW_EMAIL = "RAW_EMAIL_MARKER@example.test";
    private final SecurityAuditLogger auditLogger =
            new SecurityAuditLogger(new RateLimitKeyFactory());
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void captureLogs() {
        logger = (Logger) LoggerFactory.getLogger("SECURITY_AUDIT");
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MDC.put(RequestIdFilter.MDC_KEY, "audit-test-request");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        RAW_EMAIL, null, java.util.List.of()));
    }

    @AfterEach
    void cleanContext() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void actorIsHashedAndClientControlledValuesCannotInjectLogLines() {
        auditLogger.warn(SecurityAuditEvent.AUTHZ_ACCESS_DENIED,
                auditLogger.currentActor(), "DENIED",
                "bad\r\nreason\tvalue" + "x".repeat(200));

        String message = appender.list.getFirst().getFormattedMessage();
        assertThat(message)
                .contains("event=AUTHZ_ACCESS_DENIED")
                .contains("requestId=audit-test-request")
                .contains("actor=principal:")
                .contains("reason=bad__reason_value")
                .doesNotContain(RAW_EMAIL)
                .doesNotContain("\r")
                .doesNotContain("\n")
                .doesNotContain("\t");
    }
}
