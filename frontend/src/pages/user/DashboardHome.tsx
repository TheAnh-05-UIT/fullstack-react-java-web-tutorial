import { useState, useEffect } from 'react';
import { BookOpen, FolderKanban, Award, Flame } from 'lucide-react';
import { Card, StatCard, DonutChart, Button, Badge } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';
import type { Tutorial } from '../../types';

export function DashboardHome() {
  const { user } = useAuth();
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);

  useEffect(() => {
    const fetchTutorials = async () => {
      try {
        // Backend Java trả về thẳng mảng dữ liệu (đã bóc vỏ FormatRestResponse trong api.ts)
        const data = await api.get<any, Tutorial[]>('/tutorials');
        setTutorials(data || []);
      } catch (error) {
        console.error('Failed to fetch tutorials:', error);
      }
    };
    fetchTutorials();
  }, []);

  // Tính toán dữ liệu biểu đồ
  const categoryMap = new Map<string, number>();
  tutorials.forEach(t => {
    const cat = typeof t.category === 'object' && t.category ? (t.category as any).name : t.category || 'Khác';
    categoryMap.set(cat, (categoryMap.get(cat) || 0) + 1);
  });
  
  const COLORS = ['#3b82f6', '#8b5cf6', '#f97316', '#10b981', '#6b7280', '#ef4444', '#eab308'];
  const categoryDistribution = Array.from(categoryMap.entries()).map(([name, count], idx) => ({
    name,
    value: tutorials.length > 0 ? Math.round((count / tutorials.length) * 100) : 0,
    color: COLORS[idx % COLORS.length],
  }));

  // Tạm thời mock dữ liệu thống kê cá nhân vì Backend chưa có API User Profile
  const userStats = {
    learningStreak: 5,
    coursesCompleted: 2,
    articlesRead: 12,
    projectsFinished: 1
  };

  return (
    <div className="space-y-8">
      <div className="bg-gradient-to-r from-primary-600 to-secondary-600 rounded-2xl p-6 text-white">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Chào mừng trở lại, {user?.username || 'Bạn'}!</h1>
            <p className="mt-1 text-primary-100">Tiếp tục hành trình trở thành DevOps Engineer nhé!</p>
          </div>
          <div className="hidden sm:flex items-center gap-2">
            <Flame className="w-6 h-6 text-orange-300" />
            <span className="text-lg font-semibold">{userStats.learningStreak} ngày rèn luyện!</span>
          </div>
        </div>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={<Award className="w-5 h-5" />}
          label="Khóa học hoàn thành"
          value={userStats.coursesCompleted}
        />
        <StatCard
          icon={<BookOpen className="w-5 h-5" />}
          label="Bài viết đã đọc"
          value={userStats.articlesRead}
        />
        <StatCard
          icon={<FolderKanban className="w-5 h-5" />}
          label="Dự án thực hành"
          value={userStats.projectsFinished}
        />
        <StatCard
          icon={<Flame className="w-5 h-5" />}
          label="Chuỗi ngày học"
          value={`${userStats.learningStreak} ngày`}
        />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Tỉ lệ nội dung</h3>
            <span className="text-sm text-gray-500 dark:text-gray-400">Theo danh mục</span>
          </div>
          {categoryDistribution.length > 0 ? (
            <DonutChart data={categoryDistribution} size={160} />
          ) : (
            <p className="text-center text-gray-500 dark:text-gray-400 py-8">Chưa có dữ liệu</p>
          )}
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Khóa học mới nhất</h3>
            <Button variant="ghost" size="sm">Xem tất cả</Button>
          </div>
          <div className="space-y-4">
            {tutorials.length > 0 ? tutorials.slice(0, 4).map((tutorial) => (
              <div key={tutorial.id} className="flex items-start gap-4 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                <div className="w-16 h-16 rounded-lg overflow-hidden shrink-0">
                  <img src={tutorial.coverImage || 'https://via.placeholder.com/150'} alt="" className="w-full h-full object-cover" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge variant="primary" className="text-xs">
                      {typeof tutorial.category === 'object' && tutorial.category ? (tutorial.category as any).name : tutorial.category || 'Chung'}
                    </Badge>
                    <span className="text-xs text-gray-500 dark:text-gray-400">{tutorial.readTime || 5} phút</span>
                  </div>
                  <h4 className="font-medium text-gray-900 dark:text-gray-100 line-clamp-1">{tutorial.title}</h4>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5 line-clamp-1">{tutorial.description}</p>
                </div>
              </div>
            )) : (
              <p className="text-center text-gray-500 dark:text-gray-400 py-4">Chưa có bài học nào</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
