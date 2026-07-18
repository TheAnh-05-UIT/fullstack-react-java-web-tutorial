package com.web_tutorial.javabackend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.service.user.impl.UserDetailsImpl;
import com.web_tutorial.javabackend.service.user.impl.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserLazyRoleAndSecurityTest {

    @Mock
    private UserService userService;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userService);
    }

    @Test
    void testUserRoleIsFetchTypeLazy() throws NoSuchFieldException {
        Field roleField = User.class.getDeclaredField("role");
        ManyToOne annotation = roleField.getAnnotation(ManyToOne.class);
        assertNotNull(annotation);
        assertEquals(FetchType.LAZY, annotation.fetch());
    }

    @Test
    void testLoadUserByUsername_roleAvailableWithoutLazyException() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        User user = new User();
        user.setId(100L);
        user.setEmail("admin@test.com");
        user.setPassword("secret");
        user.setRole(adminRole);

        when(userService.getUserByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@test.com");
        assertNotNull(userDetails);
        assertEquals("admin@test.com", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testUserDetailsImpl_fallbackWhenRoleIsNull() {
        User user = new User();
        user.setId(101L);
        user.setEmail("user@test.com");
        user.setPassword("secret");
        user.setRole(null);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
