package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class AuthCookieCsrfIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String PASSWORD = "Test-password-123!";
    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginUsesHttpOnlyRefreshCookieAndDoesNotExposeRefreshTokenInJson() throws Exception {
        String email = createUser();

        mockMvc.perform(loginRequest(email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", false))
                .andExpect(cookie().path("refresh_token", "/api/v1"))
                .andExpect(cookie().maxAge("refresh_token", 2_592_000))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("SameSite=Lax")));
    }

    @Test
    void refreshRequiresCsrfAndCookieAndRotatesCookie() throws Exception {
        String refreshToken = loginCookie(createUser());

        mockMvc.perform(post("/api/v1/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf().useInvalidToken())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/refresh").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge("refresh_token", 0));

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(cookie().value("refresh_token",
                        org.hamcrest.Matchers.not(refreshToken)));
    }

    @Test
    void refreshTokenInRequestBodyIsNotAccepted() throws Exception {
        String refreshToken = loginCookie(createUser());

        mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresCsrfAndClearsCookieIdempotently() throws Exception {
        String refreshToken = loginCookie(createUser());

        mockMvc.perform(post("/api/v1/logout")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/logout")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(cookie().path("refresh_token", "/api/v1"));

        mockMvc.perform(post("/api/v1/logout").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    @Test
    void csrfBootstrapAndPublicGetDoNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));

        mockMvc.perform(get("/api/v1/tutorials"))
                .andExpect(status().isOk());
    }

    @Test
    void corsAllowsConfiguredCredentialedOriginAndRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-XSRF-TOKEN"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        org.hamcrest.Matchers.containsString("X-XSRF-TOKEN")));

        MvcResult rejected = mockMvc.perform(options("/api/v1/refresh")
                        .header(HttpHeaders.ORIGIN, "https://attacker.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andReturn();
        assertThat(rejected.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
            String email) {
        return post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}");
    }

    private String loginCookie(String email) throws Exception {
        MvcResult result = mockMvc.perform(loginRequest(email))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("refresh_token").getValue();
    }

    private String createUser() {
        String email = "auth3-" + UUID.randomUUID() + "@example.test";
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
