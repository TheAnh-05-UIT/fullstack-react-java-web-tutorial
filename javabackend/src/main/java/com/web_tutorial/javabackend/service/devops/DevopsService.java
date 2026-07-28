package com.web_tutorial.javabackend.service.devops;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.domain.devops.dto.DevopsDTOs;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;

/**
 * Service xử lý nghiệp vụ (Business Logic) cho module DevOps Lifecycle Content.
 * Mọi truy vấn/thao tác Database đều đi qua tầng Service này.
 *
 * - Các phương thức READ: Công khai, học viên được gọi.
 * - Các phương thức WRITE (create/update/delete): Bảo vệ bởi @PreAuthorize ở
 * Controller.
 */
@Service
@Transactional
public class DevopsService {

    private final DevopsPhaseRepository phaseRepository;
    private final ObjectMapper objectMapper;

    public DevopsService(DevopsPhaseRepository phaseRepository, ObjectMapper objectMapper) {
        this.phaseRepository = phaseRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lấy danh sách tóm tắt tất cả các giai đoạn đang ACTIVE.
     * Dùng để vẽ thanh hướng dẫn / sidebar bên Frontend.
     */
    @Transactional(readOnly = true)
    public List<DevopsDTOs.PhaseResponse> getActivePhases() {
        return phaseRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toPhaseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách chi tiết TẤT CẢ các giai đoạn (cả ẩn lẫn hiện).
     * Dùng cho trang quản trị Admin CRUD.
     */
    @Transactional(readOnly = true)
    public List<DevopsDTOs.PhaseDetailResponse> getAllPhasesForAdmin() {
        return phaseRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toPhaseDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đầy đủ nội dung bài học của một giai đoạn theo Khóa / Slug (ví
     * dụ: "plan", "code").
     * Frontend gọi API này khi học viên mở trang học (`/devops/plan`,
     * `/devops/code`...).
     */
    @Transactional(readOnly = true)
    public Optional<DevopsDTOs.PhaseDetailResponse> getPhaseDetailByKey(String phaseKey) {
        return phaseRepository.findByPhaseKeyAndActiveTrue(phaseKey)
                .map(this::toPhaseDetailResponse);
    }

    /**
     * Tạo mới một Giai đoạn DevOps kèm toàn bộ nội dung bài học.
     */
    public DevopsDTOs.PhaseDetailResponse createPhase(DevopsDTOs.PhaseRequest request) {
        if (phaseRepository.existsByPhaseKey(request.phaseKey())) {
            throw new IllegalArgumentException(
                    "Phase với key '" + request.phaseKey() + "' đã tồn tại. Vui lòng chọn key khác.");
        }

        DevopsPhase phase = new DevopsPhase();
        mapRequestToPhase(request, phase);
        phase.setCreatedAt(Instant.now());
        phase.setCreatedBy(getCurrentUsername());

        DevopsPhase savedPhase = phaseRepository.save(phase);
        return toPhaseDetailResponse(savedPhase);
    }

    /**
     * Cập nhật thông tin và nội dung chi tiết (curriculum, tools, quiz, labs) của
     * một Giai đoạn.
     */
    public DevopsDTOs.PhaseDetailResponse updatePhase(Long id, DevopsDTOs.PhaseRequest request) {
        DevopsPhase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phase với ID: " + id));

        if (!phase.getPhaseKey().equals(request.phaseKey()) && phaseRepository.existsByPhaseKey(request.phaseKey())) {
            throw new IllegalArgumentException("Phase với key '" + request.phaseKey() + "' đã tồn tại.");
        }

        mapRequestToPhase(request, phase);
        phase.setUpdatedAt(Instant.now());
        phase.setUpdatedBy(getCurrentUsername());

        DevopsPhase updatedPhase = phaseRepository.save(phase);
        return toPhaseDetailResponse(updatedPhase);
    }

    /**
     * Xóa mềm (Soft Delete) một Giai đoạn - ẩn khỏi giao diện học viên.
     */
    public void deletePhase(Long id) {
        DevopsPhase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Phase với ID: " + id));

        phase.setActive(false);
        phase.setUpdatedAt(Instant.now());
        phase.setUpdatedBy(getCurrentUsername());
        phaseRepository.save(phase);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }

    private void mapRequestToPhase(DevopsDTOs.PhaseRequest request, DevopsPhase phase) {
        phase.setPhaseKey(request.phaseKey());
        phase.setTitle(request.title());
        phase.setName(request.name() != null ? request.name() : request.title());
        phase.setTagline(request.tagline());
        phase.setSummary(request.summary());
        phase.setHeroSnippetTitle(request.heroSnippetTitle());
        phase.setHeroSnippet(request.heroSnippet());
        phase.setIconName(request.iconName() != null ? request.iconName() : "code-2");
        phase.setColorGradient(request.colorGradient() != null ? request.colorGradient() : "from-blue-500 to-cyan-500");
        phase.setDisplayOrder(request.displayOrder());
        phase.setActive(request.active());

        // Chuyển các Object/List từ DTO sang chuỗi JSON để lưu vào DB
        phase.setThemeJson(toJsonString(request.theme(), "theme"));
        phase.setCurriculumJson(toJsonString(request.curriculum(), "curriculum"));
        phase.setToolsJson(toJsonString(request.tools(), "tools"));
        phase.setLearningPathJson(toJsonString(request.learningPath(), "learningPath"));
        phase.setQuizJson(toJsonString(request.quiz(), "quiz"));
        phase.setHandsOnLabsJson(toJsonString(request.handsOnLabs(), "handsOnLabs"));
    }

    private DevopsDTOs.PhaseResponse toPhaseResponse(DevopsPhase phase) {
        return new DevopsDTOs.PhaseResponse(
                phase.getId(), phase.getPhaseKey(), phase.getTitle(), phase.getName(),
                phase.getTagline(), phase.getIconName(), phase.getColorGradient(),
                phase.getDisplayOrder(), phase.isActive());
    }

    private DevopsDTOs.PhaseDetailResponse toPhaseDetailResponse(DevopsPhase phase) {
        return new DevopsDTOs.PhaseDetailResponse(
                phase.getId(), phase.getPhaseKey(), phase.getTitle(), phase.getName(),
                phase.getTagline(), phase.getSummary(), phase.getHeroSnippetTitle(), phase.getHeroSnippet(),
                phase.getIconName(), phase.getColorGradient(), phase.getDisplayOrder(), phase.isActive(),
                fromJsonString(phase.getThemeJson(), "theme"),
                fromJsonString(phase.getCurriculumJson(), "curriculum"),
                fromJsonString(phase.getToolsJson(), "tools"),
                fromJsonString(phase.getLearningPathJson(), "learningPath"),
                fromJsonString(phase.getQuizJson(), "quiz"),
                fromJsonString(phase.getHandsOnLabsJson(), "handsOnLabs"));
    }

    private String toJsonString(Object obj, String fieldName) {
        if (obj == null)
            return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new com.web_tutorial.javabackend.exception.DevopsContentSerializationException("SERIALIZE", fieldName, e);
        }
    }

    private Object fromJsonString(String json, String fieldName) {
        if (json == null || json.isBlank())
            return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new com.web_tutorial.javabackend.exception.DevopsContentSerializationException("DESERIALIZE", fieldName, e);
        }
    }
}
