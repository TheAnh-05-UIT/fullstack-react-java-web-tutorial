import { useEffect, useRef, useState, useId } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle, RotateCcw, Clock, AlertCircle } from 'lucide-react';
import { useAuth } from '../../../context/AuthContext';
import { useLearningProgress } from '../hooks/useLearningProgress';
import { 
  useTouchLearningProgress,
  useUpdateLearningProgress,
  useCompleteLearningProgress,
  useResetLearningProgress 
} from '../hooks/useLearningProgressMutations';
import type { LearningContentType } from '../types/learningProgress.types';
import { Badge, Button, LoadingSpinner } from '../../../components/ui';
import toast from 'react-hot-toast';

export type LearningProgressControlsProps = {
  contentType: LearningContentType;
  contentKey: string;
  className?: string;
  compact?: boolean;
};

const formatDate = (dateString: string | null) => {
  if (!dateString) return null;
  try {
    return new Intl.DateTimeFormat('vi-VN', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(dateString));
  } catch {
    return null;
  }
};

export function LearningProgressControls({
  contentType,
  contentKey,
  className = '',
  compact = false,
}: LearningProgressControlsProps) {
  const { isAuthenticated, isInitialized } = useAuth();
  const normalizedContentKey = contentKey?.trim() ?? '';

  // 1. All hooks at top level
  const { 
    data: progress, 
    isLoading: isQueryLoading, 
    isError: isQueryError,
    isSuccess: isQuerySuccess,
    refetch 
  } = useLearningProgress(contentType, normalizedContentKey);

  const { mutate: touchProgress, isPending: isTouchPending, isError: isTouchError, reset: resetTouchMutation } = useTouchLearningProgress();
  const { mutate: updateProgress, isPending: isUpdatePending } = useUpdateLearningProgress();
  const { mutate: completeProgress, isPending: isCompletePending } = useCompleteLearningProgress();
  const { mutate: resetProgress, isPending: isResetPending } = useResetLearningProgress();

  // 2. StrictMode touch protection
  const touchedKeyRef = useRef<string | null>(null);
  const touchKey = `${contentType}:${normalizedContentKey}`;

  // 3. Draft State for Editor
  const [draftPercent, setDraftPercent] = useState(0);
  const [isDirty, setIsDirty] = useState(false);
  const sliderId = useId();

  // Sync draft with server data when loaded
  useEffect(() => {
    if (!progress) {
      return;
    }
    if (!isDirty) {
      setDraftPercent(progress.progressPercent);
    }
  }, [touchKey, progress, progress?.progressPercent, isDirty]);

  // Reset touch error and editor state when switching to a different content key
  useEffect(() => {
    resetTouchMutation();
    setIsDirty(false);
  }, [touchKey, resetTouchMutation]);

  // Only auto-touch when authenticated, initialized, query is successful, and not currently pending.
  useEffect(() => {
    if (
      !isInitialized ||
      !isAuthenticated ||
      !normalizedContentKey ||
      !isQuerySuccess ||
      isTouchPending
    ) {
      return;
    }

    if (touchedKeyRef.current === touchKey) {
      return;
    }

    touchedKeyRef.current = touchKey;

    touchProgress({
      contentType,
      contentKey: normalizedContentKey,
    });
  }, [
    isInitialized,
    isAuthenticated,
    normalizedContentKey,
    contentType,
    touchKey,
    isQuerySuccess,
    isTouchPending,
    touchProgress,
  ]);

  // 4. Early return for Initialization and Anonymous
  if (!isInitialized) {
    return (
      <div className={`bg-gray-50 dark:bg-gray-900/50 border border-gray-100 dark:border-gray-800 rounded-xl p-4 flex items-center justify-center min-h-[100px] ${className}`}>
        <LoadingSpinner size="sm" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className={`bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 flex flex-col items-center text-center gap-3 ${className}`}>
        <p className="text-sm text-gray-600 dark:text-gray-400">
          Đăng nhập để lưu tiến độ học tập.
        </p>
        <Link to="/login">
          <Button variant="secondary" size="sm">
            Đăng nhập
          </Button>
        </Link>
      </div>
    );
  }

  // 5. Loading / Error States
  if (!normalizedContentKey) return null;

  if (isQueryLoading) {
    return (
      <div className={`flex items-center justify-center p-4 border border-gray-100 dark:border-gray-800 rounded-xl ${className}`}>
        <LoadingSpinner size="sm" />
      </div>
    );
  }

  if (isQueryError) {
    return (
      <div className={`bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-900/30 rounded-xl p-4 flex items-center justify-between gap-4 ${className}`}>
        <p className="text-sm text-red-600 dark:text-red-400">Không thể tải tiến độ học tập.</p>
        <Button variant="ghost" size="sm" onClick={() => refetch()} className="text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/50">
          Thử lại
        </Button>
      </div>
    );
  }

  // 6. Main UI Rendering
  const status = progress?.status || 'NOT_STARTED';
  const percent = Math.min(100, Math.max(0, progress?.progressPercent || 0));
  const isCompleted = status === 'COMPLETED';
  const isAnyPending = isTouchPending || isUpdatePending || isCompletePending || isResetPending;
  const isDraftValid = Number.isFinite(draftPercent) && draftPercent >= 0 && draftPercent <= 100;

  const handleSliderChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const nextValue = Number(e.target.value);
    if (!Number.isFinite(nextValue) || nextValue < 0 || nextValue > 100) return;
    
    setDraftPercent(nextValue);
    setIsDirty(nextValue !== percent);
  };

  const handleSaveProgress = () => {
    if (!isDirty || !isDraftValid || isAnyPending) return;

    if (draftPercent === percent) {
      setIsDirty(false);
      return;
    }

    updateProgress(
      { contentType, contentKey: normalizedContentKey, request: { progressPercent: draftPercent } },
      {
        onSuccess: (data) => {
          toast.success('Đã cập nhật tiến độ học tập.');
          setIsDirty(false);
          setDraftPercent(data.progressPercent);
        },
        onError: () => {
          toast.error('Không thể cập nhật tiến độ. Vui lòng thử lại.');
        }
      }
    );
  };

  const handleComplete = () => {
    completeProgress(
      { contentType, contentKey: normalizedContentKey },
      {
        onSuccess: (data) => {
          toast.success('Đã đánh dấu nội dung hoàn thành.');
          setIsDirty(false);
          setDraftPercent(data.progressPercent);
        }
      }
    );
  };

  const handleReset = () => {
    if (window.confirm('Bạn có chắc muốn đặt lại tiến độ của nội dung này?')) {
      resetProgress(
        { contentType, contentKey: normalizedContentKey },
        {
          onSuccess: () => {
            toast.success('Đã đặt lại tiến độ.');
            setIsDirty(false);
            setDraftPercent(0);
          }
        }
      );
    }
  };

  const handleRetryTouch = () => {
    touchProgress({ contentType, contentKey: normalizedContentKey });
  };

  return (
    <div className={`bg-white dark:bg-gray-950 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5 shadow-sm ${className}`}>
      
      {/* Header: Status and Percent */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-gray-900 dark:text-gray-100">Tiến độ:</span>
          {status === 'NOT_STARTED' && <Badge variant="secondary">Chưa bắt đầu</Badge>}
          {status === 'IN_PROGRESS' && <Badge variant="primary">Đang học</Badge>}
          {status === 'COMPLETED' && <Badge variant="success">Đã hoàn thành</Badge>}
        </div>
        <span className="text-sm font-bold text-gray-900 dark:text-gray-100">{percent}%</span>
      </div>

      {/* Progress Bar */}
      <div className="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-2.5 mb-4 overflow-hidden" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={percent}>
        <div 
          className={`h-2.5 rounded-full transition-all duration-500 ease-in-out ${isCompleted ? 'bg-success-500' : 'bg-primary-600'}`} 
          style={{ width: `${percent}%` }}
        />
      </div>

      {/* Touch Error Warning */}
      {isTouchError && (
        <div className="mb-4 text-xs text-warning-600 dark:text-warning-400 bg-warning-50 dark:bg-warning-900/20 p-2 rounded flex items-center justify-between">
          <span className="flex items-center gap-1"><AlertCircle className="w-3 h-3" /> Không thể lưu trạng thái đang học.</span>
          <button onClick={handleRetryTouch} disabled={isTouchPending} className="underline hover:no-underline font-medium">
            Thử lưu lại
          </button>
        </div>
      )}

      {/* Meta Dates */}
      {!compact && (
        <div className="flex flex-col gap-1 mb-4 text-xs text-gray-500 dark:text-gray-400">
          {progress?.lastAccessedAt && (
            <div className="flex items-center gap-1">
              <Clock className="w-3 h-3" /> <span>Truy cập gần nhất: {formatDate(progress.lastAccessedAt)}</span>
            </div>
          )}
          {progress?.completedAt && (
            <div className="flex items-center gap-1">
              <CheckCircle className="w-3 h-3 text-success-500" /> <span>Hoàn thành lúc: {formatDate(progress.completedAt)}</span>
            </div>
          )}
        </div>
      )}

      {/* Editor: Cập nhật tiến độ */}
      <div className="flex flex-col gap-2 mb-4 pt-4 border-t border-gray-100 dark:border-gray-800">
        <label htmlFor={sliderId} className="text-sm font-semibold text-gray-900 dark:text-gray-100">
          Cập nhật tiến độ
        </label>
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div className="flex items-center gap-3 w-full sm:flex-1">
            <input 
              id={sliderId}
              type="range" 
              min={0} 
              max={100} 
              step={5} 
              value={draftPercent} 
              onChange={handleSliderChange}
              disabled={isAnyPending}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={draftPercent}
              className="flex-1 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer dark:bg-gray-700 accent-primary-600 dark:accent-primary-500"
            />
            <span className="text-sm font-medium w-12 text-right text-gray-900 dark:text-gray-100">
              {draftPercent}%
            </span>
          </div>
          <Button 
            variant="secondary" 
            size="sm" 
            disabled={!isDirty || !isDraftValid || isAnyPending}
            onClick={handleSaveProgress}
            className="w-full sm:w-auto"
          >
            {isUpdatePending ? 'Đang lưu...' : 'Lưu tiến độ'}
          </Button>
        </div>
        {!isDraftValid && isDirty && (
          <span className="text-xs text-red-600 dark:text-red-400" aria-live="polite">
            Tiến độ phải nằm trong khoảng từ 0 đến 100.
          </span>
        )}
      </div>

      {/* Actions */}
      <div className="flex flex-wrap items-center gap-2 mt-2">
        {!isCompleted && (
          <Button 
            variant="primary" 
            size="sm" 
            className="flex-1 min-w-[140px]"
            onClick={handleComplete} 
            disabled={isAnyPending}
          >
            {isCompletePending ? 'Đang hoàn thành...' : 'Đánh dấu hoàn thành'}
          </Button>
        )}
        
        {(status === 'IN_PROGRESS' || status === 'COMPLETED') && (
          <Button 
            variant="ghost" 
            size="sm" 
            onClick={handleReset} 
            disabled={isAnyPending}
            className={`flex items-center gap-1 ${isCompleted ? 'flex-1' : ''}`}
            title="Đặt lại tiến độ"
          >
            <RotateCcw className="w-4 h-4" />
            {isCompleted && <span>Đặt lại</span>}
          </Button>
        )}
      </div>
    </div>
  );
}
