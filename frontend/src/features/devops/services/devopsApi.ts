import { api } from '../../../services/api';
import type {
  PhaseResponse,
  PhaseDetailResponse,
  PhaseRequest
} from '../types/devops-dynamic.types';

// ============================================================================
// DEVOPS API CLIENT (Kết nối giữa React Frontend và Java Spring Boot Backend)
// ============================================================================
// Lưu ý bảo mật & Phân quyền:
// - Các API gọi GET: Công khai (Permit All) -> Học viên bình thường gọi để học.
// - Các API gọi POST / PUT / DELETE: Bảo mật -> CHỈ CÓ ADMIN (ROLE_ADMIN) mới
//   được phép thực thi. Thẻ JWT Access Token sẽ được tự động đính kèm vào Header
//   Authorization thông qua axios interceptor trong file `src/services/api.ts`.
// ============================================================================

export const devopsApi = {
  // --------------------------------------------------------------------------
  // PUBLIC ENDPOINTS (Dành cho Học viên & Giao diện học tập)
  // --------------------------------------------------------------------------

  /**
   * Lấy danh sách tóm tắt 8 Giai đoạn DevOps đang hoạt động (active = true).
   * URL: GET /api/v1/devops/phases
   */
  getActivePhases: async (): Promise<PhaseResponse[]> => {
    return await api.get<any, PhaseResponse[]>('/devops/phases');
  },

  /**
   * Lấy chi tiết toàn bộ nội dung (Curriculum, Tools, Learning Path, Quiz, Labs)
   * theo khóa / slug của giai đoạn (Ví dụ: "plan", "code", "build").
   * URL: GET /api/v1/devops/phases/{phaseKey}
   */
  getPhaseDetailByKey: async (phaseKey: string): Promise<PhaseDetailResponse> => {
    return await api.get<any, PhaseDetailResponse>(`/devops/phases/${phaseKey}`);
  },

  // --------------------------------------------------------------------------
  // ADMIN CRUD ENDPOINTS (CHỈ DÀNH CHO ROLE_ADMIN - Giảng viên / Quản trị viên)
  // --------------------------------------------------------------------------

  /**
   * [ADMIN ONLY] Lấy toàn bộ danh sách Giai đoạn (cả ẩn lẫn hiện) kèm nội dung chi tiết.
   * URL: GET /api/v1/devops/admin/phases
   */
  getAllPhasesForAdmin: async (): Promise<PhaseDetailResponse[]> => {
    return await api.get<any, PhaseDetailResponse[]>('/devops/admin/phases');
  },

  /**
   * [ADMIN ONLY] Thêm mới một Giai đoạn DevOps kèm cấu trúc bài học vào quy trình.
   * URL: POST /api/v1/devops/phases
   */
  createPhase: async (data: PhaseRequest): Promise<PhaseDetailResponse> => {
    return await api.post<any, PhaseDetailResponse>('/devops/phases', data);
  },

  /**
   * [ADMIN ONLY] Cập nhật thông tin & nội dung chi tiết của Giai đoạn (Curriculum, Tools, Quiz...).
   * URL: PUT /api/v1/devops/phases/{id}
   */
  updatePhase: async (id: number, data: PhaseRequest): Promise<PhaseDetailResponse> => {
    return await api.put<any, PhaseDetailResponse>(`/devops/phases/${id}`, data);
  },

  /**
   * [ADMIN ONLY] Xóa mềm (Soft Delete / Ẩn) một Giai đoạn khỏi giao diện học viên.
   * URL: DELETE /api/v1/devops/phases/{id}
   */
  deletePhase: async (id: number): Promise<void> => {
    return await api.delete<any, void>(`/devops/phases/${id}`);
  }
};
