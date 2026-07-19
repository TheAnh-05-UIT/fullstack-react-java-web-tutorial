package com.web_tutorial.javabackend.domain.devops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Tập hợp các DTO (Data Transfer Object) dành cho module DevOps Lifecycle
 * Content.
 *
 * Cho phép Admin quản lý toàn bộ nội dung của từng giai đoạn (Curriculum,
 * Tools, Learning Path, Quiz, Labs)
 * một cách trực quan mà không cần dùng đến kịch bản giả lập Terminal.
 */
public class DevopsDTOs {
    /**
     * DTO tóm tắt thông tin một Giai đoạn DevOps (dùng cho danh sách / sidebar).
     */
    public record PhaseResponse(
            Long id,
            String phaseKey,
            String title,
            String name,
            String tagline,
            String iconName,
            String colorGradient,
            Integer displayOrder,
            boolean active) {
    }

    /**
     * DTO đầy đủ toàn bộ nội dung bài học của một Giai đoạn DevOps.
     * Dùng khi hiển thị trang chi tiết học viên hoặc khi Admin tải lên để chỉnh
     * sửa.
     * Các trường curriculum, tools, learningPath, quiz, handsOnLabs, theme trả về
     * dưới dạng Object JSON mảng/bảng.
     */
    public record PhaseDetailResponse(
            Long id,
            String phaseKey,
            String title,
            String name,
            String tagline,
            String summary,
            String heroSnippetTitle,
            String heroSnippet,
            String iconName,
            String colorGradient,
            Integer displayOrder,
            boolean active,
            Object theme,
            Object curriculum,
            Object tools,
            Object learningPath,
            Object quiz,
            Object handsOnLabs) {
    }

    /**
     * DTO tạo/cập nhật toàn bộ nội dung một Giai đoạn DevOps.
     * Chỉ ROLE_ADMIN mới được phép thực thi.
     */
    public record PhaseRequest(
            @NotBlank(message = "phaseKey (slug) không được để trống") String phaseKey,

            @NotBlank(message = "title không được để trống") String title,

            String name,
            String tagline,
            String summary,
            String heroSnippetTitle,
            String heroSnippet,
            String iconName,
            String colorGradient,

            @NotNull(message = "displayOrder không được để trống") @Min(value = 1, message = "displayOrder phải >= 1") Integer displayOrder,

            boolean active,
            Object theme,
            Object curriculum,
            Object tools,
            Object learningPath,
            Object quiz,
            Object handsOnLabs) {
    }
}
