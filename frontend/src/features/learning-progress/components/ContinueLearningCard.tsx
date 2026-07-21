
import { Target, BookOpen, FolderKanban, Map, GitBranch, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Card, Button, LoadingSpinner, ErrorState, EmptyState } from '../../../components/ui';
import { useContinueLearning } from '../hooks/useLearningProgress';
import { 
  getLearningContentTypeLabel, 
  getLearningContentFallbackTitle, 
  buildLearningContentRoute,
  formatLearningLastAccessedAt
} from '../utils/learningProgressUi';
import type { LearningContentType } from '../types/learningProgress.types';

export function ContinueLearningCard() {
  const { data: continueData, isLoading, error, refetch } = useContinueLearning();

  if (isLoading) {
    return (
      <Card className="p-6 h-full flex flex-col justify-center items-center min-h-[200px]">
        <LoadingSpinner size="md" />
        <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">Đang kiểm tra nội dung...</p>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-6 h-full min-h-[200px]">
        <ErrorState 
          title="Lỗi tải dữ liệu"
          message="Không thể tải nội dung học tập tiếp theo."
          onRetry={() => refetch()} 
        />
      </Card>
    );
  }

  // If backend returns empty body or empty object when no progress exists
  if (!continueData || !continueData.contentKey) {
    return (
      <Card className="p-6 h-full min-h-[200px]">
        <EmptyState 
          title="Không có nội dung đang học"
          description="Bạn chưa có nội dung đang học. Hãy bắt đầu một bài hướng dẫn hoặc lộ trình mới."
          icon={<Target className="w-10 h-10 text-gray-400 dark:text-gray-500" />}
        />
      </Card>
    );
  }

  const { contentType, contentKey, progressPercent, lastAccessedAt, title, route, thumbnail } = continueData;
  const displayTitle = title || getLearningContentFallbackTitle(contentKey);
  const typeLabel = getLearningContentTypeLabel(contentType);
  const targetRoute = buildLearningContentRoute(contentType, contentKey, route);
  const formattedDate = formatLearningLastAccessedAt(lastAccessedAt);
  const clampedProgress = Math.min(100, Math.max(0, Math.round(progressPercent)));

  const getIconForType = (type: LearningContentType) => {
    switch (type) {
      case 'TUTORIAL': return <BookOpen className="w-8 h-8 text-primary-500" aria-hidden="true" />;
      case 'PROJECT': return <FolderKanban className="w-8 h-8 text-secondary-500" aria-hidden="true" />;
      case 'ROADMAP': return <Map className="w-8 h-8 text-success-500" aria-hidden="true" />;
      case 'DEVOPS_PHASE': return <GitBranch className="w-8 h-8 text-orange-500" aria-hidden="true" />;
      default: return <Target className="w-8 h-8 text-gray-500" aria-hidden="true" />;
    }
  };

  return (
    <Card className="p-6 h-full flex flex-col">
      <div className="flex items-center gap-2 mb-6">
        <Target className="w-5 h-5 text-primary-600" />
        <h2 className="font-semibold text-gray-900 dark:text-gray-100">Tiếp tục học</h2>
      </div>

      <div className="flex flex-col flex-1">
        <div className="flex items-start gap-4 mb-4">
          <div className="w-16 h-16 rounded-xl overflow-hidden shrink-0 bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
            {thumbnail ? (
              <img src={thumbnail} alt={displayTitle} className="w-full h-full object-cover" />
            ) : (
              getIconForType(contentType)
            )}
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex justify-between items-start gap-2 mb-1">
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300">
                {typeLabel}
              </span>
            </div>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100 line-clamp-2 leading-tight" title={displayTitle}>
              {displayTitle}
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
              Truy cập lần cuối: {formattedDate}
            </p>
          </div>
        </div>

        <div className="mt-auto">
          <div className="flex items-center justify-between text-sm mb-2">
            <span className="text-gray-600 dark:text-gray-400 font-medium">Tiến độ</span>
            <span className="font-bold text-primary-600 dark:text-primary-400">{clampedProgress}%</span>
          </div>
          
          <div className="w-full h-2.5 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden" role="progressbar" aria-valuenow={clampedProgress} aria-valuemin={0} aria-valuemax={100}>
            <div
              className="h-full rounded-full bg-gradient-to-r from-primary-500 to-secondary-500 transition-all duration-500"
              style={{ width: `${clampedProgress}%` }}
            />
          </div>

          <div className="mt-6">
            {targetRoute ? (
              <Link to={targetRoute} className="block w-full">
                <Button className="w-full group">
                  Tiếp tục học
                  <ChevronRight className="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
                </Button>
              </Link>
            ) : (
              <div className="text-center p-3 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm text-gray-500 dark:text-gray-400 border border-gray-100 dark:border-gray-700">
                Đường dẫn nội dung chưa khả dụng.
              </div>
            )}
          </div>
        </div>
      </div>
    </Card>
  );
}
