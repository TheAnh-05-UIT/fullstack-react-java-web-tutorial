import { useState, useEffect } from 'react';
import { BookOpen, Clock, CheckCircle, TrendingUp, Award, Target, ChevronRight } from 'lucide-react';
import { Card, Badge, Button, ProgressRing } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';
import type { PagedResponse, Tutorial, Roadmap } from '../../types';

export function DashboardLearning() {
  const { user } = useAuth();
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);
  const [roadmaps, setRoadmaps] = useState<Roadmap[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [tutorialsData, roadmapsData] = await Promise.all([
          api.get<any, PagedResponse<Tutorial>>('/tutorials?page=0&size=100').then(res => res?.content || []),
          api.get<any, PagedResponse<Roadmap>>('/roadmaps?page=0&size=100').then(res => res?.content || []),
        ]);
        setTutorials(tutorialsData || []);
        setRoadmaps(roadmapsData || []);
      } catch (error) {
        console.error('Failed to fetch learning data:', error);
      }
    };
    fetchData();
  }, []);

  const currentRoadmap = roadmaps.length > 0 ? roadmaps[0] : null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">My Learning</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Track your progress and continue where you left off</p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500 dark:text-gray-400">Total XP:</span>
          <Badge variant="accent" className="text-base px-3 py-1">
            <Award className="w-4 h-4" />
            {(((user as any)?.coursesCompleted || 0) * 100 + ((user as any)?.articlesRead || 0) * 10).toLocaleString()} XP
          </Badge>
        </div>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        <Card className="p-6 lg:col-span-2">
          <div className="flex items-center gap-2 mb-6">
            <Target className="w-5 h-5 text-primary-600" />
            <h2 className="font-semibold text-gray-900 dark:text-gray-100">Current Roadmap</h2>
          </div>
          {currentRoadmap ? (
            <div className="flex items-start gap-6">
              <ProgressRing progress={currentRoadmap.steps?.length ? Math.round((1 / currentRoadmap.steps.length) * 100) : 0} size={100} />
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{currentRoadmap.title}</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{currentRoadmap.description}</p>
                <div className="mt-4">
                  <div className="flex items-center justify-between text-sm mb-2">
                    <span className="text-gray-500 dark:text-gray-400">Step 1 of {currentRoadmap.steps?.length || 0}</span>
                    <span className="font-medium text-primary-600 dark:text-primary-400">{currentRoadmap.steps?.[0]?.title || ''}</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden">
                    <div
                      className="h-full rounded-full bg-gradient-to-r from-primary-500 to-secondary-500"
                      style={{ width: `${currentRoadmap.steps?.length ? (1 / currentRoadmap.steps.length) * 100 : 0}%` }}
                    />
                  </div>
                </div>
                <Button size="sm" className="mt-4">
                  Continue Learning
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          ) : (
            <p className="text-gray-500 dark:text-gray-400">No roadmaps available yet. Check back later!</p>
          )}
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="w-5 h-5 text-success-500" />
            <h2 className="font-semibold text-gray-900 dark:text-gray-100">Learning Stats</h2>
          </div>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-gray-800">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-primary-100 dark:bg-primary-900/30">
                  <BookOpen className="w-4 h-4 text-primary-600 dark:text-primary-400" />
                </div>
                <span className="text-sm text-gray-600 dark:text-gray-400">Tutorials Completed</span>
              </div>
              <span className="font-semibold text-gray-900 dark:text-gray-100">{(user as any)?.coursesCompleted || 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-gray-800">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-secondary-100 dark:bg-secondary-900/30">
                  <Clock className="w-4 h-4 text-secondary-600 dark:text-secondary-400" />
                </div>
                <span className="text-sm text-gray-600 dark:text-gray-400">Articles Read</span>
              </div>
              <span className="font-semibold text-gray-900 dark:text-gray-100">{(user as any)?.articlesRead || 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-gray-800">
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-lg bg-success-100 dark:bg-success-900/30">
                  <CheckCircle className="w-4 h-4 text-success-600 dark:text-success-400" />
                </div>
                <span className="text-sm text-gray-600 dark:text-gray-400">Projects Finished</span>
              </div>
              <span className="font-semibold text-gray-900 dark:text-gray-100">{(user as any)?.projectsFinished || 0}</span>
            </div>
          </div>
        </Card>
      </div>

      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-primary-600" />
            <h2 className="font-semibold text-gray-900 dark:text-gray-100">Available Tutorials</h2>
          </div>
          <Button variant="ghost" size="sm">View all</Button>
        </div>
        <div className="space-y-4">
          {tutorials.length > 0 ? tutorials.slice(0, 5).map(tutorial => (
            <div key={tutorial.id} className="flex items-center gap-4 p-4 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors group cursor-pointer">
              <div className="w-20 h-14 rounded-lg overflow-hidden shrink-0 relative">
                <img src={tutorial.coverImage} alt="" className="w-full h-full object-cover" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <Badge variant="primary" className="text-xs">{typeof tutorial.category === 'object' && tutorial.category ? (tutorial.category as any).name : tutorial.category || 'DevOps'}</Badge>
                  <span className="text-xs text-gray-500 dark:text-gray-400">{tutorial.readTime} min</span>
                </div>
                <h4 className="font-medium text-gray-900 dark:text-gray-100 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                  {tutorial.title}
                </h4>
              </div>
              <Button size="sm" variant="primary">
                Start
              </Button>
            </div>
          )) : (
            <p className="text-center text-gray-500 dark:text-gray-400 py-8">No tutorials available yet.</p>
          )}
        </div>
      </Card>

      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-2">
            <Award className="w-5 h-5 text-accent-600" />
            <h2 className="font-semibold text-gray-900 dark:text-gray-100">Achievements</h2>
          </div>
        </div>
        <div className="flex flex-wrap gap-4">
          {[
            { icon: '🔥', label: '7 Day Streak', description: 'Learn every day for a week' },
            { icon: '📚', label: 'Speed Reader', description: 'Completed 5 tutorials' },
            { icon: '🐳', label: 'Docker Pro', description: 'Mastered Docker basics' },
            { icon: '⚡', label: 'Quick Learner', description: 'Finished a tutorial in one day' },
          ].map((achievement, index) => (
            <div key={index} className="flex items-center gap-3 p-3 rounded-xl bg-gradient-to-r from-accent-50 to-warning-50 dark:from-accent-900/20 dark:to-warning-900/20 border border-accent-100 dark:border-accent-900/30">
              <span className="text-2xl">{achievement.icon}</span>
              <div>
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{achievement.label}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">{achievement.description}</p>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
