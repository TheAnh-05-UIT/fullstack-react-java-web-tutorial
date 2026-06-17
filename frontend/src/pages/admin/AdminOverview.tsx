import type { PagedResponse } from '../../types';
import { useState, useEffect } from 'react';
import { Activity } from 'lucide-react';
import { Card, SimpleBarChart, DonutChart, Badge, Button } from '../../components/ui';
import { api } from '../../services/api';


export function AdminOverview() {
  const [stats, setStats] = useState([
    { label: 'Total Users', value: '0' },
    { label: 'Total Tutorials', value: '0' },
    { label: 'Total Projects', value: '0' },
    { label: 'Total Views', value: '0' },
  ]);
  const [users, setUsers] = useState<any[]>([]);
  const [activityData, setActivityData] = useState<any[]>([]);
  const [distributionData, setDistributionData] = useState<any[]>([]);
  const [recentActivities, setRecentActivities] = useState<any[]>([]);

  useEffect(() => {
    const fetchAllData = async () => {
      try {
        const [usersData, tutorialsData, projectsData] = await Promise.all([
          api.get<any, PagedResponse<any>>('/users?page=0&size=100').then(res => res?.content || []),
          api.get<any, PagedResponse<any>>('/tutorials?page=0&size=100').then(res => res?.content || []),
          api.get<any, PagedResponse<any>>('/projects?page=0&size=100').then(res => res?.content || []),
        ]);

        const users = usersData || [];
        const tutorials = tutorialsData || [];
        const projects = projectsData || [];

        const totalViews = tutorials.reduce((sum, t) => sum + (t.views || 0), 0);

        setStats([
          { label: 'Total Users', value: users.length.toString() },
          { label: 'Total Tutorials', value: tutorials.length.toString() },
          { label: 'Total Projects', value: projects.length.toString() },
          { label: 'Total Views', value: totalViews.toLocaleString() },
        ]);

        setUsers(users.sort((a, b) => b.id - a.id).slice(0, 5));

        // Category distribution from tutorials
        const catMap = new Map<string, number>();
        tutorials.forEach(t => {
          const cat = typeof t.category === 'object' && t.category ? (t.category as any).name : t.category || 'Other';
          catMap.set(cat, (catMap.get(cat) || 0) + 1);
        });

        const COLORS = ['#3b82f6', '#8b5cf6', '#f97316', '#10b981', '#6b7280', '#ef4444', '#eab308'];
        const totalCat = tutorials.length || 1;

        const dist = Array.from(catMap.entries()).map(([name, count], idx) => ({
          name,
          value: Math.round((count / totalCat) * 100),
          color: COLORS[idx % COLORS.length]
        }));
        setDistributionData(dist);

        // Weekly activity based on user registrations
        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const weekData = days.map(day => ({ date: day, value: 0 }));
        users.forEach(u => {
          if (u.joinDate) {
            const d = new Date(u.joinDate).getDay();
            if (!isNaN(d)) weekData[d].value += 1;
          }
        });
        setActivityData(weekData);

        // Recent activity mixed from users, tutorials, projects
        const mixed = [
          ...users.map(u => ({ id: `u-${u.id}`, action: 'New user registered', user: u.email || u.name, time: u.joinDate || 'Recently', ts: u.id })),
          ...tutorials.map(t => ({ id: `t-${t.id}`, action: `Tutorial published: ${t.title}`, user: 'admin', time: t.publishDate || 'Recently', ts: t.id + 1000 })),
          ...projects.map(p => ({ id: `p-${p.id}`, action: `Project added: ${p.title}`, user: 'admin', time: 'Recently', ts: p.id + 500 })),
        ];
        mixed.sort((a, b) => b.ts - a.ts);
        setRecentActivities(mixed.slice(0, 5));

      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
      }
    };
    fetchAllData();
  }, []);

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
          {activityData.some(d => d.value > 0) ? (
            <SimpleBarChart data={activityData} height={200} />
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
            {recentActivities.length > 0 ? recentActivities.map(activity => (
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
            {users.length > 0 ? users.map(user => (
              <div key={user.id} className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center text-primary-700 dark:text-primary-300 font-semibold">
                  {(user.name || 'U').charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">{user.name || 'Anonymous'}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user.email || 'No email'}</p>
                </div>
                <Badge variant={user.role === 'ADMIN' ? 'primary' : 'secondary'}>{user.role || 'USER'}</Badge>
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
