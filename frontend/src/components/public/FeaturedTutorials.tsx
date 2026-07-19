import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, Clock, Eye, Calendar } from 'lucide-react';
import { Card, Badge, Button, Avatar } from '../ui';
import { tutorialService } from '../../services/tutorialService';
import type { Tutorial } from '../../types';
import { formatReadTime, formatViews } from '../../utils/format';

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
  const { data: tutorials = [] } = useQuery({
    queryKey: ['featuredTutorials'],
    queryFn: () => tutorialService.getAll(0, 4)
  });

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
          {featured.map((tutorial: Tutorial) => (
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
  const rawCat = typeof tutorial.category === 'object' && tutorial.category 
    ? (tutorial.category as any).name 
    : tutorial.category || 'DevOps';
  const categoryName = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
  const color = categoryColors[categoryName] || 'primary';

  return (
    <Link to={`/tutorials/${tutorial.slug || tutorial.id}`} className={`block group ${featured ? 'md:col-span-2' : ''}`}>
      <Card hover className="overflow-hidden h-full">
        <div className="relative">
          <img
            src={tutorial.coverImage || tutorial.thumbnail}
            alt={tutorial.title}
            className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <Badge
            variant={color}
            className="absolute top-3 left-3 capitalize"
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
                {tutorial.authorName || (tutorial.author?.name || tutorial.createBy || 'Administrator').split('@')[0]}
              </p>
            </div>
          </div>

          <div className="mt-4 flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
            <div className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              {formatReadTime(tutorial.readTime, tutorial.content, tutorial.description)} min
            </div>
            <div className="flex items-center gap-1">
              <Eye className="w-3.5 h-3.5" />
              {formatViews(tutorial.views, tutorial.viewCount)}
            </div>
            <div className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              {new Date(tutorial.createdAt || tutorial.publishDate || Date.now()).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
            </div>
          </div>
        </div>
      </Card>
    </Link>
  );
}
