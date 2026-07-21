import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RefreshCw, FilterX, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, Badge, Button, LoadingSpinner } from '../../components/ui';
import { useLearningProgressList } from '../../features/learning-progress/hooks/useLearningProgress';
import type { LearningProgressListFilters, LearningContentType } from '../../features/learning-progress/types/learningProgress.types';
import { LearningProgressListCard } from '../../features/learning-progress/components/LearningProgressListCard';


type StatusFilter = 'ALL' | 'IN_PROGRESS' | 'COMPLETED';
type ContentTypeFilter = 'ALL' | LearningContentType;

export function DashboardLearningProgress() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<{
    page: number;
    size: number;
    status: StatusFilter;
    contentType: ContentTypeFilter;
  }>({
    page: 0,
    size: 10,
    status: 'ALL',
    contentType: 'ALL',
  });

  // Convert local state to API filters (omitting 'ALL')
  const apiFilters: LearningProgressListFilters = {
    page: filters.page,
    size: filters.size,
    ...(filters.status !== 'ALL' ? { status: filters.status } : {}),
    ...(filters.contentType !== 'ALL' ? { contentType: filters.contentType } : {}),
  };

  const { data, isLoading, isError, refetch, isFetching, isPlaceholderData } = useLearningProgressList(apiFilters);

  const handleFilterChange = (key: keyof typeof filters, value: string) => {
    setFilters(prev => ({
      ...prev,
      [key]: value,
      page: 0, // Reset to first page when changing filters
    }));
  };

  const handleClearFilters = () => {
    setFilters(prev => ({
      ...prev,
      status: 'ALL',
      contentType: 'ALL',
      page: 0,
    }));
  };

  const handlePageChange = (newPage: number) => {
    if (!data) return;
    if (newPage >= 0 && newPage < data.totalPages) {
      setFilters(prev => ({ ...prev, page: newPage }));
    }
  };

  const totalPages = data?.totalPages;

  useEffect(() => {
    if (totalPages === undefined || isPlaceholderData) {
      return;
    }

    setFilters((current) => {
      if (totalPages === 0 && current.page !== 0) {
        return {
          ...current,
          page: 0,
        };
      }

      if (totalPages > 0 && current.page >= totalPages) {
        return {
          ...current,
          page: totalPages - 1,
        };
      }

      return current;
    });
  }, [totalPages, isPlaceholderData]);

  const isEmptyOverall = data?.content.length === 0 && filters.status === 'ALL' && filters.contentType === 'ALL';
  const isEmptyFiltered = data?.content.length === 0 && (filters.status !== 'ALL' || filters.contentType !== 'ALL');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Tiến độ học tập</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Theo dõi các nội dung bạn đang học và đã hoàn thành.</p>
        </div>
        {data && (
          <Badge variant="primary" className="text-sm px-3 py-1">
            Tổng cộng: {data.totalElements}
          </Badge>
        )}
      </div>

      {/* Toolbar */}
      <Card className="p-4 flex flex-col sm:flex-row gap-4">
        <div className="flex-1 flex flex-col sm:flex-row gap-4">
          <select
            value={filters.status}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            className="w-full sm:w-48 px-3 py-2 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-primary-500 outline-none"
            aria-label="Lọc theo trạng thái"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="IN_PROGRESS">Đang học</option>
            <option value="COMPLETED">Đã hoàn thành</option>
          </select>

          <select
            value={filters.contentType}
            onChange={(e) => handleFilterChange('contentType', e.target.value)}
            className="w-full sm:w-48 px-3 py-2 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-primary-500 outline-none"
            aria-label="Lọc theo loại nội dung"
          >
            <option value="ALL">Tất cả loại nội dung</option>
            <option value="TUTORIAL">Tutorial</option>
            <option value="PROJECT">Project</option>
            <option value="ROADMAP">Roadmap</option>
            <option value="DEVOPS_PHASE">DevOps Lifecycle</option>
          </select>
        </div>
        {(filters.status !== 'ALL' || filters.contentType !== 'ALL') && (
          <Button variant="ghost" size="sm" onClick={handleClearFilters} className="sm:self-center">
            <FilterX className="w-4 h-4 mr-2" />
            Xóa bộ lọc
          </Button>
        )}
      </Card>

      {/* Content */}
      <div className={`transition-opacity duration-200 ${isFetching && !isLoading ? 'opacity-60' : 'opacity-100'}`}>
        {isLoading ? (
          <Card className="p-12 flex justify-center items-center">
            <LoadingSpinner className="w-8 h-8" />
          </Card>
        ) : isError ? (
          <Card className="p-12 text-center">
            <div className="text-red-500 dark:text-red-400 mb-4 flex justify-center">
              <RefreshCw className="w-12 h-12" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">Không thể tải danh sách tiến độ học tập.</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Đã xảy ra lỗi khi kết nối tới máy chủ.</p>
            <Button onClick={() => refetch()} variant="primary">Thử lại</Button>
          </Card>
        ) : isEmptyOverall ? (
          <Card className="p-12 text-center">
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">Bạn chưa bắt đầu nội dung học nào.</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Hãy khám phá các bài hướng dẫn hoặc dự án để bắt đầu hành trình của bạn.</p>
            <Button onClick={() => navigate('/tutorials')} variant="primary">Khám phá ngay</Button>
          </Card>
        ) : isEmptyFiltered ? (
          <Card className="p-12 text-center">
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">Không có nội dung phù hợp với bộ lọc.</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Thử thay đổi trạng thái hoặc loại nội dung để xem các kết quả khác.</p>
            <Button onClick={handleClearFilters} variant="primary">Xóa bộ lọc</Button>
          </Card>
        ) : (
          <div className="grid lg:grid-cols-2 xl:grid-cols-3 gap-6">
            {data?.content.map((item) => (
              <LearningProgressListCard
                key={`${item.contentType}-${item.contentKey}`}
                item={item}
              />
            ))}
          </div>
        )}
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-gray-200 dark:border-gray-800 pt-6">
          <Button
            variant="ghost"
            onClick={() => handlePageChange(filters.page - 1)}
            disabled={data.first || filters.page <= 0}
            className="flex items-center"
            aria-label="Trang trước"
          >
            <ChevronLeft className="w-4 h-4 mr-1" />
            Trước
          </Button>
          <span className="text-sm font-medium text-gray-600 dark:text-gray-400">
            Trang {filters.page + 1} / {data.totalPages}
          </span>
          <Button
            variant="ghost"
            onClick={() => handlePageChange(filters.page + 1)}
            disabled={data.last || filters.page >= data.totalPages - 1}
            className="flex items-center"
            aria-label="Trang sau"
          >
            Sau
            <ChevronRight className="w-4 h-4 ml-1" />
          </Button>
        </div>
      )}
    </div>
  );
}
