import { useState, useEffect } from 'react';
import { Search } from 'lucide-react';
import { ProjectCard } from '../../components/public';
import { Button, SearchInput } from '../../components/ui';
import { api } from '../../services/api';
import type { PagedResponse, Project } from '../../types';

const difficulties = ['All', 'Beginner', 'Intermediate', 'Advanced'] as const;
type DifficultyFilter = typeof difficulties[number];

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [difficulty, setDifficulty] = useState<DifficultyFilter>('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchProjects = async () => {
      setIsLoading(true);
      try {
        const url = `/projects?page=${currentPage}&size=10${difficulty !== 'All' ? `&difficulty=${difficulty}` : ''}`;
        const data = await api.get<any, any>(url);
        if (Array.isArray(data)) {
          setProjects(data);
          setTotalPages(1);
          setTotalElements(data.length);
        } else if (data && data.content) {
          setProjects(data.content);
          setTotalPages(data.totalPages);
          setTotalElements(data.totalElements);
        } else {
          setProjects([]);
        }
      } catch (error) {
        console.error('Failed to fetch projects:', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchProjects();
  }, [currentPage, difficulty]);

  const filteredProjects = projects.filter(project => {
    const matchesDifficulty = difficulty === 'All' || project.difficulty === difficulty;
    const matchesSearch = (project.title?.toLowerCase().includes(searchQuery.toLowerCase()) || '') ||
      (project.description?.toLowerCase().includes(searchQuery.toLowerCase()) || '');
    return matchesDifficulty && matchesSearch;
  });

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-950">
        <div className="container-app py-8">
          <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100">
            Projects
          </h1>
          <p className="mt-2 text-lg text-gray-600 dark:text-gray-400">
            Hands-on projects to build your DevOps portfolio
          </p>
        </div>
      </div>

      <div className="container-app py-8">
        <div className="flex flex-col sm:flex-row gap-4 mb-8">
          <SearchInput
            placeholder="Search projects..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1 max-w-md"
          />
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600 dark:text-gray-400">Difficulty:</span>
            {difficulties.map(d => (
              <button
                key={d}
                onClick={() => setDifficulty(d)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${difficulty === d
                    ? 'bg-primary-600 text-white'
                    : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-400 border border-gray-200 dark:border-gray-800 hover:border-primary-300'
                  }`}
              >
                {d}
              </button>
            ))}
          </div>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map(project => (
            <ProjectCard key={project.id} project={project} />
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
        ) : filteredProjects.length === 0 && (
          <div className="text-center py-16">
            <div className="w-16 h-16 mx-auto rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mb-4">
              <Search className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
              No projects found
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-4">
              Try selecting a different difficulty level
            </p>
            <Button variant="secondary" onClick={() => { setDifficulty('All'); setSearchQuery(''); }}>
              Show all projects
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
