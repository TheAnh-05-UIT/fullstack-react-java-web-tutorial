package com.web_tutorial.javabackend.repository.devops;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;

/**
 * Repository truy vấn bảng devops_phases.
 */
@Repository
public interface DevopsPhaseRepository extends JpaRepository<DevopsPhase, Long> {

    /**
     * Tìm giai đoạn theo khóa định danh (phaseKey).
     * Ví dụ: findByPhaseKey("code") → trả về giai đoạn Code.
     */
    Optional<DevopsPhase> findByPhaseKey(String phaseKey);
    Optional<DevopsPhase> findByPhaseKeyAndActiveTrue(String phaseKey);

    /**
     * Kiểm tra phaseKey đã tồn tại chưa (dùng khi tạo mới để tránh trùng).
     */
    boolean existsByPhaseKey(String phaseKey);
    boolean existsByPhaseKeyAndActiveTrue(String phaseKey);

    List<DevopsPhase> findByPhaseKeyInAndActiveTrue(java.util.Collection<String> phaseKeys);

    /**
     * Lấy danh sách tất cả giai đoạn đang hoạt động, sắp xếp theo thứ tự hiển thị.
     * Chỉ trả về giai đoạn có active = true (không bao gồm giai đoạn bị ẩn).
     */
    List<DevopsPhase> findByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Lấy tất cả giai đoạn (cả ẩn lẫn hiện) dành cho Admin Dashboard.
     */
    List<DevopsPhase> findAllByOrderByDisplayOrderAsc();

    /**
     * Đếm số giai đoạn đang active (dùng để kiểm tra DB có dữ liệu chưa).
     * Nếu = 0, DevopsDataSeeder sẽ tự động nạp 8 giai đoạn mặc định.
     */
    @Query("SELECT COUNT(p) FROM DevopsPhase p")
    long countAll();
}
