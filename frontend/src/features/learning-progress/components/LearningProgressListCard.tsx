import { useNavigate } from 'react-router-dom';
import { Play, CheckCircle, RotateCcw, ImageOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { Card, Badge, Button } from '../../../components/ui';
import type { LearningProgressListItem } from '../types/learningProgress.types';
import { useCompleteLearningProgress, useResetLearningProgress } from '../hooks/useLearningProgressMutations';
import {
  getLearningContentTypeLabel,
  getLearningContentTypeIcon,
  getLearningProgressStatusLabel,
  getLearningProgressStatusClassName,
  formatLearningLastAccessedAt,
  getLearningContentFallbackTitle,
} from '../utils/learningProgressUi';

interface LearningProgressListCardProps {
  item: LearningProgressListItem;
}

export function LearningProgressListCard({ item }: LearningProgressListCardProps) {
  const navigate = useNavigate();
  const completeMutation = useCompleteLearningProgress();
  const resetMutation = useResetLearningProgress();

  const isPending = completeMutation.isPending || resetMutation.isPending;
  const isCompleted = item.status === 'COMPLETED';
  const percent = Math.min(100, Math.max(0, Number.isFinite(item.progressPercent) ? item.progressPercent : 0));
  const normalizedRoute = item.route?.trim() ?? '';
  const canContinue = item.contentAvailable && normalizedRoute.startsWith('/') && !normalizedRoute.startsWith('//');
  const canComplete = item.status === 'IN_PROGRESS' && percent < 100 && item.contentAvailable;
  
  const displayTitle = item.contentAvailable ? item.title : getLearningContentFallbackTitle(item.title);
  const TypeIcon = getLearningContentTypeIcon(item.contentType);

  const handleComplete = () => {
    if (isPending || !canComplete) return;
    
    completeMutation.mutate(
      { contentType: item.contentType, contentKey: item.contentKey },
      {
        onSuccess: () => {
          toast.success('Đã đánh dấu hoàn thành.');
        },
        onError: () => {
          toast.error('Không thể hoàn thành nội dung. Vui lòng thử lại.');
        }
      }
    );
  };

  const handleReset = () => {
    if (isPending) return;

    const confirmed = window.confirm('Bạn có chắc muốn đặt lại tiến độ của nội dung này?');
    if (!confirmed) return;

    resetMutation.mutate(
      { contentType: item.contentType, contentKey: item.contentKey },
      {
        onSuccess: () => {
          toast.success('Đã đặt lại tiến độ học tập.');
        },
        onError: () => {
          toast.error('Không thể đặt lại tiến độ. Vui lòng thử lại.');
        }
      }
    );
  };

  return (
    <Card className="flex flex-col h-full overflow-hidden">
      {/* Thumbnail Area */}
      <div className="relative h-40 bg-gray-100 dark:bg-gray-800 shrink-0">
        {item.thumbnail ? (
          <img
            src={item.thumbnail}
            alt={displayTitle}
            className="w-full h-full object-cover"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none';
              const fallbackEl = (e.target as HTMLImageElement).nextElementSibling;
              if (fallbackEl) {
                (fallbackEl as HTMLElement).style.display = 'flex';
              }
            }}
          />
        ) : null}
        <div 
          className="absolute inset-0 flex items-center justify-center bg-gray-100 dark:bg-gray-800" 
          aria-hidden="true" 
          style={{ display: item.thumbnail ? 'none' : 'flex' }}
        >
          <TypeIcon className="w-12 h-12 text-gray-400" />
        </div>

        {!item.contentAvailable && (
          <div className="absolute inset-0 bg-gray-900/60 flex items-center justify-center">
            <Badge variant="error" className="bg-red-500/90 text-white border-0 px-3 py-1.5 flex items-center gap-2">
              <ImageOff className="w-4 h-4" />
              Nội dung không khả dụng
            </Badge>
          </div>
        )}
      </div>

      {/* Content Area */}
      <div className="p-5 flex flex-col flex-1">
        <div className="flex items-start justify-between gap-2 mb-3">
          <Badge variant="secondary" className="flex items-center gap-1.5 text-xs">
            <TypeIcon className="w-3 h-3" />
            {getLearningContentTypeLabel(item.contentType)}
          </Badge>
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getLearningProgressStatusClassName(item.status)}`}>
            {getLearningProgressStatusLabel(item.status)}
          </span>
        </div>

        <h3 className="font-semibold text-gray-900 dark:text-gray-100 line-clamp-2 mb-4 flex-1" title={displayTitle}>
          {displayTitle}
        </h3>

        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-sm mb-1.5">
              <span className="text-gray-500 dark:text-gray-400 font-medium">Tiến độ</span>
              <span className="font-semibold text-gray-900 dark:text-gray-100">{Math.round(percent)}%</span>
            </div>
            <div
              className="h-2.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden"
              role="progressbar"
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={percent}
            >
              <div
                className={`h-full rounded-full transition-all duration-500 ${isCompleted ? 'bg-success-500' : 'bg-primary-500'}`}
                style={{ width: `${percent}%` }}
              />
            </div>
          </div>

          <div className="flex flex-col gap-1 text-xs text-gray-500 dark:text-gray-400">
            <span>Truy cập: {formatLearningLastAccessedAt(item.lastAccessedAt)}</span>
            {isCompleted && item.completedAt && (
              <span>Hoàn thành: {formatLearningLastAccessedAt(item.completedAt)}</span>
            )}
          </div>

          {/* Actions */}
          <div className="flex flex-col gap-2 pt-2">
            {canContinue && (
              <Button
                onClick={() => navigate(normalizedRoute)}
                variant="primary"
                className="w-full"
                disabled={isPending}
              >
                <Play className="w-4 h-4 mr-2" />
                Tiếp tục học
              </Button>
            )}
            
            <div className="flex gap-2">
              {canComplete && (
                <Button
                  onClick={handleComplete}
                  variant="secondary"
                  className="flex-1 bg-green-50 text-green-700 border-green-200 hover:bg-green-100 hover:text-green-800 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800 dark:hover:bg-green-900/40"
                  disabled={isPending}
                >
                  <CheckCircle className="w-4 h-4 mr-1.5" />
                  {completeMutation.isPending ? 'Đang...' : 'Hoàn thành'}
                </Button>
              )}
              
              <Button
                onClick={handleReset}
                variant="ghost"
                className="flex-1 text-red-600 hover:bg-red-50 hover:text-red-700 dark:text-red-400 dark:hover:bg-red-900/20"
                disabled={isPending}
              >
                <RotateCcw className="w-4 h-4 mr-1.5" />
                {resetMutation.isPending ? 'Đang...' : 'Đặt lại'}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
}
