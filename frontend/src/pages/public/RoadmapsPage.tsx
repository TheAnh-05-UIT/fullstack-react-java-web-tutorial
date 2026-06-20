import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { RoadmapCard } from '../../components/public';
import { api } from '../../services/api';
import type { Roadmap } from '../../types';
import { Button } from '../../components/ui';

export function RoadmapsPage() {
  const [currentPage, setCurrentPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['roadmaps', currentPage],
    queryFn: async () => {
      const response = await api.get<any, any>(`/roadmaps?page=${currentPage}&size=10`);
      if (Array.isArray(response)) {
        return { content: response, totalPages: 1 };
      }
      return { content: response?.content || [], totalPages: response?.totalPages || 1 };
    }
  });

  const roadmaps = data?.content || [];
  const totalPages = data?.totalPages || 1;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950">
        <div className="container-app py-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100">
            Learning Roadmaps
          </h1>
          <p className="mt-2 text-lg text-gray-600 dark:text-gray-400">
            Structured paths to guide your DevOps career journey
          </p>
        </div>
      </div>

      <div className="container-app py-8">
        {isLoading ? (
          <div className="flex justify-center items-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500"></div>
          </div>
        ) : (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {roadmaps.map((roadmap: Roadmap) => (
              <RoadmapCard key={roadmap.id} roadmap={roadmap} />
            ))}
          </div>
        )}

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

        <div className="mt-8 flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 justify-center">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          More roadmaps coming soon!
        </div>
      </div>
    </div>
  );
}
