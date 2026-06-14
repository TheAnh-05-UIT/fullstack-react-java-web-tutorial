package com.web_tutorial.javabackend.service.user;

import com.web_tutorial.javabackend.model.user.User;
import java.util.Optional;
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
}
