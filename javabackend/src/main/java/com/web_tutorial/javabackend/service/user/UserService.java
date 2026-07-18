package com.web_tutorial.javabackend.service.user;

import java.util.Optional;
import java.util.List;

import com.web_tutorial.javabackend.domain.dto.request.user.CreateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.user.UpdateUserRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.CreateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UpdateUserResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.user.UserResponseDTO;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.exception.IdInvalidException;

public interface UserService {
    // Lấy tất cả user (trả về Entity - phục vụ nội bộ/backward compatibility)
    List<User> getAllUsers();

    // Lấy tất cả user (trả về Response DTO - phục vụ Controller)
    List<UserResponseDTO> getAllUserResponses();

    // Tìm user theo ID (trả về Entity)
    Optional<User> getUserById(Long id);

    // Tìm user theo ID (trả về Response DTO hoặc ném ngoại lệ 404)
    UserResponseDTO getUserResponseById(Long id);

    // Tìm user theo Username
    Optional<User> getUserByUsername(String username);

    // Đăng ký/Tạo mới user (trả về Entity - phục vụ AuthServiceImpl)
    User createUser(User user);

    // Tạo mới user từ DTO (phục vụ Controller - bao gồm kiểm tra trùng, mã hóa mật khẩu, gán role)
    CreateUserResponseDTO createUserFromDTO(CreateUserRequestDTO requestDTO) throws IdInvalidException;

    // Cập nhật thông tin user (trả về Entity)
    User updateUser(Long id, User userDetails);

    // Cập nhật thông tin user từ DTO (phục vụ Controller - bao gồm gán role và mapping DTO)
    UpdateUserResponseDTO updateUserFromDTO(Long id, UpdateUserRequestDTO requestDTO);

    // Xóa user (ném ngoại lệ 404 nếu không tồn tại)
    void deleteUser(Long id);

    // Tồn tại email
    boolean existsUserByEmail(String email);

    // Tìm user theo Email
    Optional<User> getUserByEmail(String email);

    // Lưu Refresh Token vào Database
    void updateRefreshToken(String email, String refreshToken);

    // Tìm User bằng Refresh Token
    Optional<User> getUserByRefreshToken(String refreshToken);

    // Thu hồi Refresh Token khi user logout
    void revokeRefreshToken(String email);

    // Gán role cho user theo tên role ("USER" hoặc "ADMIN")
    // Trả về false nếu role không tồn tại
    boolean assignRoleByName(User user, String roleName);
}
