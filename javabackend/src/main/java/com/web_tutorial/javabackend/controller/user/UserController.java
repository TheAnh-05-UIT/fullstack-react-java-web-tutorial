package com.web_tutorial.javabackend.controller.user;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.web_tutorial.javabackend.repository.user.RoleRepository;
import com.web_tutorial.javabackend.domain.user.Role;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @ApiMessage("Get All User")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> listUser = this.userService.getAllUsers();
        List<UserResponseDTO> listUserResponseDTOs = MapperUtils.toUserResponseDTOList(listUser);
        return ResponseEntity.status(HttpStatus.OK).body(listUserResponseDTOs);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) throws IdInvalidException {
        Optional<User> userById = this.userService.getUserById(id);
        if (!userById.isPresent()) {
            throw new IdInvalidException("Id " + id + " does not exist");
        }
        UserResponseDTO userResponseDTO = MapperUtils.toUserResponseDTO(userById.get());
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a User")
    public ResponseEntity<CreateUserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO requestDTO)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.existsUserByEmail(requestDTO.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + requestDTO.getEmail() + " already exists, please use another email.");
        }
        User user = MapperUtils.toUser(requestDTO);
        user.setPassword(this.passwordEncoder.encode(requestDTO.getPassword()));

        if (requestDTO.getRole() != null) {
            String roleName = requestDTO.getRole().toUpperCase();
            Optional<Role> roleOpt = this.roleRepository.findByName(roleName);
            if (roleOpt.isPresent()) {
                user.setRole(roleOpt.get());
            } else {
                user.setRole(this.roleRepository.findByName("USER").orElse(null));
            }
        } else {
            user.setRole(this.roleRepository.findByName("USER").orElse(null));
        }

        if (requestDTO.getAvatar() != null && !requestDTO.getAvatar().isEmpty()) {
            user.setAvatar(requestDTO.getAvatar());
        }

        User createdUser = userService.createUser(user);
        CreateUserResponseDTO userResponseDTO = MapperUtils.toCreateUserResponseDTO(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a User")
    public ResponseEntity<UpdateUserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequestDTO requestDTO) throws IdInvalidException {
        User userDetails = new User();
        MapperUtils.updateUserFromDTO(requestDTO, userDetails);

        if (requestDTO.getRole() != null) {
            String roleName = requestDTO.getRole().toUpperCase();
            Optional<Role> roleOpt = this.roleRepository.findByName(roleName);
            if (roleOpt.isPresent()) {
                userDetails.setRole(roleOpt.get());
            }
        }

        User userUpdate = this.userService.updateUser(id, userDetails);
        if (userUpdate == null) {
            throw new IdInvalidException("User with Id " + id + " is invalid");
        }
        UpdateUserResponseDTO updateUserResponseDTO = MapperUtils.toUpdateUserResponseDTO(userUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(updateUserResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a User")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws IdInvalidException {
        Optional<User> deleteUser = this.userService.getUserById(id);
        if (!deleteUser.isPresent()) {
            throw new IdInvalidException("Id " + id + " does not exist");
        }
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
