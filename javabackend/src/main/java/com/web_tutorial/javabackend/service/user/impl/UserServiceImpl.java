package com.web_tutorial.javabackend.service.user.impl;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.user.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditLogger auditLogger;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            SecurityAuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public List<UserResponseDTO> getAllUserResponses() {
        List<User> listUser = this.getAllUsers();
        return MapperUtils.toUserResponseDTOList(listUser);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public UserResponseDTO getUserResponseById(Long id) {
        User user = this.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));
        return MapperUtils.toUserResponseDTO(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public CreateUserResponseDTO createUserFromDTO(CreateUserRequestDTO requestDTO) throws IdInvalidException {
        if (this.existsUserByEmail(requestDTO.getEmail())) {
            throw new IdInvalidException(
                    "Email " + requestDTO.getEmail() + " already exists, please use another email.");
        }

        User user = MapperUtils.toUser(requestDTO);
        user.setPassword(this.passwordEncoder.encode(requestDTO.getPassword()));

        if (requestDTO.getRole() != null) {
            boolean assigned = this.assignRoleByName(user, requestDTO.getRole());
            if (!assigned) {
                this.assignRoleByName(user, "USER");
            }
        } else {
            this.assignRoleByName(user, "USER");
        }

        if (requestDTO.getAvatar() != null && !requestDTO.getAvatar().isEmpty()) {
            user.setAvatar(requestDTO.getAvatar());
        }

        User createdUser = this.createUser(user);
        return MapperUtils.toCreateUserResponseDTO(createdUser);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        Optional<User> userById = this.userRepository.findById(id);
        if (userById.isPresent()) {
            User userUpdate = userById.get();
            if (userDetails.getUsername() != null)
                userUpdate.setUsername(userDetails.getUsername());
            if (userDetails.getEmail() != null)
                userUpdate.setEmail(userDetails.getEmail());
            if (userDetails.getAvatar() != null)
                userUpdate.setAvatar(userDetails.getAvatar());
            if (userDetails.getRole() != null)
                userUpdate.setRole(userDetails.getRole());
            return this.userRepository.save(userUpdate);
        }
        throw new ResourceNotFoundException("User with id " + id + " not found");
    }

    @Override
    @Transactional
    public UpdateUserResponseDTO updateUserFromDTO(Long id, UpdateUserRequestDTO requestDTO) {
        User existingUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        String oldRole = roleName(existingUser);
        User userDetails = new User();
        MapperUtils.updateUserFromDTO(requestDTO, userDetails);

        if (requestDTO.getRole() != null) {
            this.assignRoleByName(userDetails, requestDTO.getRole());
        }

        if (userDetails.getUsername() != null)
            existingUser.setUsername(userDetails.getUsername());
        if (userDetails.getEmail() != null)
            existingUser.setEmail(userDetails.getEmail());
        if (userDetails.getAvatar() != null)
            existingUser.setAvatar(userDetails.getAvatar());
        if (userDetails.getRole() != null)
            existingUser.setRole(userDetails.getRole());
        User userUpdate = this.userRepository.save(existingUser);
        String newRole = roleName(userUpdate);
        if (!oldRole.equals(newRole)) {
            auditLogger.admin(SecurityAuditEvent.ADMIN_ROLE_CHANGED, auditLogger.currentActor(),
                    "USER", id, "oldRole=" + oldRole + " newRole=" + newRole);
        }
        return MapperUtils.toUpdateUserResponseDTO(userUpdate);
    }

    private String roleName(User user) {
        return user.getRole() == null ? "none" : user.getRole().getName();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!this.userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }
        this.userRepository.deleteById(id);
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public boolean assignRoleByName(User user, String roleName) {
        String normalizedName = roleName.toUpperCase();
        return roleRepository.findByName(normalizedName).map(role -> {
            user.setRole(role);
            return true;
        }).orElse(false);
    }
}
