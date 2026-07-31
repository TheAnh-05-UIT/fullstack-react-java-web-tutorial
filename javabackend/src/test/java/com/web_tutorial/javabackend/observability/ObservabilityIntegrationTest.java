package com.web_tutorial.javabackend.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringBootTest(properties = "app.security.rate-limit.enabled=true")
@AutoConfigureMockMvc
@Import(ObservabilityIntegrationTest.FailingControllerConfiguration.class)
class ObservabilityIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String VALID_REQUEST_ID = "obs-1-request_123";
    private static final String RAW_EMAIL = "RAW_EMAIL_MARKER@example.test";
    private static final String RAW_PASSWORD = "SUPER_SECRET_PASSWORD_123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Logger rootLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void captureAuditLogs() {
        ensureRole("USER");
        ensureRole("ADMIN");
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

    @AfterEach
    void stopCapturingLogs() {
        rootLogger.detachAppender(appender);
        appender.stop();
        MDC.clear();
    }

    @Test
    void requestIdIsGeneratedPreservedAndReturnedOnSuccessAndError() throws Exception {
        String generated = mockMvc.perform(get("/api/v1/tutorials"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME))
                .andReturn().getResponse().getHeader(RequestIdFilter.HEADER_NAME);
        assertThat(generated).matches("[0-9a-f-]{36}");

        mockMvc.perform(get("/api/v1/csrf")
                        .header(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID))
                .andExpect(status().isNoContent())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID));

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME));

        mockMvc.perform(get("/api/v1/learning-progress/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME));

        mockMvc.perform(get("/api/v1/observability-test/fail")
                        .with(user("member@example.test").roles("USER")))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME));
    }

    @Test
    void unsafeRequestIdsAreReplacedAndMdcIsCleanedAfterEveryRequest() throws Exception {
        String oversized = "a".repeat(65);
        String replacement = mockMvc.perform(get("/api/v1/csrf")
                        .header(RequestIdFilter.HEADER_NAME, oversized))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getHeader(RequestIdFilter.HEADER_NAME);

        assertThat(replacement).isNotEqualTo(oversized).matches("[0-9a-f-]{36}");
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();

        mockMvc.perform(get("/api/v1/csrf")
                        .header(RequestIdFilter.HEADER_NAME, "bad\trequest"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME,
                        org.hamcrest.Matchers.not("bad\trequest")));
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void securityResponsesRetainRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(user("member@example.test").roles("USER"))
                        .header(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID))
                .andExpect(status().isForbidden())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID));
    }

    @Test
    void rateLimitedResponseRetainsRequestIdAndCreatesAuditEvent() throws Exception {
        User member = createUser("USER");
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/v1/login")
                            .with(remoteAddress("198.51.100.70"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"%s","password":"wrong-password"}
                                    """.formatted(member.getEmail())))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/login")
                        .with(remoteAddress("198.51.100.70"))
                        .header(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(member.getEmail())))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID));

        assertThat(auditMessages()).anyMatch(message ->
                message.contains("event=AUTH_LOGIN_THROTTLED")
                        && message.contains("policy=login-account"));
    }

    @Test
    void loginFailureAuditContainsCorrelationButNoSensitiveMarkers() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                        .with(remoteAddress("198.51.100.71"))
                        .header(RequestIdFilter.HEADER_NAME, VALID_REQUEST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(RAW_EMAIL, RAW_PASSWORD)))
                .andExpect(status().isUnauthorized());

        List<String> messages = auditMessages();
        assertThat(messages).anyMatch(message ->
                message.contains("event=AUTH_LOGIN_FAILED")
                        && message.contains("requestId=" + VALID_REQUEST_ID)
                        && message.contains("outcome=DENIED"));
        assertThat(String.join("\n", messages))
                .doesNotContain(RAW_EMAIL)
                .doesNotContain(RAW_PASSWORD)
                .doesNotContain("Authorization")
                .doesNotContain("refresh_token")
                .doesNotContain("XSRF-TOKEN");
    }

    @Test
    void loginRefreshAdminAndUploadFlowsCreateSafeAuditEvents() throws Exception {
        User member = createUser("USER");
        String requestId = "obs-flow-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/login")
                        .with(remoteAddress("198.51.100.72"))
                        .header(RequestIdFilter.HEADER_NAME, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"valid-password"}
                                """.formatted(member.getEmail())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .with(remoteAddress("198.51.100.73"))
                        .header(RequestIdFilter.HEADER_NAME, requestId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/users")
                        .with(user("admin@example.test").roles("ADMIN"))
                        .header(RequestIdFilter.HEADER_NAME, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"audit-created",
                                  "email":"audit-created-%s@example.test",
                                  "password":"valid-password",
                                  "role":"USER"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        MockMultipartFile invalidImage = new MockMultipartFile(
                "file", "RAW_ORIGINAL_FILENAME_MARKER.exe",
                MediaType.TEXT_PLAIN_VALUE, "RAW_FILE_BYTES_MARKER".getBytes());
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(invalidImage)
                        .param("folder", "general")
                        .with(user("admin@example.test").roles("ADMIN"))
                        .header(RequestIdFilter.HEADER_NAME, requestId))
                .andExpect(status().isUnsupportedMediaType());

        String logs = String.join("\n", auditMessages());
        assertThat(logs)
                .contains("event=AUTH_LOGIN_SUCCEEDED")
                .contains("event=AUTH_REFRESH_FAILED")
                .contains("reason=MISSING_COOKIE")
                .contains("event=ADMIN_USER_CREATED")
                .contains("event=UPLOAD_REJECTED")
                .doesNotContain("valid-password")
                .doesNotContain(member.getEmail())
                .doesNotContain("RAW_ORIGINAL_FILENAME_MARKER")
                .doesNotContain("RAW_FILE_BYTES_MARKER");
    }

    private List<String> auditMessages() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private void ensureRole(String name) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.saveAndFlush(role);
        });
    }

    private User createUser(String roleName) {
        String identity = "obs-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(identity);
        user.setEmail(identity + "@example.test");
        user.setPassword(passwordEncoder.encode("valid-password"));
        user.setRole(roleRepository.findByName(roleName).orElseThrow());
        return userRepository.saveAndFlush(user);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor remoteAddress(
            String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    @TestConfiguration
    static class FailingControllerConfiguration {
        @Bean
        FailingController failingController() {
            return new FailingController();
        }
    }

    @RestController
    static class FailingController {
        @GetMapping("/api/v1/observability-test/fail")
        void fail() {
            throw new IllegalStateException("OBS-1 synthetic failure");
        }
    }
}
