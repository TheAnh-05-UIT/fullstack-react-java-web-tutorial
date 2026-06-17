import { useState, useEffect } from 'react';
import { Search, Filter } from 'lucide-react';
import { TutorialCard } from '../../components/public';
import { Button, SearchInput, Badge } from '../../components/ui';
import type { PagedResponse, Category, Tutorial } from '../../types';
import { api } from '../../services/api';

// Removed hardcoded categories array

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
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<Category | 'all'>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchTutorials();
  }, [currentPage]);

  const fetchTutorials = async () => {
    setIsLoading(true);
    try {
      const data = await api.get<PagedResponse<Tutorial>>(`/tutorials?page=${currentPage}&size=10`);
      if (data && data.content) {
        setTutorials(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } else {
        setTutorials([]);
      }
    } catch (error) {
      console.error('Failed to fetch tutorials:', error);
    } finally {
      setIsLoading(false);
    }
  };


  const filteredTutorials = tutorials.filter(tutorial => {
    const categoryName = typeof tutorial.category === 'object' && tutorial.category 
      ? (tutorial.category as any).name 
      : tutorial.category || 'Other';
    
    const matchesCategory = selectedCategory === 'all' || categoryName === selectedCategory;
    const matchesSearch = (tutorial.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (tutorial.description || '').toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const categoryCounts = new Map<string, number>();
  tutorials.forEach(t => {
    const catName = typeof t.category === 'object' && t.category ? (t.category as any).name : t.category || 'Other';
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
                      {category}
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
                    className="cursor-pointer"
                    onClick={() => setSelectedCategory('all')}
                  >
                    {selectedCategory}
                    <svg className="w-3 h-3 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </Badge>
                )}
              </div>
            </div>

            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredTutorials.map(tutorial => (
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

            {isLoading ? (
              <div className="flex justify-center items-center py-16">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
              </div>
            ) : filteredTutorials.length === 0 && (
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
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
