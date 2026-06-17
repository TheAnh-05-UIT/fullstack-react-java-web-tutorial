import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Infinity, Cloud, Shield, Layers, Container } from 'lucide-react';
import { Card, Button } from '../ui';
import { api } from '../../services/api';
import type { PagedResponse, Roadmap } from '../../types';

const iconComponents: Record<string, React.ReactNode> = {
  'infinity': <Infinity className="w-6 h-6" />,
  'cloud': <Cloud className="w-6 h-6" />,
  'shield': <Shield className="w-6 h-6" />,
  'layers': <Layers className="w-6 h-6" />,
  'container': <Container className="w-6 h-6" />,
};

const colorClasses: Record<string, { bg: string; text: string; border: string }> = {
  'primary': {
    bg: 'bg-primary-100 dark:bg-primary-900/30',
    text: 'text-primary-600 dark:text-primary-400',
    border: 'group-hover:border-primary-300 dark:group-hover:border-primary-700',
  },
  'secondary': {
    bg: 'bg-secondary-100 dark:bg-secondary-900/30',
    text: 'text-secondary-600 dark:text-secondary-400',
    border: 'group-hover:border-secondary-300 dark:group-hover:border-secondary-700',
  },
  'accent': {
    bg: 'bg-accent-100 dark:bg-accent-900/30',
    text: 'text-accent-600 dark:text-accent-400',
    border: 'group-hover:border-accent-300 dark:group-hover:border-accent-700',
  },
  'success': {
    bg: 'bg-success-100 dark:bg-success-900/30',
    text: 'text-success-600 dark:text-success-400',
    border: 'group-hover:border-success-300 dark:group-hover:border-success-700',
  },
  'warning': {
    bg: 'bg-warning-100 dark:bg-warning-900/30',
    text: 'text-warning-600 dark:text-warning-400',
    border: 'group-hover:border-warning-300 dark:group-hover:border-warning-700',
  },
};

export function FeaturedRoadmaps() {
  const [roadmaps, setRoadmaps] = useState<Roadmap[]>([]);

  useEffect(() => {
    const fetchRoadmaps = async () => {
      try {
        const data = await api.get<PagedResponse<Roadmap>>('/roadmaps?page=0&size=3');
        if (data && data.content) {
          setRoadmaps(data.content);
        }
      } catch (error) {
        console.error('Failed to fetch roadmaps:', error);
      }
    };
    fetchRoadmaps();
  }, []);

  return (
    <section className="py-20 bg-gray-50 dark:bg-gray-900">
      <div className="container-app">
        <div className="flex items-end justify-between mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
              Learning Roadmaps
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">
              Structured paths to guide your DevOps journey
            </p>
          </div>
          <Link to="/roadmaps">
            <Button variant="ghost">
              View all
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {roadmaps.slice(0, 5).map(roadmap => (
            <RoadmapCard key={roadmap.id} roadmap={roadmap} />
          ))}
        </div>
      </div>
    </section>
  );
}

interface RoadmapCardProps {
  roadmap: Roadmap;
}

export function RoadmapCard({ roadmap }: RoadmapCardProps) {
  const colors = colorClasses[roadmap.color] || colorClasses.primary;
  const completion = Math.floor(Math.random() * 100);

  return (
    <Card hover className={`p-6 group ${colors.border}`}>
      <Link to={`/roadmaps/${roadmap.id}`} className="flex items-start gap-4">
        <div className={`p-3 rounded-xl ${colors.bg} ${colors.text} transition-colors`}>
          {iconComponents[roadmap.icon] || <Infinity className="w-6 h-6" />}
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
            {roadmap.title}
          </h3>
          <p className="mt-1 text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
            {roadmap.description}
          </p>
          <div className="mt-4 flex items-center justify-between">
            <span className="text-xs text-gray-500 dark:text-gray-400">
              {roadmap.steps?.length || 0} steps
            </span>
            <div className="flex items-center gap-2">
              <div className="w-24 h-1.5 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden">
                <div
                  className={`h-full rounded-full ${colors.bg.replace('bg-', 'bg-').replace('-100', '-500').replace('-900/30', '-500')}`}
                  style={{ width: `${completion}%` }}
                />
              </div>
              <span className="text-xs text-gray-500 dark:text-gray-400">{completion}%</span>
            </div>
          </div>
        </div>
      </Link>
    </Card>
  );
}
