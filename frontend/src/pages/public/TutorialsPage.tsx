import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, Filter, X } from 'lucide-react';
import { TutorialCard } from '../../components/public';
import { Button, SearchInput, Badge } from '../../components/ui';
import type { Category, Tutorial } from '../../types';
import { api } from '../../services/api';

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

  const { data, isLoading } = useQuery({
    queryKey: ['tutorials', currentPage],
    queryFn: async () => {
      const response = await api.get<any, any>(`/tutorials?page=${currentPage}&size=9`);
      if (Array.isArray(response)) {
        return { content: response, totalPages: 1 };
      }
      return { content: response?.content || [], totalPages: response?.totalPages || 1 };
    }
  });

  const tutorials = data?.content || [];
  const totalPages = data?.totalPages || 1;

  const filteredTutorials = useMemo(() => {
    return tutorials.filter((tutorial: Tutorial) => {
      const rawCat = typeof tutorial.category === 'object' && tutorial.category 
        ? (tutorial.category as any).name 
        : tutorial.category || 'Other';
      const categoryName = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
      
      const matchesCategory = selectedCategory === 'all' || categoryName === selectedCategory;
      const matchesSearch = (tutorial.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (tutorial.description || '').toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [tutorials, selectedCategory, searchQuery]);

  const categoryCounts = new Map<string, number>();
  tutorials.forEach((t: { category: any; }) => {
    const rawCat = typeof t.category === 'object' && t.category ? (t.category as any).name : t.category || 'Other';
    const catName = typeof rawCat === 'string' ? rawCat.trim() : String(rawCat);
    categoryCounts.set(catName, (categoryCounts.get(catName) || 0) + 1);
  });
  const dynamicCategories = Array.from(categoryCounts.entries()).sort((a, b) => b[1] - a[1]); // Sort by count descending

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
                    onClick={() => setSelectedCategory('all')}
                    className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${selectedCategory === 'all'
                        ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400'
                        : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                      }`}
                  >
                    All Categories
                  </button>
                  {dynamicCategories.map(([category, count]) => (
                    <button
                      key={category}
                      onClick={() => setSelectedCategory(category)}
                      className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between ${selectedCategory === category
                          ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400'
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
              <div className="flex justify-center items-center py-16">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
              </div>
            ) : filteredTutorials.length === 0 ? (
              <div className="text-center py-16">
                <div className="w-16 h-16 mx-auto rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mb-4">
                  <Search className="w-8 h-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
                  No tutorials found
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mb-4">
                  Try adjusting your search or filter criteria
                </p>
                <Button variant="secondary" onClick={() => { setSearchQuery(''); setSelectedCategory('all'); }}>
                  Clear filters
                </Button>
              </div>
            ) : (
              <>
                <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredTutorials.map((tutorial: Tutorial) => (
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
