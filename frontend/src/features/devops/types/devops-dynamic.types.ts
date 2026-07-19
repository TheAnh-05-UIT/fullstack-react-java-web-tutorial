import type {
  PhaseTheme,
  CurriculumItem,
  ToolItem,
  LearningStep,
  QuizQuestion,
  PracticeLab
} from './devops.types';

// ============================================================================
// TypeScript Interfaces cho dữ liệu nội dung DevOps Lifecycle từ Java Backend
// Ánh xạ chính xác với DevopsDTOs (PhaseResponse, PhaseDetailResponse, PhaseRequest)
// ============================================================================

/**
 * Thông tin tóm tắt một Giai đoạn DevOps (dùng cho danh sách / sidebar)
 */
export interface PhaseResponse {
  id: number;
  phaseKey: string;
  title: string;
  name?: string;
  tagline?: string;
  iconName: string;
  colorGradient: string;
  displayOrder: number;
  active: boolean;
}

/**
 * Thông tin chi tiết toàn bộ nội dung bài học của một Giai đoạn DevOps
 * (Trả về từ GET /api/v1/devops/phases/{phaseKey} hoặc GET /api/v1/devops/admin/phases)
 */
export interface PhaseDetailResponse extends PhaseResponse {
  summary?: string;
  heroSnippetTitle?: string;
  heroSnippet?: string;
  theme?: PhaseTheme;
  curriculum?: CurriculumItem[];
  tools?: ToolItem[];
  learningPath?: LearningStep[];
  quiz?: QuizQuestion[];
  handsOnLabs?: PracticeLab[];
}

/**
 * Payload gửi từ Admin sang Backend (POST / PUT) để Thêm mới / Cập nhật Giai đoạn
 */
export interface PhaseRequest {
  phaseKey: string;
  title: string;
  name?: string;
  tagline?: string;
  summary?: string;
  heroSnippetTitle?: string;
  heroSnippet?: string;
  iconName?: string;
  colorGradient?: string;
  displayOrder: number;
  active: boolean;
  theme?: PhaseTheme;
  curriculum?: CurriculumItem[];
  tools?: ToolItem[];
  learningPath?: LearningStep[];
  quiz?: QuizQuestion[];
  handsOnLabs?: PracticeLab[];
}
