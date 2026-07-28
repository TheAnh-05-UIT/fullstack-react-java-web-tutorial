package com.web_tutorial.javabackend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;

import com.web_tutorial.javabackend.config.SecurityConfiguration;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.service.security.SecurityService;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class TokenTypeSecurityIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    @Qualifier("refreshJwtDecoder")
    private JwtDecoder refreshJwtDecoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void refreshTokenCannotAuthenticateProtectedEndpoint() throws Exception {
        String refreshToken = securityService.generateRefreshToken("stolen@example.test");

        mockMvc.perform(post("/api/v1/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenCannotAuthenticateAdminEndpoint() throws Exception {
        String refreshToken = securityService.generateRefreshToken("stolen-admin@example.test");

        mockMvc.perform(get("/api/v1/tutorials/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenCanAuthenticateProtectedEndpoint() throws Exception {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "user@example.test",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(post("/api/v1/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void userAccessTokenCannotAccessAdminEndpoint() throws Exception {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "user@example.test",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(get("/api/v1/tutorials/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccessTokenCanAccessAdminEndpoint() throws Exception {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin@example.test",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        mockMvc.perform(get("/api/v1/tutorials/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void tokenWithoutTypeCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("web-tutorial", "webtutorial-api", null,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString()));
    }

    @Test
    void tokenWithUnknownTypeCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("web-tutorial", "webtutorial-api", "unknown",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString()));
    }

    @Test
    void accessTokenWithWrongAudienceCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("web-tutorial", "webtutorial-auth", "access",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString()));
    }

    @Test
    void accessTokenWithWrongIssuerCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("another-issuer", "webtutorial-api", "access",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString()));
    }

    @Test
    void accessTokenWithoutJwtIdCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("web-tutorial", "webtutorial-api", "access",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), null));
    }

    @Test
    void expiredAccessTokenCannotAuthenticate() throws Exception {
        assertProtectedEndpointRejects(token("web-tutorial", "webtutorial-api", "access",
                Instant.now().minusSeconds(600), Instant.now().minusSeconds(300), UUID.randomUUID().toString()));
    }

    @Test
    void unknownScopeDoesNotCreateAuthority() throws Exception {
        String token = token("web-tutorial", "webtutorial-api", "access",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString(),
                "ROLE_SUPERUSER");

        mockMvc.perform(get("/api/v1/tutorials/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatedTokensContainSeparatedClaimsAndAuthorities() {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "claims@example.test",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_UNKNOWN"))));
        String refreshToken = securityService.generateRefreshToken("claims@example.test");

        Jwt access = jwtDecoder.decode(accessToken);
        Jwt refresh = refreshJwtDecoder.decode(refreshToken);

        assertThat(access.getClaimAsString("iss")).isEqualTo("web-tutorial");
        assertThat(access.getAudience()).containsExactly("webtutorial-api");
        assertThat(access.getClaimAsString("token_type")).isEqualTo("access");
        assertThat(access.getId()).isNotBlank();
        assertThat(access.getClaimAsString("scope")).isEqualTo("ROLE_USER");
        assertThat(access.getExpiresAt()).isAfter(access.getIssuedAt());

        assertThat(refresh.getClaimAsString("iss")).isEqualTo("web-tutorial");
        assertThat(refresh.getAudience()).containsExactly("webtutorial-auth");
        assertThat(refresh.getClaimAsString("token_type")).isEqualTo("refresh");
        assertThat(refresh.getId()).isNotBlank();
        assertThat(refresh.getClaims()).doesNotContainKey("scope");
        assertThat(refresh.getExpiresAt()).isAfter(refresh.getIssuedAt());
        assertThat(refresh.getExpiresAt()).isAfter(access.getExpiresAt());
    }

    @Test
    void dedicatedDecodersRejectTheOtherTokenType() {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "cross-policy@example.test", null, List.of()));
        String refreshToken = securityService.generateRefreshToken("cross-policy@example.test");

        assertThatThrownBy(() -> jwtDecoder.decode(refreshToken)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> refreshJwtDecoder.decode(accessToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void validRefreshRotatesPairAndOldTokenIsRejected() throws Exception {
        User user = createUser("rotation-" + UUID.randomUUID() + "@example.test");
        String refreshToken = securityService.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/refresh")
                        .contentType("application/json")
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/refresh")
                        .contentType("application/json")
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginPersistsCompleteRefreshTokenWithoutTruncation() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.test";
        String password = "Test-password-123!";
        User user = createUser(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.saveAndFlush(user);

        String response = mockMvc.perform(post("/api/v1/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        String responseRefreshToken = body.at("/data/refreshToken").asText();
        String storedRefreshToken = userRepository.findByEmail(email).orElseThrow().getRefreshToken();
        assertThat(storedRefreshToken)
                .hasSize(responseRefreshToken.length())
                .isEqualTo(responseRefreshToken);
    }

    @Test
    void generatedTokenSizesFitColumnWithHeadroom() {
        String longEmail = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + ".example";
        String userAccess = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        longEmail, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        String adminAccess = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        longEmail, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        String refresh = securityService.generateRefreshToken(longEmail);

        assertThat(userAccess.length()).isLessThan(2048);
        assertThat(adminAccess.length()).isLessThan(2048);
        assertThat(refresh.length()).isLessThan(2048);
        System.out.printf(
                "JWT length evidence only: ROLE_USER access=%d, ROLE_ADMIN access=%d, max-subject refresh=%d%n",
                userAccess.length(), adminAccess.length(), refresh.length());
    }

    @Test
    void accessTokenCannotBeUsedAtRefreshEndpoint() throws Exception {
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        "access-at-refresh@example.test", null, List.of()));

        assertRefreshRejects(accessToken);
    }

    @Test
    void validRefreshJwtThatDoesNotMatchDatabaseIsRejected() throws Exception {
        assertRefreshRejects(securityService.generateRefreshToken("not-stored@example.test"));
    }

    @Test
    void refreshEndpointRejectsWrongIssuerAndExpiredRefreshTokens() throws Exception {
        assertRefreshRejects(token("wrong-issuer", "webtutorial-auth", "refresh",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString(), ""));
        assertRefreshRejects(token("web-tutorial", "webtutorial-auth", "refresh",
                Instant.now().minusSeconds(600), Instant.now().minusSeconds(300), UUID.randomUUID().toString(), ""));
    }

    @Test
    void refreshEndpointRejectsWrongAudienceMissingTypeAndMalformedToken() throws Exception {
        assertRefreshRejects(token("web-tutorial", "webtutorial-api", "refresh",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString(), ""));
        assertRefreshRejects(token("web-tutorial", "webtutorial-auth", null,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300), UUID.randomUUID().toString(), ""));
        assertRefreshRejects("not-a-jwt");
    }

    @Test
    void resourceServerRejectsMalformedAndInvalidSignatureTokens() throws Exception {
        assertProtectedEndpointRejects("not-a-jwt");
        String valid = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated("tampered@example.test", null, List.of()));
        String[] segments = valid.split("\\.");
        segments[2] = (segments[2].startsWith("a") ? "b" : "a") + segments[2].substring(1);
        String tampered = String.join(".", segments);
        assertProtectedEndpointRejects(tampered);
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        User user = createUser("logout-" + UUID.randomUUID() + "@example.test");
        String refreshToken = securityService.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        userRepository.saveAndFlush(user);
        String accessToken = securityService.generateAccessToken(
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(post("/api/v1/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertRefreshRejects(refreshToken);
    }

    private void assertProtectedEndpointRejects(String token) throws Exception {
        mockMvc.perform(post("/api/v1/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private void assertRefreshRejects(String token) throws Exception {
        mockMvc.perform(post("/api/v1/refresh")
                        .contentType("application/json")
                        .content(refreshBody(token)))
                .andExpect(status().isUnauthorized());
    }

    private String refreshBody(String token) {
        return "{\"refreshToken\":\"" + token + "\"}";
    }

    private User createUser(String email) {
        Role role = roleRepository.findByName("USER").orElseGet(() -> {
            Role created = new Role();
            created.setName("USER");
            return roleRepository.saveAndFlush(created);
        });
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword("test-only-not-used");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private String token(String issuer, String audience, String tokenType, Instant issuedAt,
            Instant expiresAt, String jwtId) {
        return token(issuer, audience, tokenType, issuedAt, expiresAt, jwtId, "ROLE_USER");
    }

    private String token(String issuer, String audience, String tokenType, Instant issuedAt,
            Instant expiresAt, String jwtId, String scope) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject("user@example.test")
                .claim("scope", scope);
        if (tokenType != null) {
            claims.claim("token_type", tokenType);
        }
        if (jwtId != null) {
            claims.id(jwtId);
        }
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims.build())).getTokenValue();
    }
}
