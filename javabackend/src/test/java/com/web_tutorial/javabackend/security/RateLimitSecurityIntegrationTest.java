package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitSecurityIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String PASSWORD = "Test-password-123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void rateLimitProperties(DynamicPropertyRegistry registry) {
        registry.add("app.security.rate-limit.enabled", () -> "true");
        registry.add("app.security.rate-limit.login-ip.capacity", () -> "3");
        registry.add("app.security.rate-limit.login-account.capacity", () -> "2");
        registry.add("app.security.rate-limit.register.capacity", () -> "2");
        registry.add("app.security.rate-limit.refresh.capacity", () -> "2");
        registry.add("app.security.rate-limit.upload.capacity", () -> "2");
    }

    @Test
    void loginIpLimitReturnsStable429EnvelopeAndHeaders() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            String email = "missing-" + UUID.randomUUID() + "@example.test";
            mockMvc.perform(login(email, "wrong", "198.51.100.10"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(login(
                        "missing-" + UUID.randomUUID() + "@example.test",
                        "wrong",
                        "198.51.100.10"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string("RateLimit-Limit", "3"))
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(jsonPath("$.statusCode").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    void accountFailuresAreSharedAcrossIpsAndSuccessfulLoginResetsThem() throws Exception {
        String email = createUser();
        mockMvc.perform(login(email, "wrong", "198.51.100.20"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials or too many attempts."));
        mockMvc.perform(login(email, PASSWORD, "198.51.100.21"))
                .andExpect(status().isOk());

        mockMvc.perform(login(email, "wrong", "198.51.100.22"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(login(email, "wrong", "198.51.100.23"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(login(email, PASSWORD, "198.51.100.24"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Invalid credentials or too many attempts."));
    }

    @Test
    void registrationLimitIsIndependentPerIp() throws Exception {
        mockMvc.perform(register("198.51.100.30")).andExpect(status().isCreated());
        mockMvc.perform(register("198.51.100.30")).andExpect(status().isCreated());
        mockMvc.perform(register("198.51.100.30")).andExpect(status().isTooManyRequests());

        mockMvc.perform(register("198.51.100.31")).andExpect(status().isCreated());
    }

    @Test
    void refreshRateLimitRunsOnlyAfterCsrfValidation() throws Exception {
        String email = createUser();
        MvcResult login = mockMvc.perform(login(email, PASSWORD, "198.51.100.40"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refresh = login.getResponse().getCookie("refresh_token");

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/refresh")
                            .cookie(refresh)
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.41");
                                return request;
                            }))
                    .andExpect(status().isForbidden());
        }

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .cookie(refresh)
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.41");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void refreshReturns429AfterIpLimitWithValidCsrf() throws Exception {
        Cookie invalidRefresh = new Cookie("refresh_token", "invalid");
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/v1/refresh")
                            .with(csrf())
                            .cookie(invalidRefresh)
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.42");
                                return request;
                            }))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .cookie(invalidRefresh)
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.42");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void securityHeadersArePresentAndHstsIsDisabledInTestProfile() throws Exception {
        MvcResult response = mockMvc.perform(get("/api/v1/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Permissions-Policy"))
                .andReturn();

        assertThat(response.getResponse().getHeader("Strict-Transport-Security")).isNull();
    }

    @Test
    void uploadLimitAppliesPerAuthenticatedAdminAndCountsInvalidUploads() throws Exception {
        MockMultipartFile invalid = new MockMultipartFile(
                "file", "payload.txt", MediaType.TEXT_PLAIN_VALUE, "not-an-image".getBytes());

        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(multipart("/api/v1/upload")
                            .file(invalid)
                            .with(user("admin-a").authorities(() -> "ROLE_ADMIN")))
                    .andExpect(status().isUnsupportedMediaType());
        }
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(invalid)
                        .with(user("admin-a").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(invalid)
                        .with(user("admin-b").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isUnsupportedMediaType());

        mockMvc.perform(multipart("/api/v1/upload").file(invalid))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(multipart("/api/v1/upload")
                        .file(invalid)
                        .with(user("reader").authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder login(String email, String password, String ip) {
        return post("/api/v1/login")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    private MockHttpServletRequestBuilder register(String ip) {
        String id = UUID.randomUUID().toString();
        return post("/api/v1/register")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"User %s","email":"%s@example.test","password":"%s"}
                        """.formatted(id, id, PASSWORD));
    }

    private String createUser() {
        String email = "sec5-" + UUID.randomUUID() + "@example.test";
        Role role = roleRepository.findByName("USER").orElseGet(() -> {
            Role created = new Role();
            created.setName("USER");
            return roleRepository.saveAndFlush(created);
        });
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        userRepository.saveAndFlush(user);
        return email;
    }
}
