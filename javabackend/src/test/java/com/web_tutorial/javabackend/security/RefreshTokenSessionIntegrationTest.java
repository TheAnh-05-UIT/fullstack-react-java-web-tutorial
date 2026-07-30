package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jakarta.servlet.http.Cookie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.domain.user.RefreshTokenSession;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RefreshTokenSessionRepository;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.security.RefreshTokenHasher;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenSessionIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String PASSWORD = "Test-password-123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenSessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenHasher tokenHasher;

    @Autowired
    @Qualifier("refreshJwtDecoder")
    private JwtDecoder refreshJwtDecoder;

    @Test
    void multipleLoginsCreateIndependentFamiliesAndStoreOnlyHashes() throws Exception {
        User user = createUser();
        TokenPair first = login(user.getEmail());
        TokenPair second = login(user.getEmail());

        Jwt firstJwt = refreshJwtDecoder.decode(first.refreshToken());
        Jwt secondJwt = refreshJwtDecoder.decode(second.refreshToken());
        List<RefreshTokenSession> sessions = sessionRepository.findAllByUserId(user.getId());

        assertThat(firstJwt.getClaimAsString("family_id")).isNotBlank()
                .isNotEqualTo(secondJwt.getClaimAsString("family_id"));
        assertThat(firstJwt.getId()).isNotBlank().isNotEqualTo(secondJwt.getId());
        assertThat(sessions).hasSize(2).allSatisfy(session -> {
            assertThat(session.getTokenHash()).hasSize(64);
            assertThat(session.getRevokedAt()).isNull();
        });
        assertThat(sessions).extracting(RefreshTokenSession::getTokenHash)
                .containsExactlyInAnyOrder(
                        tokenHasher.hash(first.refreshToken()),
                        tokenHasher.hash(second.refreshToken()))
                .doesNotContain(first.refreshToken(), second.refreshToken());
    }

    @Test
    void refreshRotatesHashAndJtiWithinFamilyAndNewTokenCanRotateAgain() throws Exception {
        User user = createUser();
        TokenPair first = login(user.getEmail());
        Jwt firstJwt = refreshJwtDecoder.decode(first.refreshToken());
        RefreshTokenSession before = sessionRepository
                .findByFamilyId(firstJwt.getClaimAsString("family_id")).orElseThrow();

        TokenPair second = refresh(first.refreshToken(), 200);
        Jwt secondJwt = refreshJwtDecoder.decode(second.refreshToken());
        RefreshTokenSession after = sessionRepository
                .findByFamilyId(firstJwt.getClaimAsString("family_id")).orElseThrow();

        assertThat(secondJwt.getClaimAsString("family_id"))
                .isEqualTo(firstJwt.getClaimAsString("family_id"));
        assertThat(secondJwt.getId()).isNotEqualTo(firstJwt.getId());
        assertThat(after.getTokenHash())
                .isNotEqualTo(before.getTokenHash())
                .isEqualTo(tokenHasher.hash(second.refreshToken()));
        assertThat(after.getCurrentJti()).isEqualTo(secondJwt.getId());
        assertThat(after.getPreviousJti()).isEqualTo(firstJwt.getId());
        assertThat(after.getPreviousTokenHash()).isEqualTo(tokenHasher.hash(first.refreshToken()));
        assertThat(after.getPreviousConsumedAt()).isNotNull();
        assertThat(after.getExpiresAt()).isEqualTo(secondJwt.getExpiresAt());
        assertThat(after.getReplacedByJti()).isEqualTo(secondJwt.getId());
        refresh(second.refreshToken(), 200);
    }

    @Test
    void reuseRevokesOnlyAffectedFamily() throws Exception {
        User user = createUser();
        TokenPair familyOne = login(user.getEmail());
        TokenPair familyTwo = login(user.getEmail());
        TokenPair rotatedFamilyOne = refresh(familyOne.refreshToken(), 200);

        expireConcurrencyGrace(familyOne.refreshToken());
        refresh(familyOne.refreshToken(), 401);
        Jwt familyOneJwt = refreshJwtDecoder.decode(familyOne.refreshToken());
        RefreshTokenSession revoked = sessionRepository
                .findByFamilyId(familyOneJwt.getClaimAsString("family_id")).orElseThrow();

        assertThat(revoked.getRevokedAt()).isNotNull();
        assertThat(revoked.getRevokeReason()).isEqualTo("TOKEN_REUSE");
        refresh(rotatedFamilyOne.refreshToken(), 401);
        refresh(familyTwo.refreshToken(), 200);
    }

    @Test
    void concurrentRefreshKeepsWinnerTokenUsableAndDoesNotRevokeFamily() throws Exception {
        User user = createUser();
        TokenPair original = login(user.getEmail());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<RefreshResult> first =
                    executor.submit(() -> concurrentRefresh(original.refreshToken(), ready, start));
            Future<RefreshResult> second =
                    executor.submit(() -> concurrentRefresh(original.refreshToken(), ready, start));
            ready.await();
            start.countDown();

            List<RefreshResult> results = List.of(first.get(), second.get());
            assertThat(results).extracting(RefreshResult::status)
                    .containsExactlyInAnyOrder(200, 401);
            RefreshResult winner = results.stream()
                    .filter(result -> result.status() == 200)
                    .findFirst()
                    .orElseThrow();
            refresh(winner.refreshToken(), 200);
        }

        assertThat(sessionRepository.findAllByUserId(user.getId()))
                .singleElement()
                .satisfies(session -> {
                    assertThat(session.getRevokedAt()).isNull();
                    assertThat(session.getRevokeReason()).isNull();
                });
    }

    @Test
    void logoutRevokesCurrentFamilyAndIsIdempotent() throws Exception {
        User user = createUser();
        TokenPair first = login(user.getEmail());
        TokenPair second = login(user.getEmail());

        logout(first.refreshToken(), 204);
        logout(first.refreshToken(), 204);

        refresh(first.refreshToken(), 401);
        refresh(second.refreshToken(), 200);
        Jwt firstJwt = refreshJwtDecoder.decode(first.refreshToken());
        Jwt secondJwt = refreshJwtDecoder.decode(second.refreshToken());
        assertThat(sessionRepository.findByFamilyId(
                firstJwt.getClaimAsString("family_id")).orElseThrow().getRevokeReason())
                .isEqualTo("LOGOUT");
        assertThat(sessionRepository.findByFamilyId(
                secondJwt.getClaimAsString("family_id")).orElseThrow().getRevokedAt())
                .isNull();
    }

    private RefreshResult concurrentRefresh(String token, CountDownLatch ready,
            CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        MvcResult result = mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", token)))
                .andReturn();
        int status = result.getResponse().getStatus();
        String refreshToken = status == 200
                ? result.getResponse().getCookie("refresh_token").getValue()
                : null;
        return new RefreshResult(status, refreshToken);
    }

    private TokenPair login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new TokenPair(
                data.path("accessToken").asText(),
                result.getResponse().getCookie("refresh_token").getValue());
    }

    private TokenPair refresh(String token, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", token)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
        if (expectedStatus != 200) {
            return null;
        }
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new TokenPair(
                data.path("accessToken").asText(),
                result.getResponse().getCookie("refresh_token").getValue());
    }

    private void logout(String refreshToken, int expectedStatus) throws Exception {
        int status = mockMvc.perform(post("/api/v1/logout")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(expectedStatus);
    }

    private User createUser() {
        String email = "auth2-" + UUID.randomUUID() + "@example.test";
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
        return userRepository.saveAndFlush(user);
    }

    private void expireConcurrencyGrace(String refreshToken) {
        Jwt jwt = refreshJwtDecoder.decode(refreshToken);
        RefreshTokenSession session = sessionRepository
                .findByFamilyId(jwt.getClaimAsString("family_id")).orElseThrow();
        session.setPreviousConsumedAt(Instant.now().minusSeconds(30));
        sessionRepository.saveAndFlush(session);
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }

    private record RefreshResult(int status, String refreshToken) {
    }
}
