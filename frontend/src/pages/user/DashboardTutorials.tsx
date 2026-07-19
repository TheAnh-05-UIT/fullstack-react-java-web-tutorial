import { useState, useEffect } from 'react';
import { Edit, Trash2, Eye } from 'lucide-react';
import { Card, Badge, Button, SearchInput, LoadingSpinner, EmptyState, ErrorState } from '../../components/ui';
import { tutorialService } from '../../services/tutorialService';
import type { Tutorial } from '../../types';
import { formatReadTime, formatViews } from '../../utils/format';

const statusColors = {
  'Published': 'success',
  'Draft': 'warning',
  'Scheduled': 'primary',
} as const;

export function DashboardTutorials() {
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<'all' | 'Published' | 'Draft' | 'Scheduled'>('all');

  const fetchTutorials = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await tutorialService.getAll(0, 100);
      setTutorials(data);
    } catch (err) {
      console.error('Failed to fetch tutorials:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTutorials();
  }, []);

  // Use a pseudo status since backend doesn't have status yet
  const articlesWithStatus = tutorials.map((t, idx) => ({
    ...t,
    status: ['Published', 'Draft', 'Scheduled'][idx % 3] as 'Published' | 'Draft' | 'Scheduled',
  }));

  const filteredArticles = articlesWithStatus.filter(article => {
    const matchesSearch = article.title?.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = selectedStatus === 'all' || article.status === selectedStatus;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">My Tutorials</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage your articles and tutorials</p>
        </div>
        <Button>+ New Article</Button>
      </div>

      <Card className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <SearchInput
            placeholder="Search articles..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1"
          />
          <div className="flex gap-2">
            {(['all', 'Published', 'Draft', 'Scheduled'] as const).map(status => (
              <Button
                key={status}
                variant={selectedStatus === status ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setSelectedStatus(status)}
              >
                {status === 'all' ? 'All' : status}
              </Button>
            ))}
          </div>
        </div>
      </Card>

      <Card className="overflow-hidden">
        {loading ? (
          <LoadingSpinner text="Loading your tutorials..." />
        ) : error ? (
          <ErrorState 
            title="Failed to Load Tutorials" 
            error={error} 
            onRetry={fetchTutorials} 
          />
        ) : filteredArticles.length === 0 ? (
          <EmptyState
            title="No tutorials found"
            description="You haven't written any tutorials matching your current filters yet."
            actionLabel="Reset Filters"
            onAction={() => { setSearchQuery(''); setSelectedStatus('all'); }}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
                <tr>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Title</th>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Category</th>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Views</th>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
                  <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Date</th>
                  <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {filteredArticles.map(article => (
                  <tr key={article.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-lg overflow-hidden shrink-0">
                          <img src={article.coverImage || article.thumbnail} alt={article.title || 'Tutorial thumbnail'} className="w-full h-full object-cover" />
                        </div>
                        <div>
                          <p className="font-medium text-gray-900 dark:text-gray-100 line-clamp-1">{article.title}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{formatReadTime(article.readTime, article.content, article.description)} min read</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <Badge variant="primary">
                        {typeof article.category === 'object' && article.category
                          ? (article.category.name || 'DevOps')
                          : (typeof article.category === 'string' ? article.category : 'DevOps')}
                      </Badge>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
                        <Eye className="w-4 h-4" aria-hidden="true" />
                        {formatViews(article.views, article.viewCount)}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <Badge variant={statusColors[article.status]}>{article.status}</Badge>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                      {new Date(article.createdAt || article.publishDate || Date.now()).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button type="button" aria-label={`Edit ${article.title}`} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 dark:text-gray-400 transition-colors">
                          <Edit className="w-4 h-4" aria-hidden="true" />
                        </button>
                        <button type="button" aria-label={`Delete ${article.title}`} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 dark:text-gray-400 transition-colors">
                          <Trash2 className="w-4 h-4" aria-hidden="true" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
