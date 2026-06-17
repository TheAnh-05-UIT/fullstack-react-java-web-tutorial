package com.web_tutorial.javabackend.service.user;

import java.util.Optional;

import com.web_tutorial.javabackend.domain.user.User;

import java.util.List;

public interface UserService {
    // Lấy tất cả user
    List<User> getAllUsers();

    // Tìm user theo ID
    Optional<User> getUserById(Long id);

    // Tìm user theo Username
    Optional<User> getUserByUsername(String username);

    // Đăng ký/Tạo mới user
    User createUser(User user);

    // Cập nhật thông tin user
    User updateUser(Long id, User userDetails);

    // Xóa user (Soft Delete hoặc Hard Delete)
    void deleteUser(Long id);

    // Tồn tại email
    boolean existsUserByEmail(String email);

    // Tìm user theo Email
    Optional<User> getUserByEmail(String email);

    // Lưu Refresh Token vào Database
    void updateRefreshToken(String email, String refreshToken);

    // Tìm User bằng Refresh Token
    Optional<User> getUserByRefreshToken(String refreshToken);
}
