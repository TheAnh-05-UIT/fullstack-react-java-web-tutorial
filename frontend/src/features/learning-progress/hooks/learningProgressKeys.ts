import type { LearningContentType } from '../types/learningProgress.types';

export const learningProgressKeys = {
  all: ['learning-progress'] as const,
  summary: () => [...learningProgressKeys.all, 'summary'] as const,
  continue: () => [...learningProgressKeys.all, 'continue'] as const,
  detail: (contentType: LearningContentType, contentKey: string) =>
    [...learningProgressKeys.all, 'detail', contentType, contentKey] as const,
  lists: () => [...learningProgressKeys.all, 'list'] as const,
  list: (filters: { page: number; size: number; status?: string; contentType?: string }) =>
    [
      ...learningProgressKeys.lists(),
      filters.page,
      filters.size,
      filters.status ?? 'ALL',
      filters.contentType ?? 'ALL',
    ] as const,
};
