import { useState, useMemo, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Filter, X } from 'lucide-react';
import { TutorialCard } from '../../components/public';
import { Button, SearchInput, Badge, LoadingSpinner, EmptyState, ErrorState } from '../../components/ui';
import type { Category, Tutorial } from '../../types';
import { tutorialService } from '../../services';

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

export function TutorialsPage() {
  const [selectedCategory, setSelectedCategory] = useState<Category | 'all'>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [currentPage, selectedCategory]);

  // Fetch all tutorials so we can compute exact category counts and filter globally across all pages
  const { data: allTutorials = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['tutorials-all'],
    // Gọi qua tutorialService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
    queryFn: () => tutorialService.getAll(0, 1000)
  });

  // Calculate global category counts across ALL tutorials
  const categoryCounts = useMemo(() => {
    const counts = new Map<string, number>();
    allTutorials.forEach((tutorial: Tutorial) => {
      const rawCat = typeof tutorial.category === 'object' && tutorial.category && 'name' in tutorial.category
        ? String((tutorial.category as Record<string, unknown>).name || '')
        : tutorial.category || 'Other';
      const catName = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
      counts.set(catName, (counts.get(catName) || 0) + 1);
    });
    return counts;
  }, [allTutorials]);

  const dynamicCategories = useMemo(() => {
    return Array.from(categoryCounts.entries()).sort((a, b) => b[1] - a[1]);
  }, [categoryCounts]);

  const filteredTutorials = useMemo(() => {
    return allTutorials.filter((tutorial: Tutorial) => {
      const rawCat = typeof tutorial.category === 'object' && tutorial.category && 'name' in tutorial.category
        ? String((tutorial.category as Record<string, unknown>).name || '')
        : tutorial.category || 'Other';
      const categoryName = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
      
      const matchesCategory = selectedCategory === 'all' || categoryName.toLowerCase() === selectedCategory.toLowerCase();
      const matchesSearch = (tutorial.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (tutorial.description || '').toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [allTutorials, selectedCategory, searchQuery]);

  // Client-side pagination for filtered results
  const pageSize = 9;
  const totalPages = Math.ceil(filteredTutorials.length / pageSize) || 1;
  const paginatedTutorials = useMemo(() => {
    return filteredTutorials.slice(currentPage * pageSize, (currentPage + 1) * pageSize);
  }, [filteredTutorials, currentPage]);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950">
        <div className="container-app py-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100">
            Tutorials
          </h1>
          <p className="mt-2 text-lg text-gray-600 dark:text-gray-400">
            Learn DevOps through practical tutorials and hands-on guides
          </p>
        </div>
      </div>

      <div className="container-app py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          <aside className="lg:w-64 shrink-0">
            <div className="sticky top-24">
              <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-800 p-5">
                <div className="flex items-center gap-2 mb-4">
                  <Filter className="w-4 h-4 text-gray-500" />
                  <h3 className="font-semibold text-gray-900 dark:text-gray-100">Categories</h3>
                </div>
                <div className="flex flex-wrap lg:flex-col gap-2">
                  <button
                    onClick={() => { setSelectedCategory('all'); setCurrentPage(0); }}
                    className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${selectedCategory === 'all'
                        ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400 font-medium'
                        : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                      }`}
                  >
                    All Categories
                  </button>
                  {dynamicCategories.map(([category, count]) => (
                    <button
                      key={category}
                      onClick={() => { setSelectedCategory(category); setCurrentPage(0); }}
                      className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between ${selectedCategory.toLowerCase() === category.toLowerCase()
                          ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400 font-medium'
                          : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                        }`}
                    >
                      <span className="capitalize">{category}</span>
                      <span className="text-xs text-gray-400">
                        {count}
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          <div className="flex-1">
            <div className="mb-6">
              <SearchInput
                placeholder="Search tutorials..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-md"
              />
            </div>

            <div className="flex items-center justify-between mb-6">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                {filteredTutorials.length} {filteredTutorials.length === 1 ? 'tutorial' : 'tutorials'} found
              </p>
              <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-hide">
                {selectedCategory !== 'all' && (
                  <Badge
                    variant={categoryColors[selectedCategory] || 'primary'}
                    className="flex items-center gap-1 cursor-pointer hover:opacity-80 capitalize"
                    onClick={() => setSelectedCategory('all')}
                  >
                    {selectedCategory}
                    <X className="w-3 h-3" />
                  </Badge>
                )}
              </div>
            </div>

            {isLoading ? (
              <LoadingSpinner text="Loading tutorials..." />
            ) : isError ? (
              <ErrorState 
                title="Failed to Load Tutorials" 
                error={error} 
                onRetry={() => refetch()} 
              />
            ) : filteredTutorials.length === 0 ? (
              <EmptyState
                title="No tutorials found"
                description="Try adjusting your search or filter criteria to find what you're looking for."
                actionLabel="Clear filters"
                onAction={() => { setSearchQuery(''); setSelectedCategory('all'); setCurrentPage(0); }}
              />
            ) : (
              <>
                <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {paginatedTutorials.map((tutorial: Tutorial) => (
                    <TutorialCard key={tutorial.id} tutorial={tutorial} />
                  ))}
                </div>

                {totalPages > 1 && (
                  <div className="flex items-center justify-center gap-4 mt-8">
                    <Button
                      variant="secondary"
                      onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                      disabled={currentPage === 0}
                    >
                      Previous
                    </Button>
                    <span className="text-sm text-gray-600 dark:text-gray-400">
                      Page {currentPage + 1} of {totalPages}
                    </span>
                    <Button
                      variant="secondary"
                      onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                      disabled={currentPage >= totalPages - 1}
                    >
                      Next
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
