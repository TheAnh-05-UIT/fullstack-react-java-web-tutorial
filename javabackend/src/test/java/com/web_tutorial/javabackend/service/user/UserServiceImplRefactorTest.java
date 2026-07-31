package com.web_tutorial.javabackend.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.Role;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.user.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplRefactorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityAuditLogger auditLogger;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, roleRepository, passwordEncoder, auditLogger);
    }

    @Test
    void testGetAllUserResponses() {
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("Alice");
        when(userRepository.findAll()).thenReturn(List.of(u1));

        List<UserResponseDTO> result = userService.getAllUserResponses();
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getUsername());
    }

    @Test
    void testGetUserResponseById_found() {
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("Bob");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));

        UserResponseDTO result = userService.getUserResponseById(1L);
        assertNotNull(result);
        assertEquals("Bob", result.getUsername());
    }

    @Test
    void testGetUserResponseById_notFoundThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserResponseById(99L));
    }

    @Test
    void testCreateUserFromDTO_duplicateEmailThrowsException() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setEmail("duplicate@example.com");
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertThrows(IdInvalidException.class, () -> userService.createUserFromDTO(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUserFromDTO_successWithRoleFallback() throws IdInvalidException {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername("Charlie");
        dto.setEmail("charlie@example.com");
        dto.setPassword("rawpass");
        dto.setRole("INVALID_ROLE");

        when(userRepository.existsByEmail("charlie@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawpass")).thenReturn("encodedpass");
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        Role fallbackRole = new Role();
        fallbackRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(fallbackRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        CreateUserResponseDTO result = userService.createUserFromDTO(dto);
        assertNotNull(result);
        assertEquals("Charlie", result.getUsername());
        verify(passwordEncoder, times(1)).encode("rawpass");
        verify(roleRepository, times(1)).findByName("INVALID_ROLE");
        verify(roleRepository, times(1)).findByName("USER");
    }

    @Test
    void testDeleteUser_notFoundThrowsException() {
        when(userRepository.existsById(50L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(50L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteUser_success() {
        when(userRepository.existsById(50L)).thenReturn(true);
        userService.deleteUser(50L);
        verify(userRepository, times(1)).deleteById(50L);
    }
}
