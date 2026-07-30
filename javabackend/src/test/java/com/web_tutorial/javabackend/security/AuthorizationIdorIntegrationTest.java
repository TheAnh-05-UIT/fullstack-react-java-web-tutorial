package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;
import com.web_tutorial.javabackend.repository.learning.UserLearningProgressRepository;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationIdorIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DevopsPhaseRepository devopsPhaseRepository;
    @Autowired
    private UserLearningProgressRepository progressRepository;

    @Test
    void privateAndAdminEndpointsEnforceAuthenticationAndRole() throws Exception {
        User target = createUser("target", "USER");
        User regularUser = createUser("regular", "USER");

        mockMvc.perform(get("/api/v1/users/" + target.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/users/" + target.getId()).with(asUser(regularUser)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/users/" + target.getId())
                        .with(asUser(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"hijacked","avatar":"/attacker.png","role":"ADMIN"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/users/" + target.getId()).with(asUser(regularUser)))
                .andExpect(status().isForbidden());

        User unchanged = userRepository.findById(target.getId()).orElseThrow();
        assertThat(unchanged.getUsername()).isEqualTo(target.getUsername());
        assertThat(unchanged.getRole().getName()).isEqualTo("USER");
    }

    @Test
    void adminCanManageUsersButRegularUserCannotMutateAdminContent() throws Exception {
        User target = createUser("managed", "USER");
        User regularUser = createUser("content-user", "USER");
        User admin = createUser("content-admin", "ADMIN");

        mockMvc.perform(put("/api/v1/users/" + target.getId())
                        .with(asAdmin(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"managed-by-admin","avatar":"/default-avatar.png","role":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("managed-by-admin"))
                .andExpect(jsonPath("$.data.role.name").value("ADMIN"));

        mockMvc.perform(post("/api/v1/tutorials")
                        .with(asUser(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/devops/phases")
                        .with(asUser(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/upload")
                        .with(asUser(regularUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationIgnoresPrivilegeAndInternalFields() throws Exception {
        String email = "mass-assignment-" + UUID.randomUUID() + "@example.test";

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Mass Assignment User",
                                  "email":"%s",
                                  "password":"Test-password-123!",
                                  "role":"ADMIN",
                                  "enabled":true,
                                  "isDeleted":false,
                                  "userId":999,
                                  "createdAt":"2000-01-01T00:00:00Z"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userLogin.role").value("USER"));

        User registered = userRepository.findByEmail(email).orElseThrow();
        assertThat(registered.getRole().getName()).isEqualTo("USER");
        assertThat(registered.getId()).isNotEqualTo(999L);
        assertThat(registered.getCreatedAt()).isAfter(Instant.parse("2000-01-01T00:00:00Z"));
    }

    @Test
    void learningProgressRemainsScopedToAuthenticatedUser() throws Exception {
        User userA = createUser("progress-a", "USER");
        User userB = createUser("progress-b", "USER");
        String contentKey = createActiveDevopsPhase();

        UserLearningProgress progressB = new UserLearningProgress();
        progressB.setUser(userB);
        progressB.setContentType(LearningContentType.DEVOPS_PHASE);
        progressB.setContentKey(contentKey);
        progressB.setStatus(LearningProgressStatus.IN_PROGRESS);
        progressB.setProgressPercent(65);
        progressB.setLastAccessedAt(Instant.now());
        progressRepository.saveAndFlush(progressB);

        mockMvc.perform(get("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(asUser(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.progressPercent").value(0));

        mockMvc.perform(put("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(asUser(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"progressPercent":20,"userId":%d,"status":"COMPLETED"}
                                """.formatted(userB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(20))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        assertThat(progressRepository.findByUserIdAndContentTypeAndContentKey(
                userB.getId(), LearningContentType.DEVOPS_PHASE, contentKey)
                .orElseThrow().getProgressPercent()).isEqualTo(65);

        mockMvc.perform(delete("/api/v1/learning-progress/me/DEVOPS_PHASE/" + contentKey)
                        .with(asUser(userA)))
                .andExpect(status().isNoContent());

        assertThat(progressRepository.findByUserIdAndContentTypeAndContentKey(
                userA.getId(), LearningContentType.DEVOPS_PHASE, contentKey)).isEmpty();
        assertThat(progressRepository.findByUserIdAndContentTypeAndContentKey(
                userB.getId(), LearningContentType.DEVOPS_PHASE, contentKey)).isPresent();
    }

    @Test
    void learningProgressRejectsAnonymousAndPrincipalWithoutApplicationRole() throws Exception {
        mockMvc.perform(get("/api/v1/learning-progress/me/summary"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/learning-progress/me/summary")
                        .with(user("roleless@example.test").authorities()))
                .andExpect(status().isForbidden());
    }

    private User createUser(String prefix, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role created = new Role();
            created.setName(roleName);
            return roleRepository.saveAndFlush(created);
        });
        String identity = prefix + "-" + UUID.randomUUID();
        User user = new User();
        user.setUsername(identity);
        user.setEmail(identity + "@example.test");
        user.setPassword("test-only-not-used");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private String createActiveDevopsPhase() {
        String key = "idor-" + UUID.randomUUID();
        DevopsPhase phase = new DevopsPhase();
        phase.setPhaseKey(key);
        phase.setTitle("SEC-6 ownership test");
        phase.setDisplayOrder(999);
        phase.setActive(true);
        devopsPhaseRepository.saveAndFlush(phase);
        return key;
    }

    private UserRequestPostProcessor asUser(User userEntity) {
        return user(userEntity.getEmail())
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private UserRequestPostProcessor asAdmin(User userEntity) {
        return user(userEntity.getEmail())
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
