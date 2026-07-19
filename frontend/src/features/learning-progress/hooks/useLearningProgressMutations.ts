import { useMutation, useQueryClient } from '@tanstack/react-query';
import { learningProgressApi } from '../api/learningProgressApi';
import { learningProgressKeys } from './learningProgressKeys';
import type { LearningContentType, UpdateLearningProgressRequest, LearningProgress } from '../types/learningProgress.types';

export const useTouchLearningProgress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ contentType, contentKey }: { contentType: LearningContentType; contentKey: string }) =>
      learningProgressApi.touchProgress(contentType, contentKey),
    onSuccess: (data, { contentType, contentKey }) => {
      queryClient.setQueryData(learningProgressKeys.detail(contentType, contentKey), data);
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.summary() });
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.continue() });
    },
  });
};

export const useUpdateLearningProgress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      contentType,
      contentKey,
      request,
    }: {
      contentType: LearningContentType;
      contentKey: string;
      request: UpdateLearningProgressRequest;
    }) => learningProgressApi.updateProgress(contentType, contentKey, request),
    onSuccess: (data, { contentType, contentKey }) => {
      queryClient.setQueryData(learningProgressKeys.detail(contentType, contentKey), data);
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.summary() });
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.continue() });
    },
  });
};

export const useCompleteLearningProgress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ contentType, contentKey }: { contentType: LearningContentType; contentKey: string }) =>
      learningProgressApi.completeProgress(contentType, contentKey),
    onSuccess: (data, { contentType, contentKey }) => {
      queryClient.setQueryData(learningProgressKeys.detail(contentType, contentKey), data);
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.summary() });
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.continue() });
    },
  });
};

export const useResetLearningProgress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ contentType, contentKey }: { contentType: LearningContentType; contentKey: string }) =>
      learningProgressApi.resetProgress(contentType, contentKey),
    onSuccess: (_, { contentType, contentKey }) => {
      const resetData: LearningProgress = {
        contentType,
        contentKey,
        status: 'NOT_STARTED',
        progressPercent: 0,
        lastAccessedAt: null,
        completedAt: null,
      };
      queryClient.setQueryData(learningProgressKeys.detail(contentType, contentKey), resetData);
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.summary() });
      queryClient.invalidateQueries({ queryKey: learningProgressKeys.continue() });
    },
  });
};
