import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Github, ExternalLink, ArrowRight } from 'lucide-react';
import { Card, Badge, Button } from '../ui';
import { api } from '../../services/api';
import type { PagedResponse, Project } from '../../types';

const difficultyColors: Record<string, 'success' | 'warning' | 'error'> = {
  'Beginner': 'success',
  'Intermediate': 'warning',
  'Advanced': 'error',
};

export function FeaturedProjects() {
  const [projects, setProjects] = useState<Project[]>([]);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const data = await api.get<PagedResponse<Project>>('/projects?page=0&size=3');
        if (data && data.content) {
          setProjects(data.content);
        }
      } catch (error) {
        console.error('Failed to fetch projects:', error);
      }
    };
    fetchProjects();
  }, []);

  return (
    <section className="py-20 bg-white dark:bg-gray-950">
      <div className="container-app">
        <div className="flex items-end justify-between mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
              Hands-On Projects
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">
              Build real-world projects to strengthen your skills
            </p>
          </div>
          <Link to="/projects">
            <Button variant="ghost">
              View all
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.slice(0, 3).map(project => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      </div>
    </section>
  );
}

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const diffColor = difficultyColors[project.difficulty] || 'success';

  return (
    <Card className="overflow-hidden group flex flex-col h-full">
      <Link to={`/projects/${project.id}`} className="block relative">
        <img
          src={project.thumbnail}
          alt={project.title}
          className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
      </Link>
      
      <div className="p-5 flex-1 flex flex-col">
        <div className="flex items-center gap-2 mb-3">
          <Badge variant={diffColor}>{project.difficulty}</Badge>
        </div>
        <Link to={`/projects/${project.id}`} className="hover:text-primary-600 transition-colors">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            {project.title}
          </h3>
        </Link>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-4 flex-1">
          {project.description}
        </p>
        
        <div className="flex flex-wrap gap-2 mb-4">
          {project.techStack?.slice(0, 4).map(tech => (
            <span
              key={tech}
              className="text-xs px-2 py-1 rounded-md bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400"
            >
              {tech}
            </span>
          ))}
        </div>

        <div className="flex gap-2 mt-auto pt-4 border-t border-gray-100 dark:border-gray-800">
          <Link
            to={`/projects/${project.id}`}
            className="flex-1 flex items-center justify-center gap-2 py-2 rounded-lg bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
          >
            Details
          </Link>
          {project.githubUrl && (
            <a
              href={project.githubUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-center gap-2 p-2 rounded-lg bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
            >
              <Github className="w-5 h-5" />
            </a>
          )}
          {project.demoUrl && (
            <a
              href={project.demoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-center gap-2 p-2 rounded-lg bg-primary-600 text-white text-sm font-medium hover:bg-primary-700 transition-colors"
            >
              <ExternalLink className="w-5 h-5" />
            </a>
          )}
        </div>
      </div>
    </Card>
  );
}
