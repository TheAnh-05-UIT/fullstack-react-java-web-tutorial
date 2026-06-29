package com.web_tutorial.javabackend.domain.user;

import java.time.Instant;

import com.web_tutorial.javabackend.service.security.SecurityService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Model User: Quản lý thông tin tài khoản đăng nhập và phân quyền hệ thống.
 *
 * Dùng Lombok thay thế getter/setter thủ công để code ngắn gọn, an toàn hơn
 * (tránh quên thêm accessor khi thêm field mới).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thông tin cơ bản
    private String username;
    private String email;
    private String password; // Mật khẩu đã được mã hóa
    private String avatar; // Đường dẫn URL ảnh đại diện

    private String refreshToken;

    // Lưu thời gian và người thao tác
    private Instant createdAt;
    private String createBy;
    private Instant updatedAt;
    private String updateBy;

    // Liên kết với bảng phân quyền Role (user many-to-one role)
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Author author;

    // Tự động set audit fields khi tạo mới
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.createBy = SecurityService.getCurrentUserLogin().orElse("System");
    }

    // Tự động set audit fields khi cập nhật
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        this.updateBy = SecurityService.getCurrentUserLogin().orElse("System");
    }
}
