import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Clock, Eye, Calendar, ArrowLeft, ChevronRight } from 'lucide-react';
import { Badge, Avatar } from '../../components/ui';
import { api } from '../../services/api';
import type { Tutorial } from '../../types';

export function TutorialDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [tutorial, setTutorial] = useState<Tutorial | null>(null);
  const [loading, setLoading] = useState(true);

  const formatAuthorName = (emailOrName?: string) => {
    if (!emailOrName) return null;
    if (emailOrName.includes('@')) {
      const namePart = emailOrName.split('@')[0];
      return namePart.charAt(0).toUpperCase() + namePart.slice(1);
    }
    return emailOrName;
  };

  useEffect(() => {
    const fetchTutorial = async () => {
      try {
        const data = await api.get<any, any>(`/tutorials/${id}`);
        if (data && data.id) {
          setTutorial(data);
        } else {
          setTutorial(null);
        }
      } catch (error) {
        console.error('Failed to fetch tutorial details:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchTutorial();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center dark:bg-gray-900">
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  if (!tutorial) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center dark:bg-gray-900">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Tutorial Not Found</h2>
        <Link to="/tutorials" className="mt-4 text-primary-600 hover:underline">
          Back to Tutorials
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
                <Link to="/tutorials" className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors">Tutorial</Link>
              </li>
              <li>
                <ChevronRight className="w-4 h-4 text-gray-400" />
              </li>
              <li className="text-gray-900 dark:text-gray-100 truncate max-w-[200px] sm:max-w-[400px]">
                {tutorial.title}
              </li>
            </ol>
          </nav>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white dark:bg-gray-950 rounded-2xl overflow-hidden shadow-sm border border-gray-200 dark:border-gray-800">
          {tutorial.coverImage && (
            <img 
              src={tutorial.coverImage} 
              alt={tutorial.title} 
              className="w-full h-64 md:h-96 object-cover"
            />
          )}
          
          <div className="p-8 md:p-12">
            <div className="flex items-center gap-4 mb-6">
              <Badge variant="primary">{typeof tutorial.category === 'object' && tutorial.category ? (tutorial.category as any).name : tutorial.category || 'DevOps'}</Badge>
              <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                <div className="flex items-center gap-1">
                  <Clock className="w-4 h-4" />
                  {tutorial.readTime} min read
                </div>
                <div className="flex items-center gap-1">
                  <Eye className="w-4 h-4" />
                  {(((tutorial.viewCount ?? tutorial.views) || 0) / 1000).toFixed(1)}k views
                </div>
                <div className="flex items-center gap-1">
                  <Calendar className="w-4 h-4" />
                  {new Date(tutorial.createdAt || tutorial.publishDate || Date.now()).toLocaleDateString()}
                </div>
              </div>
            </div>

            <h1 className="text-3xl md:text-5xl font-bold text-gray-900 dark:text-gray-100 mb-6">
              {tutorial.title}
            </h1>

            <div className="flex items-center gap-4 py-6 border-y border-gray-200 dark:border-gray-800 mb-8">
              <Avatar src={tutorial.author?.avatar} alt={tutorial.author?.name} size="md" />
              <div>
                <p className="font-medium text-gray-900 dark:text-gray-100">
                  {formatAuthorName(tutorial.createBy) || tutorial.author?.name || 'Unknown Author'}
                </p>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {tutorial.author?.role || 'Contributor'}
                </p>
              </div>
            </div>

            <div className="prose dark:prose-invert max-w-none">
              <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-400">
                {tutorial.description}
              </p>
              <h2 className="text-2xl font-bold mt-8 mb-4">Content</h2>
              {tutorial.content ? (
                <div className="w-full mt-8 rounded-xl overflow-hidden bg-white border border-gray-200 dark:border-gray-800 shadow-sm">
                  <iframe
                    srcDoc={`
                      <!DOCTYPE html>
                      <html>
                        <head>
                          <style>
                            /* Hide scrollbar for Chrome, Safari and Opera */
                            body::-webkit-scrollbar { display: none; }
                            /* Hide scrollbar for IE, Edge and Firefox */
                            body { -ms-overflow-style: none; scrollbar-width: none; overflow-y: hidden !important; height: auto !important; }
                            html { overflow-y: hidden !important; height: auto !important; }
                          </style>
                        </head>
                        <body>
                          ${tutorial.content}
                        </body>
                      </html>
                    `}
                    title={tutorial.title}
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
                <p className="text-gray-600 dark:text-gray-400">
                  This tutorial does not have any detailed content yet.
                </p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
