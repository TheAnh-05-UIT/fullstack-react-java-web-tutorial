import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Github, Globe, ChevronRight } from 'lucide-react';
import { Badge } from '../../components/ui';
import { api } from '../../services/api';
import type { Project } from '../../types';

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProject = async () => {
      try {
        const data = await api.get<any, any>(`/projects/${id}`);
        if (data && data.id) {
          setProject(data);
        } else {
          setProject(null);
        }
      } catch (error) {
        console.error('Failed to fetch project details:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProject();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center dark:bg-gray-900">
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center dark:bg-gray-900">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Project Not Found</h2>
        <Link to="/projects" className="mt-4 text-primary-600 hover:underline">
          Back to Projects
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-12">
      {/* Sticky Breadcrumb */}
      <div className="sticky top-16 z-40 bg-white/90 dark:bg-gray-950/90 backdrop-blur-md border-b border-gray-200 dark:border-gray-800 px-4 py-3 mb-8">
        <div className="max-w-4xl mx-auto">
          <nav className="flex text-sm font-medium text-gray-500 dark:text-gray-400">
            <ol className="flex items-center space-x-2 whitespace-nowrap overflow-x-auto hide-scrollbar">
              <li>
                <Link to="/" className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors">Trang chủ</Link>
              </li>
              <li>
                <ChevronRight className="w-4 h-4 text-gray-400" />
              </li>
              <li>
                <Link to="/projects" className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors">Projects</Link>
              </li>
              <li>
                <ChevronRight className="w-4 h-4 text-gray-400" />
              </li>
              <li className="text-gray-900 dark:text-gray-100 truncate max-w-[200px] sm:max-w-[400px]">
                {project.title}
              </li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white dark:bg-gray-950 rounded-2xl overflow-hidden shadow-sm border border-gray-200 dark:border-gray-800">
          {project.thumbnail && (
            <img 
              src={project.thumbnail} 
              alt={project.title} 
              className="w-full h-64 md:h-96 object-cover"
            />
          )}
          
          <div className="p-8 md:p-12">
            <div className="flex items-center gap-4 mb-6 flex-wrap">
              <Badge variant="primary">{project.difficulty}</Badge>
              {project.status && <Badge variant="secondary">{project.status}</Badge>}
            </div>

            <h1 className="text-3xl md:text-5xl font-bold text-gray-900 dark:text-gray-100 mb-6">
              {project.title}
            </h1>

            <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-400 mb-8">
              {project.description}
            </p>

            <div className="flex flex-wrap items-center gap-4 py-6 border-y border-gray-200 dark:border-gray-800 mb-8">
              {project.githubUrl && (
                <a href={project.githubUrl} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-2 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white transition-colors bg-gray-100 dark:bg-gray-800 px-4 py-2 rounded-lg font-medium">
                  <Github className="w-5 h-5" />
                  Source Code
                </a>
              )}
              {project.demoUrl && (
                <a href={project.demoUrl} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-2 text-primary-600 hover:text-primary-700 transition-colors bg-primary-50 dark:bg-primary-900/20 px-4 py-2 rounded-lg font-medium">
                  <Globe className="w-5 h-5" />
                  Live Demo
                </a>
              )}
            </div>

            {project.techStack && project.techStack.length > 0 && (
              <div className="mb-8">
                <h3 className="text-lg font-semibold mb-3">Tech Stack</h3>
                <div className="flex flex-wrap gap-2">
                  {project.techStack.map(tech => (
                    <Badge key={tech} variant="secondary">{tech}</Badge>
                  ))}
                </div>
              </div>
            )}

            {project.content ? (
              <div className="w-full mt-8 rounded-xl overflow-hidden bg-white border border-gray-200 dark:border-gray-800 shadow-sm">
                <iframe
                  srcDoc={`
                    <!DOCTYPE html>
                    <html>
                      <head>
                        <style>
                          body::-webkit-scrollbar { display: none; }
                          body { -ms-overflow-style: none; scrollbar-width: none; overflow-y: hidden !important; height: auto !important; }
                          html { overflow-y: hidden !important; height: auto !important; }
                        </style>
                      </head>
                      <body>
                        ${project.content}
                      </body>
                    </html>
                  `}
                  title={project.title}
                  className="w-full transition-all duration-300"
                  style={{ minHeight: '400px', border: 'none' }}
                  sandbox="allow-scripts allow-same-origin allow-popups"
                  scrolling="no"
                  onLoad={(e) => {
                    const iframe = e.currentTarget;
                    try {
                      const doc = iframe.contentWindow?.document;
                      if (doc) {
                        const updateHeight = () => {
                          const body = doc.body;
                          const html = doc.documentElement;
                          const height = Math.max(
                            body.scrollHeight, body.offsetHeight,
                            html.clientHeight, html.scrollHeight, html.offsetHeight
                          );
                          iframe.style.height = `${height + 30}px`;
                        };
                        updateHeight();
                        setTimeout(updateHeight, 500);
                        setTimeout(updateHeight, 2000);
                      }
                    } catch (err) {
                      console.error('Failed to resize iframe:', err);
                    }
                  }}
                />
              </div>
            ) : (
              <div>
                <h2 className="text-2xl font-bold mb-4">Content</h2>
                <p className="text-gray-600 dark:text-gray-400">
                  This project does not have any detailed HTML content yet.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
