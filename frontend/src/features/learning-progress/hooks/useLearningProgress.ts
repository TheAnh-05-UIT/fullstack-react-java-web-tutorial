import { useQuery } from '@tanstack/react-query';
import { learningProgressApi } from '../api/learningProgressApi';
import { learningProgressKeys } from './learningProgressKeys';
import { useAuth } from '../../../context/AuthContext';
import type { LearningContentType, LearningProgressListFilters } from '../types/learningProgress.types';

export const useLearningProgressSummary = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: learningProgressKeys.summary(),
    queryFn: () => learningProgressApi.getSummary(),
    enabled: isAuthenticated === true,
    staleTime: 60000,
  });
};

export const useContinueLearning = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: learningProgressKeys.continue(),
    queryFn: () => learningProgressApi.getContinueLearning(),
    enabled: isAuthenticated === true,
    staleTime: 60000,
  });
};

export const useLearningProgress = (contentType: LearningContentType, contentKey: string) => {
  const { isAuthenticated } = useAuth();
  const trimmedKey = contentKey?.trim() ?? '';

  return useQuery({
    queryKey: learningProgressKeys.detail(contentType, trimmedKey),
    queryFn: () => learningProgressApi.getProgress(contentType, trimmedKey),
    enabled: isAuthenticated === true && !!contentType && trimmedKey.length > 0,
    staleTime: 60000,
  });
};

export const useLearningProgressList = (filters: LearningProgressListFilters) => {
  const { isInitialized, isAuthenticated } = useAuth();

  return useQuery({
    queryKey: learningProgressKeys.list(filters),
    queryFn: () => learningProgressApi.getMyProgressPage(filters),
    enabled: isInitialized === true && isAuthenticated === true,
    staleTime: 60000,
    placeholderData: (previousData) => previousData,
  });
};
