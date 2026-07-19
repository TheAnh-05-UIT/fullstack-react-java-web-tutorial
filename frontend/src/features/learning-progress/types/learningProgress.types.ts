export type LearningContentType = 'TUTORIAL' | 'PROJECT' | 'ROADMAP' | 'DEVOPS_PHASE';

export type LearningProgressStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export interface LearningProgress {
  contentType: LearningContentType;
  contentKey: string;
  status: LearningProgressStatus;
  progressPercent: number;
  lastAccessedAt: string | null;
  completedAt: string | null;
}

export interface LearningProgressSummary {
  totalTracked: number;
  inProgress: number;
  completed: number;
  completionRate: number;
}

export interface ContinueLearning {
  contentType: LearningContentType;
  contentKey: string;
  progressPercent: number;
  lastAccessedAt: string | null;
  title: string | null;
  route: string | null;
  thumbnail: string | null;
}

export interface UpdateLearningProgressRequest {
  progressPercent: number;
}
