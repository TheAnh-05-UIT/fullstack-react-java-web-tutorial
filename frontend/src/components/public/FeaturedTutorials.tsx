import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Clock, Eye, Calendar } from 'lucide-react';
import { Card, Badge, Button, Avatar } from '../ui';
import { api } from '../../services/api';
import type { PagedResponse, Tutorial } from '../../types';

const categoryColors: Record<string, 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'error'> = {
  'DevOps': 'primary',
  'Docker': 'secondary',
  'Kubernetes': 'accent',
  'AWS': 'success',
  'Terraform': 'warning',
  'CI/CD': 'primary',
  'Linux': 'secondary',
  'Monitoring': 'accent',
  'Security': 'error',
};

export function FeaturedTutorials() {
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);

  useEffect(() => {
    const fetchTutorials = async () => {
      try {
        const data = await api.get<any, PagedResponse<Tutorial>>('/tutorials?page=0&size=3');
        if (data && data.content) {
          setTutorials(data.content);
        }
      } catch (error) {
        console.error('Failed to fetch featured tutorials:', error);
      } finally {
        // loading state handling if any
      }
    };
    fetchTutorials();
  }, []);

  const featured = tutorials.slice(0, 4);

  return (
    <section className="py-20 bg-gray-50 dark:bg-gray-900">
      <div className="container-app">
        <div className="flex items-end justify-between mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
              Featured Tutorials
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">
              Most popular guides chosen by our community
            </p>
          </div>
          <Link to="/tutorials">
            <Button variant="ghost">
              View all
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          {featured.map(tutorial => (
            <TutorialCard key={tutorial.id} tutorial={tutorial} />
          ))}
        </div>
      </div>
    </section>
  );
}

interface TutorialCardProps {
  tutorial: Tutorial;
  featured?: boolean;
}

export function TutorialCard({ tutorial, featured = false }: TutorialCardProps) {
  const categoryName = typeof tutorial.category === 'object' && tutorial.category 
    ? (tutorial.category as any).name 
    : tutorial.category || 'DevOps';
  const color = categoryColors[categoryName] || 'primary';

  return (
    <Link to={`/tutorials/${tutorial.id}`} className={`block group ${featured ? 'md:col-span-2' : ''}`}>
      <Card hover className="overflow-hidden h-full">
        <div className="relative">
          <img
            src={tutorial.coverImage}
            alt={tutorial.title}
            className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <Badge
            variant={color}
            className="absolute top-3 left-3"
          >
            {categoryName}
          </Badge>
        </div>
        <div className="p-5">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
            {tutorial.title}
          </h3>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
            {tutorial.description}
          </p>

          <div className="mt-4 flex items-center gap-3">
            <Avatar src={tutorial.author?.avatar} alt={tutorial.author?.name} size="sm" />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                {tutorial.author?.name}
              </p>
            </div>
          </div>

          <div className="mt-4 flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
            <div className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              {tutorial.readTime} min
            </div>
            <div className="flex items-center gap-1">
              <Eye className="w-3.5 h-3.5" />
              {(tutorial.views || 0) >= 1000 ? ((tutorial.views || 0) / 1000).toFixed(1) + 'K' : (tutorial.views || 0)}
            </div>
            <div className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              {new Date(tutorial.publishDate || Date.now()).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
            </div>
          </div>
        </div>
      </Card>
    </Link>
  );
}
