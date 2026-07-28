package com.web_tutorial.javabackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;

@Component
@ConditionalOnProperty(name = "app.bootstrap.admin.enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.email:}") String adminEmail,
            @Value("${app.bootstrap.admin.password:}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim();
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        validateConfiguration();

        // 1. Khởi tạo role USER nếu chưa tồn tại
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            log.info("Creating default USER role...");
            Role role = new Role();
            role.setName("USER");
            role.setDescription("Default user role with standard permissions");
            return roleRepository.save(role);
        });

        // 2. Khởi tạo role ADMIN nếu chưa tồn tại
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            log.info("Creating default ADMIN role...");
            Role role = new Role();
            role.setName("ADMIN");
            role.setDescription("Administrator role with full access permissions");
            return roleRepository.save(role);
        });

        // 3. Khởi tạo tài khoản Admin mặc định nếu database chưa có
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating default administrator account with email: {}", adminEmail);
            User admin = new User();
            admin.setUsername("Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("Default administrator account created successfully. Email: {}", adminEmail);
        }
    }

    private void validateConfiguration() {
        if (adminEmail.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but app.bootstrap.admin.email is missing");
        }
        if (adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but app.bootstrap.admin.password is missing");
        }
        if (adminPassword.length() < 12
                || !adminPassword.matches(".*[A-Z].*")
                || !adminPassword.matches(".*[a-z].*")
                || !adminPassword.matches(".*\\d.*")
                || !adminPassword.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalStateException(
                    "Admin bootstrap password must be at least 12 characters and include upper, lower, digit, and special characters");
        }
    }
}
