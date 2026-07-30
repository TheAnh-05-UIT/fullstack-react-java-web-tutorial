package com.web_tutorial.javabackend.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.learning.UserLearningProgressRepository;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.security.ratelimit.SensitiveEndpointRateLimiter;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class ApiInputValidationIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DevopsPhaseRepository devopsPhaseRepository;
    @Autowired
    private UserLearningProgressRepository progressRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SensitiveEndpointRateLimiter rateLimiter;

    @BeforeEach
    void ensureUserRoleExists() {
        roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            return roleRepository.saveAndFlush(role);
        });
    }

    @Test
    void registerRejectsInvalidShapeAndDoesNotReflectPassword() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"invalid","password":"secret"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .doesNotContain("\"password\"")
                        .doesNotContain("secret"));

        String oversizedPassword = "a".repeat(ApiInputConstraints.PASSWORD_MAX + 1);
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Valid User","email":"valid@example.test","password":"%s"}
                                """.formatted(oversizedPassword)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Valid User","email":"%s@example.test","password":"short"}
                                """.formatted("a".repeat(ApiInputConstraints.EMAIL_MAX))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Valid User","email":"short-password@example.test","password":" "}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Valid User","email":"too-short@example.test","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerNormalizesEmailButIgnoresPrivilegeFields() throws Exception {
        String email = "normalized-" + UUID.randomUUID() + "@example.test";

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Normal User",
                                  "email":"  %s  ",
                                  "password":"valid password ",
                                  "role":"ADMIN",
                                  "id":999,
                                  "enabled":true
                                }
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userLogin.role").value("USER"));

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRole().getName()).isEqualTo("USER");
        assertThat(saved.getId()).isNotEqualTo(999L);
        assertThat(passwordEncoder.matches("valid password ", saved.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("valid password", saved.getPassword())).isFalse();

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplicate User","email":"%s","password":"valid password "}
                                """.formatted(email)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginDistinguishesMalformedInputFromInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing@example.test","password":"valid-shape"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contentValidationRejectsUnsafeShapeBeforePersistence() throws Exception {
        mockMvc.perform(post("/api/v1/tutorials")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":" ",
                                  "slug":"../unsafe",
                                  "content":"valid",
                                  "status":"NOT_A_STATUS",
                                  "views":999,
                                  "createdAt":"2000-01-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        mockMvc.perform(post("/api/v1/projects")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Project","slug":"project","content":"valid","difficulty":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/users")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"managed-user",
                                  "email":"managed@example.test",
                                  "password":"valid-password",
                                  "role":"SUPER_ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void idsPaginationAndSortUseBoundedAllowlists() throws Exception {
        mockMvc.perform(get("/api/v1/tutorials").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials").param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials").param("size", "100"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tutorials").param("sort", "password,asc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials").param("sort", "title,desc"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tutorials").param("sort", "category.name,asc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials").param("sort", "title,sideways"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials/-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/tutorials/slug/bad_slug"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void learningProgressRejectsInvalidRangeAndKeepsOwnership() throws Exception {
        User owner = createUser();
        String contentKey = createActivePhase();

        mockMvc.perform(put("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(applicationUser(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\":-1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(applicationUser(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(0));
        mockMvc.perform(put("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(applicationUser(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\":101}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(applicationUser(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\":100,\"userId\":999,\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(100))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        UserLearningProgress saved = progressRepository
                .findByUserIdAndContentTypeAndContentKey(
                        owner.getId(), LearningContentType.DEVOPS_PHASE, contentKey)
                .orElseThrow();
        assertThat(saved.getUser().getId()).isEqualTo(owner.getId());
    }

    @Test
    void malformedAndUnsupportedBodiesUseSafeErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.statusCode").value(415))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .doesNotContain("HttpMediaTypeNotSupportedException")
                        .doesNotContain("org.springframework"));
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin() {
        return user("admin@example.test")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor applicationUser(
            User owner) {
        return user(owner.getEmail())
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private User createUser() {
        Role role = roleRepository.findByName("USER").orElseThrow();
        String identity = "validation-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(identity);
        user.setEmail(identity + "@example.test");
        user.setPassword("test-only-not-used");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private String createActivePhase() {
        String key = "validation-" + UUID.randomUUID();
        DevopsPhase phase = new DevopsPhase();
        phase.setPhaseKey(key);
        phase.setTitle("SEC-10 validation test");
        phase.setDisplayOrder(999);
        phase.setActive(true);
        return devopsPhaseRepository.saveAndFlush(phase).getPhaseKey();
    }
}
