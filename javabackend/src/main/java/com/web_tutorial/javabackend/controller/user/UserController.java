package com.web_tutorial.javabackend.controller.user;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.user.UserService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tất cả endpoint /api/v1/users yêu cầu role ADMIN.
// @PreAuthorize kiểm tra authority từ JWT claim "scope" sau khi strip prefix ROLE_.
// Nếu user không đủ quyền, SecurityConfiguration sẽ trả 403 Forbidden.
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @ApiMessage("Get All Users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> listUser = this.userService.getAllUsers();
        List<UserResponseDTO> listUserResponseDTOs = MapperUtils.toUserResponseDTOList(listUser);
        return ResponseEntity.status(HttpStatus.OK).body(listUserResponseDTOs);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        // ResourceNotFoundException → 404 NOT FOUND
        // IdInvalidException → 400 BAD REQUEST
        User user = this.userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));
        UserResponseDTO userResponseDTO = MapperUtils.toUserResponseDTO(user);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a User")
    public ResponseEntity<CreateUserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO requestDTO)
            throws IdInvalidException {
        // Kiểm tra email trùng trước khi tạo
        if (this.userService.existsUserByEmail(requestDTO.getEmail())) {
            throw new IdInvalidException(
                    "Email " + requestDTO.getEmail() + " already exists, please use another email.");
        }

        User user = MapperUtils.toUser(requestDTO);
        user.setPassword(this.passwordEncoder.encode(requestDTO.getPassword()));

        // Gán role qua UserService thay vì truy cập RoleRepository trực tiếp
        if (requestDTO.getRole() != null) {
            // Nếu role không tồn tại trong DB → fallback về USER
            boolean assigned = this.userService.assignRoleByName(user, requestDTO.getRole());
            if (!assigned) {
                this.userService.assignRoleByName(user, "USER");
            }
        } else {
            this.userService.assignRoleByName(user, "USER");
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

        // Gán role qua UserService nếu có cung cấp
        if (requestDTO.getRole() != null) {
            this.userService.assignRoleByName(userDetails, requestDTO.getRole());
        }

        // updateUser ném ResourceNotFoundException (404) nếu không tìm thấy user
        User userUpdate = this.userService.updateUser(id, userDetails);
        UpdateUserResponseDTO updateUserResponseDTO = MapperUtils.toUpdateUserResponseDTO(userUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(updateUserResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a User")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Kiểm tra tồn tại trước khi xóa → 404 nếu không tìm thấy
        this.userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
