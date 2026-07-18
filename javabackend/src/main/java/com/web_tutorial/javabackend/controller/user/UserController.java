package com.web_tutorial.javabackend.controller.user;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tất cả endpoint /api/v1/users yêu cầu role ADMIN.
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ApiMessage("Get All Users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUserResponses());
    }

    @GetMapping("/{id}")
    @ApiMessage("Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getUserResponseById(id));
    }

    @PostMapping
    @ApiMessage("Create a User")
    public ResponseEntity<CreateUserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO requestDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUserFromDTO(requestDTO));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a User")
    public ResponseEntity<UpdateUserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequestDTO requestDTO) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.updateUserFromDTO(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a User")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
