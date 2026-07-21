import { vi } from 'vitest';
import type { LearningProgress, LearningProgressListItem } from '../types/learningProgress.types';

export const createProgress = (
  overrides: Partial<LearningProgress> = {}
): LearningProgress => ({
  contentType: 'TUTORIAL',
  contentKey: 'java-basic',
  status: 'IN_PROGRESS',
  progressPercent: 40,
  lastAccessedAt: '2026-07-20T10:00:00Z',
  completedAt: null,
  ...overrides,
});

export const createListItem = (
  overrides: Partial<LearningProgressListItem> = {}
): LearningProgressListItem => ({
  contentType: 'TUTORIAL',
  contentKey: 'java-basic',
  title: 'Java Basic',
  route: '/tutorials/java-basic',
  thumbnail: null,
  contentAvailable: true,
  status: 'IN_PROGRESS',
  progressPercent: 40,
  lastAccessedAt: '2026-07-20T10:00:00Z',
  completedAt: null,
  ...overrides,
});

export const createMutationMock = (
  overrides = {}
) => ({
  mutate: vi.fn(),
  mutateAsync: vi.fn(),
  isPending: false,
  isError: false,
  isSuccess: false,
  reset: vi.fn(),
  data: undefined,
  error: null,
  ...overrides,
});
