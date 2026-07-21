import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { createTestQueryClient } from '../../../test/createTestQueryClient';
import { learningProgressApi } from '../api/learningProgressApi';
import { learningProgressKeys } from './learningProgressKeys';
import {
  useTouchLearningProgress,
  useUpdateLearningProgress,
  useCompleteLearningProgress,
  useResetLearningProgress,
} from './useLearningProgressMutations';
import type { LearningProgress } from '../types/learningProgress.types';

vi.mock('../api/learningProgressApi', () => ({
  learningProgressApi: {
    touchProgress: vi.fn(),
    updateProgress: vi.fn(),
    completeProgress: vi.fn(),
    resetProgress: vi.fn(),
  },
}));

const createProgress = (overrides: Partial<LearningProgress> = {}): LearningProgress => ({
  contentType: 'TUTORIAL',
  contentKey: 'java-basic',
  status: 'IN_PROGRESS',
  progressPercent: 40,
  lastAccessedAt: '2023-01-01T00:00:00Z',
  completedAt: null,
  ...overrides,
});

describe('useLearningProgressMutations', () => {
  let queryClient: ReturnType<typeof createTestQueryClient>;
  let wrapper: React.FC<{ children: React.ReactNode }>;

  beforeEach(() => {
    vi.clearAllMocks();
    queryClient = createTestQueryClient();
    queryClient.setDefaultOptions({
      queries: {
        ...queryClient.getDefaultOptions().queries,
        gcTime: Infinity,
      },
    });
    wrapper = ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  });

  describe('useTouchLearningProgress', () => {
    it('should call touch API and update cache', async () => {
      const mockResponse = createProgress({ progressPercent: 10 });
      vi.mocked(learningProgressApi.touchProgress).mockResolvedValueOnce(mockResponse);

      const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined);

      const { result } = renderHook(() => useTouchLearningProgress(), { wrapper });

      result.current.mutate({ contentType: 'TUTORIAL', contentKey: 'java-basic' });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(learningProgressApi.touchProgress).toHaveBeenCalledWith('TUTORIAL', 'java-basic');
        const detailCache = queryClient.getQueryData(
          learningProgressKeys.detail('TUTORIAL', 'java-basic')
        );
        expect(detailCache).toEqual(mockResponse);
      });

      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.summary() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.continue() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.lists() });
    });
  });

  describe('useUpdateLearningProgress', () => {
    it('should call update API, update detail cache, and invalidate lists', async () => {
      const mockResponse = createProgress({ progressPercent: 50 });
      vi.mocked(learningProgressApi.updateProgress).mockResolvedValueOnce(mockResponse);

      const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined);

      const { result } = renderHook(() => useUpdateLearningProgress(), { wrapper });

      result.current.mutate({
        contentType: 'TUTORIAL',
        contentKey: 'java-basic',
        request: { progressPercent: 50 },
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(learningProgressApi.updateProgress).toHaveBeenCalledWith('TUTORIAL', 'java-basic', {
          progressPercent: 50,
        });
        const detailCache = queryClient.getQueryData(
          learningProgressKeys.detail('TUTORIAL', 'java-basic')
        );
        expect(detailCache).toEqual(mockResponse);
      });

      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.summary() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.continue() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.lists() });
    });

    it('should propagate errors and not call setQueryData or invalidate on failure', async () => {
      const error = new Error('Network error');
      vi.mocked(learningProgressApi.updateProgress).mockRejectedValueOnce(error);
      const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
      const setQueryDataSpy = vi.spyOn(queryClient, 'setQueryData');

      const { result } = renderHook(() => useUpdateLearningProgress(), { wrapper });

      result.current.mutate({
        contentType: 'TUTORIAL',
        contentKey: 'java-basic',
        request: { progressPercent: 50 },
      });

      await waitFor(() => expect(result.current.isError).toBe(true));

      expect(result.current.error).toBe(error);
      expect(setQueryDataSpy).not.toHaveBeenCalled();
      expect(invalidateSpy).not.toHaveBeenCalled();
    });
  });

  describe('useCompleteLearningProgress', () => {
    it('should call complete API and update cache to COMPLETED', async () => {
      const mockResponse = createProgress({ status: 'COMPLETED', progressPercent: 100 });
      vi.mocked(learningProgressApi.completeProgress).mockResolvedValueOnce(mockResponse);

      const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined);

      const { result } = renderHook(() => useCompleteLearningProgress(), { wrapper });

      result.current.mutate({ contentType: 'TUTORIAL', contentKey: 'java-basic' });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(learningProgressApi.completeProgress).toHaveBeenCalledWith('TUTORIAL', 'java-basic');
        const detailCache = queryClient.getQueryData(
          learningProgressKeys.detail('TUTORIAL', 'java-basic')
        );
        expect(detailCache).toEqual(mockResponse);
      });

      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.summary() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.continue() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.lists() });
    });
  });

  describe('useResetLearningProgress', () => {
    it('should call reset API and set detail cache to NOT_STARTED fallback', async () => {
      vi.mocked(learningProgressApi.resetProgress).mockResolvedValueOnce();

      const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined);

      const { result } = renderHook(() => useResetLearningProgress(), { wrapper });

      result.current.mutate({ contentType: 'TUTORIAL', contentKey: 'java-basic' });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(learningProgressApi.resetProgress).toHaveBeenCalledWith('TUTORIAL', 'java-basic');
        const detailCache = queryClient.getQueryData(
          learningProgressKeys.detail('TUTORIAL', 'java-basic')
        );
        expect(detailCache).toEqual({
          contentType: 'TUTORIAL',
          contentKey: 'java-basic',
          status: 'NOT_STARTED',
          progressPercent: 0,
          lastAccessedAt: null,
          completedAt: null,
        });
      });

      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.summary() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.continue() });
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: learningProgressKeys.lists() });
    });
  });

  describe('Await invalidation behavior', () => {
    it('should keep mutation as pending until invalidateQueries resolves', async () => {
      vi.mocked(learningProgressApi.touchProgress).mockResolvedValueOnce(createProgress());
      
      let resolveInvalidation: (value: unknown) => void;
      const invalidatePromise = new Promise((resolve) => {
        resolveInvalidation = resolve;
      });

      vi.spyOn(queryClient, 'invalidateQueries').mockReturnValue(invalidatePromise as unknown as Promise<void>);

      const { result } = renderHook(() => useTouchLearningProgress(), { wrapper });
      result.current.mutate({ contentType: 'TUTORIAL', contentKey: 'java-basic' });

      // First, the API call is made and resolves, but the invalidation is pending
      await waitFor(() => {
        expect(learningProgressApi.touchProgress).toHaveBeenCalled();
      });

      // Still pending because invalidateQueries is stuck
      expect(result.current.isPending).toBe(true);

      // Resolve the invalidations
      resolveInvalidation!(undefined);

      // Now it should be successful
      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.isPending).toBe(false);
    });
  });
});
