package com.web_tutorial.javabackend.controller.user;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tất cả endpoint /api/v1/users yêu cầu role ADMIN.
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Validated
public class UserController {

    private final UserService userService;
    private final SecurityAuditLogger auditLogger;

    public UserController(UserService userService, SecurityAuditLogger auditLogger) {
        this.userService = userService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    @ApiMessage("Get All Users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUserResponses());
    }

    @GetMapping("/{id}")
    @ApiMessage("Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable @Positive Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getUserResponseById(id));
    }

    @PostMapping
    @ApiMessage("Create a User")
    public ResponseEntity<CreateUserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO requestDTO)
            throws IdInvalidException {
        CreateUserResponseDTO response = this.userService.createUserFromDTO(requestDTO);
        auditLogger.admin(SecurityAuditEvent.ADMIN_USER_CREATED, auditLogger.currentActor(),
                "USER", response.getId(), "USER_CREATED");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a User")
    public ResponseEntity<UpdateUserResponseDTO> updateUser(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateUserRequestDTO requestDTO) throws IdInvalidException {
        UpdateUserResponseDTO response = this.userService.updateUserFromDTO(id, requestDTO);
        auditLogger.admin(SecurityAuditEvent.ADMIN_USER_UPDATED, auditLogger.currentActor(),
                "USER", id, "PROFILE_FIELDS");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a User")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long id) {
        this.userService.deleteUser(id);
        auditLogger.admin(SecurityAuditEvent.ADMIN_USER_DELETED, auditLogger.currentActor(),
                "USER", id, "USER_DELETED");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
