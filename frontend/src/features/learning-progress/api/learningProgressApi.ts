import { api } from '../../../services/api';
import type {
  LearningContentType,
  LearningProgress,
  LearningProgressSummary,
  ContinueLearning,
  UpdateLearningProgressRequest,
} from '../types/learningProgress.types';

const BASE_PATH = '/learning-progress/me';

const buildContentPath = (contentType: LearningContentType, contentKey: string): string => {
  return `${BASE_PATH}/${contentType}/${encodeURIComponent(contentKey)}`;
};

export const learningProgressApi = {
  getSummary: (): Promise<LearningProgressSummary> => {
    return api.get<unknown, LearningProgressSummary>(`${BASE_PATH}/summary`);
  },

  getContinueLearning: (): Promise<ContinueLearning | null> => {
    return api.get<unknown, ContinueLearning | null>(`${BASE_PATH}/continue`);
  },

  getProgress: (contentType: LearningContentType, contentKey: string): Promise<LearningProgress> => {
    return api.get<unknown, LearningProgress>(buildContentPath(contentType, contentKey));
  },

  touchProgress: (contentType: LearningContentType, contentKey: string): Promise<LearningProgress> => {
    return api.post<unknown, LearningProgress>(`${buildContentPath(contentType, contentKey)}/touch`);
  },

  updateProgress: (
    contentType: LearningContentType,
    contentKey: string,
    request: UpdateLearningProgressRequest
  ): Promise<LearningProgress> => {
    return api.put<unknown, LearningProgress>(buildContentPath(contentType, contentKey), request);
  },

  completeProgress: (contentType: LearningContentType, contentKey: string): Promise<LearningProgress> => {
    return api.post<unknown, LearningProgress>(`${buildContentPath(contentType, contentKey)}/complete`);
  },

  resetProgress: async (contentType: LearningContentType, contentKey: string): Promise<void> => {
    await api.delete<unknown, unknown>(buildContentPath(contentType, contentKey));
    return; // Đảm bảo không return data để tránh lỗi với HTTP 204
  },
};
