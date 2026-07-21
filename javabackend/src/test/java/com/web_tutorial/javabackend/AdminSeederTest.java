package com.web_tutorial.javabackend;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;

@SpringBootTest
public class AdminSeederTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void seedAdminUser() {
        Optional<Role> adminRoleOpt = roleRepository.findByName("ADMIN");
        Role adminRole;
        if (adminRoleOpt.isEmpty()) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);
        } else {
            adminRole = adminRoleOpt.get();
        }

        Optional<User> existingAdminOpt = userRepository.findByEmail("admin@example.com");
        if (existingAdminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("Admin User");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println("=== ADMIN USER CREATED SUCCESSFULLY ===");
            System.out.println("Email: admin@example.com");
            System.out.println("Password: admin123");
        } else {
            System.out.println("=== ADMIN USER ALREADY EXISTS ===");
            System.out.println("Email: admin@example.com");
        }
    }
}
