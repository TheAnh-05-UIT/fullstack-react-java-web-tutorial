package com.web_tutorial.javabackend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    private static final String ADMIN_EMAIL = "bootstrap@example.test";
    private static final String VALID_PASSWORD = "StrongPassword1!";

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void missingEnabledPropertyDoesNotCreateInitializerBean() {
        contextRunner().run(context -> assertThat(context).doesNotHaveBean(DataInitializer.class));
    }

    @Test
    void explicitlyDisabledPropertyDoesNotCreateInitializerBean() {
        contextRunner()
                .withPropertyValues("app.bootstrap.admin.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DataInitializer.class));
    }

    @Test
    void missingEmailFailsBeforeDatabaseWrites() {
        DataInitializer initializer = initializer("", VALID_PASSWORD);

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email is missing");
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void missingPasswordFailsBeforeDatabaseWrites() {
        DataInitializer initializer = initializer(ADMIN_EMAIL, "");

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password is missing");
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void weakPasswordFailsBeforeDatabaseWrites() {
        DataInitializer initializer = initializer(ADMIN_EMAIL, "weak-password");

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 12 characters");
        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void validConfigurationCreatesRolesAndBcryptEncodedAdmin() throws Exception {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(false);
        PasswordEncoder bcrypt = new BCryptPasswordEncoder();

        initializer(ADMIN_EMAIL, VALID_PASSWORD, bcrypt).run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(saved.getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(bcrypt.matches(VALID_PASSWORD, saved.getPassword())).isTrue();
        assertThat(saved.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void rerunDoesNotCreateDuplicateOrOverwriteExistingAdmin() throws Exception {
        Role userRole = role("USER");
        Role adminRole = role("ADMIN");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(true);

        DataInitializer initializer = initializer(ADMIN_EMAIL, VALID_PASSWORD);
        initializer.run();
        initializer.run();

        verify(userRepository, times(2)).existsByEmail(ADMIN_EMAIL);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    private DataInitializer initializer(String email, String password) {
        return initializer(email, password, passwordEncoder);
    }

    private DataInitializer initializer(String email, String password, PasswordEncoder encoder) {
        return new DataInitializer(roleRepository, userRepository, encoder, email, password);
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withUserConfiguration(DataInitializer.class)
                .withBean(RoleRepository.class, () -> roleRepository)
                .withBean(UserRepository.class, () -> userRepository)
                .withBean(PasswordEncoder.class, () -> passwordEncoder);
    }
}
