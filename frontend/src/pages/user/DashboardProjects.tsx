import { useState, useEffect } from 'react';
import { Plus, MoreVertical, Github, ExternalLink } from 'lucide-react';
import { Card, Badge, Button } from '../../components/ui';
import { projectService } from '../../services/projectService';
import type { Project, ProjectStatus } from '../../types';

const statusColors: Record<ProjectStatus, 'success' | 'warning' | 'primary' | 'secondary'> = {
  'Completed': 'success',
  'Review': 'warning',
  'In Progress': 'primary',
  'Planned': 'secondary',
};

const columns: ProjectStatus[] = ['Planned', 'In Progress', 'Review', 'Completed'];

export function DashboardProjects() {
  const [projects, setProjects] = useState<Project[]>([]);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const data = await projectService.getAll(0, 100);
        setProjects(data);
      } catch (error) {
        console.error('Failed to fetch projects:', error);
      }
    };
    fetchProjects();
  }, []);

  const projectsByStatus = columns.reduce((acc, status) => {
    acc[status] = projects.filter(p => (p.status || 'Planned') === status);
    return acc;
  }, {} as Record<ProjectStatus, Project[]>);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Projects</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Track your learning projects</p>
        </div>
        <Button>
          <Plus className="w-4 h-4" />
          New Project
        </Button>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {columns.map(status => (
          <div key={status} className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Badge variant={statusColors[status]}>{status}</Badge>
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  {projectsByStatus[status]?.length || 0}
                </span>
              </div>
              <button className="p-1 rounded hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400">
                <Plus className="w-4 h-4" />
              </button>
            </div>
            <div className="space-y-3">
              {(projectsByStatus[status] || []).map(project => (
                <Card key={project.id} hover className="p-4">
                  <div className="aspect-video rounded-lg overflow-hidden mb-3 bg-gray-100 dark:bg-gray-800">
                    <img src={project.thumbnail} alt={project.title} className="w-full h-full object-cover" />
                  </div>
                  <h4 className="font-medium text-gray-900 dark:text-gray-100 line-clamp-1">{project.title}</h4>
                  <div className="flex flex-wrap gap-1 mt-2">
                    {project.techStack.slice(0, 3).map(tech => (
                      <span key={tech} className="text-xs px-1.5 py-0.5 rounded bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400">
                        {tech}
                      </span>
                    ))}
                  </div>
                  <div className="flex items-center gap-2 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                    <a href={project.githubUrl} className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400">
                      <Github className="w-4 h-4" />
                    </a>
                    {project.demoUrl && (
                      <a href={project.demoUrl} className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400">
                        <ExternalLink className="w-4 h-4" />
                      </a>
                    )}
                    <button className="ml-auto p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400">
                      <MoreVertical className="w-4 h-4" />
                    </button>
                  </div>
                </Card>
              ))}
              {(projectsByStatus[status]?.length || 0) === 0 && (
                <div className="h-32 rounded-xl border-2 border-dashed border-gray-200 dark:border-gray-700 flex items-center justify-center">
                  <p className="text-sm text-gray-400">No projects</p>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
