import { useQueries } from '@tanstack/react-query';
import { Activity } from 'lucide-react';
import { Card, SimpleBarChart, DonutChart, Badge, Button } from '../../components/ui';
import { userService } from '../../services/userService';
import { tutorialService } from '../../services/tutorialService';
import { projectService } from '../../services/projectService';
import type { Project, Tutorial, User } from '../../types';
import { getRoleName } from '../../types';

export function AdminOverview() {

  // Dùng useQueries để fetch song song cả 3 nguồn dữ liệu
  const [usersQuery, tutorialsQuery, projectsQuery] = useQueries({
    queries: [
      {
        queryKey: ['admin-users'],
        queryFn: () => userService.getAll(0, 100),
        staleTime: 30000,
      },
      {
        queryKey: ['admin-tutorials'],
        queryFn: () => tutorialService.getAllForAdmin(0, 100),
        staleTime: 30000,
      },
      {
        queryKey: ['admin-projects'],
        queryFn: () => projectService.getAllForAdmin(0, 100),
        staleTime: 30000,
      },
    ],
  });

  const isLoading = usersQuery.isLoading || tutorialsQuery.isLoading || projectsQuery.isLoading;

  const users: User[] = usersQuery.data || [];
  const tutorials: Tutorial[] = tutorialsQuery.data || [];
  const projects: Project[] = projectsQuery.data || [];

  // Tính toán stats từ data
  const totalViews = tutorials.reduce((sum, tutorial) => sum + (tutorial.views || 0), 0);

  const stats = [
    { label: 'Total Users', value: users.length.toString() },
    { label: 'Total Tutorials', value: tutorials.length.toString() },
    { label: 'Total Projects', value: projects.length.toString() },
    { label: 'Total Views', value: totalViews.toLocaleString() },
  ];

  // Top 5 users mới nhất
  const recentUsers = [...users]
    .sort((a, b) => Number(b.id) - Number(a.id))
    .slice(0, 5);

  // Category distribution
  const catMap = new Map<string, number>();
  tutorials.forEach((tutorial) => {
    const rawCat = typeof tutorial.category === 'object'
      ? tutorial.category.name || 'Other'
      : tutorial.category || 'Other';
    const cat = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
    catMap.set(cat, (catMap.get(cat) || 0) + 1);
  });
  const COLORS = ['#3b82f6', '#8b5cf6', '#f97316', '#10b981', '#6b7280', '#ef4444', '#eab308'];
  const totalCat = tutorials.length || 1;
  const distributionData = Array.from(catMap.entries()).map(([name, count], idx) => ({
    name,
    value: Math.round((count / totalCat) * 100),
    color: COLORS[idx % COLORS.length],
  }));

  // Weekly activity based on user registrations
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const weekData = days.map(day => ({ date: day, value: 0 }));
  users.forEach((user) => {
    if (user.createdAt) {
      const d = new Date(user.createdAt).getDay();
      if (!isNaN(d)) weekData[d].value += 1;
    }
  });

  // Recent activity – mix users, tutorials, projects
  const mixed = [
    ...users.map((user) => ({ id: `u-${user.id}`, action: 'New user registered', user: user.email || user.username, time: user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Recently', ts: new Date(user.createdAt || Date.now()).getTime() })),
    ...tutorials.map((tutorial) => ({ id: `t-${tutorial.id}`, action: `Tutorial published: ${tutorial.title}`, user: tutorial.createBy || 'admin', time: tutorial.createdAt ? new Date(tutorial.createdAt).toLocaleDateString() : 'Recently', ts: new Date(tutorial.createdAt || Date.now()).getTime() })),
    ...projects.map((project) => ({ id: `p-${project.id}`, action: `Project added: ${project.title}`, user: project.createBy || 'admin', time: project.createdAt ? new Date(project.createdAt).toLocaleDateString() : 'Recently', ts: new Date(project.createdAt || Date.now()).getTime() })),
  ].sort((a, b) => b.ts - a.ts).slice(0, 5);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Overview</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Admin dashboard overview</p>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, index) => (
          <Card key={index} className="p-5">
            <div className="flex flex-col items-center justify-center text-center">
              <p className="text-sm text-gray-500 dark:text-gray-400">{stat.label}</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-gray-100 mt-1">{stat.value}</p>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-6">User Registrations</h3>
          {weekData.some(d => d.value > 0) ? (
            <SimpleBarChart data={weekData} height={200} />
          ) : (
            <p className="text-center text-gray-500 dark:text-gray-400 py-8">No registration data yet</p>
          )}
        </Card>

        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-6">Content Distribution</h3>
          {distributionData.length > 0 ? (
            <DonutChart data={distributionData} size={180} />
          ) : (
            <p className="text-center text-gray-500 dark:text-gray-400 py-8">No content data yet</p>
          )}
        </Card>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Recent Activity</h3>
            <Button variant="ghost" size="sm">View all</Button>
          </div>
          <div className="space-y-4">
            {mixed.length > 0 ? mixed.map(activity => (
              <div key={activity.id} className="flex items-center gap-4 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
                  <Activity className="w-5 h-5 text-gray-500" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{activity.action}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">by {activity.user}</p>
                </div>
                <span className="text-xs text-gray-500 dark:text-gray-400">{activity.time}</span>
              </div>
            )) : (
              <p className="text-center text-gray-500 dark:text-gray-400 py-4">No recent activity</p>
            )}
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">New Users</h3>
            <Button variant="ghost" size="sm">View all</Button>
          </div>
          <div className="space-y-4">
            {recentUsers.length > 0 ? recentUsers.map(user => (
              <div key={user.id} className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center text-primary-700 dark:text-primary-300 font-semibold">
                  {((user.username || user.name) || 'U').charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">{user.username || user.name || 'Anonymous'}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user.email || 'No email'}</p>
                </div>
                <Badge variant={getRoleName(user.role) === 'ADMIN' ? 'primary' : 'secondary'}>
                  {getRoleName(user.role)}
                </Badge>
              </div>
            )) : (
              <p className="text-center text-gray-500 dark:text-gray-400 py-4">No users yet</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
