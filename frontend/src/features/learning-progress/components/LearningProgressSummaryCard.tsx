
import { Award, BookOpen, CheckCircle, TrendingUp } from 'lucide-react';
import { Card, LoadingSpinner, ErrorState } from '../../../components/ui';
import { useLearningProgressSummary } from '../hooks/useLearningProgress';

export function LearningProgressSummaryCard() {
  const { data: summary, isLoading, error, refetch } = useLearningProgressSummary();

  if (isLoading) {
    return (
      <Card className="p-6 h-full flex flex-col justify-center items-center min-h-[200px]">
        <LoadingSpinner size="md" />
        <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">Đang tải tiến độ học tập...</p>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-6 h-full min-h-[200px]">
        <ErrorState 
          title="Không thể tải tiến độ"
          message="Đã xảy ra lỗi khi lấy thông tin tiến độ học tập của bạn."
          onRetry={() => refetch()} 
        />
      </Card>
    );
  }

  // Fallback to 0 if data is somehow missing
  const totalTracked = summary?.totalTracked || 0;
  const inProgress = summary?.inProgress || 0;
  const completed = summary?.completed || 0;
  const rawRate = summary?.completionRate || 0;
  const completionRate = Math.min(100, Math.max(0, Math.round(rawRate * 10) / 10));

  return (
    <Card className="p-6 h-full">
      <div className="flex items-center gap-2 mb-6">
        <TrendingUp className="w-5 h-5 text-success-500" />
        <h2 className="font-semibold text-gray-900 dark:text-gray-100">Tiến độ học tập</h2>
      </div>

      {totalTracked === 0 && (
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6 bg-gray-50 dark:bg-gray-800/50 p-3 rounded-lg border border-gray-100 dark:border-gray-800">
          Bạn chưa bắt đầu nội dung học nào. Hãy khám phá các bài hướng dẫn hoặc lộ trình để bắt đầu.
        </p>
      )}

      <div className="grid grid-cols-2 gap-4">
        {/* Total Tracked */}
        <div className="flex flex-col p-4 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-2 mb-2">
            <BookOpen className="w-4 h-4 text-primary-600 dark:text-primary-400" />
            <span className="text-sm font-medium text-gray-600 dark:text-gray-400">Đang theo dõi</span>
          </div>
          <span className="text-2xl font-bold text-gray-900 dark:text-gray-100">{totalTracked}</span>
        </div>

        {/* In Progress */}
        <div className="flex flex-col p-4 rounded-xl bg-orange-50 dark:bg-orange-900/20 border border-orange-100 dark:border-orange-800/30">
          <div className="flex items-center gap-2 mb-2">
            <TrendingUp className="w-4 h-4 text-orange-600 dark:text-orange-400" />
            <span className="text-sm font-medium text-orange-800 dark:text-orange-300">Đang học</span>
          </div>
          <span className="text-2xl font-bold text-orange-700 dark:text-orange-400">{inProgress}</span>
        </div>

        {/* Completed */}
        <div className="flex flex-col p-4 rounded-xl bg-success-50 dark:bg-success-900/20 border border-success-100 dark:border-success-800/30">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="w-4 h-4 text-success-600 dark:text-success-400" />
            <span className="text-sm font-medium text-success-800 dark:text-success-300">Hoàn thành</span>
          </div>
          <span className="text-2xl font-bold text-success-700 dark:text-success-400">{completed}</span>
        </div>

        {/* Completion Rate */}
        <div className="flex flex-col p-4 rounded-xl bg-primary-50 dark:bg-primary-900/20 border border-primary-100 dark:border-primary-800/30">
          <div className="flex items-center gap-2 mb-2">
            <Award className="w-4 h-4 text-primary-600 dark:text-primary-400" />
            <span className="text-sm font-medium text-primary-800 dark:text-primary-300">Tỷ lệ</span>
          </div>
          <span className="text-2xl font-bold text-primary-700 dark:text-primary-400">{completionRate}%</span>
        </div>
      </div>
    </Card>
  );
}
