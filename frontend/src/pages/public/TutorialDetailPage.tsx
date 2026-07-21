import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Clock, Eye, Calendar, ChevronRight } from 'lucide-react';
import { Badge, Avatar, LoadingSpinner, EmptyState, ErrorState } from '../../components/ui';
import { marked } from 'marked';
import { tutorialService } from '../../services';
import type { Tutorial } from '../../types';
import { formatReadTime, formatViews } from '../../utils/format';
import { LearningProgressControls } from '../../features/learning-progress/components/LearningProgressControls';

export function TutorialDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [tutorial, setTutorial] = useState<Tutorial | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const formatAuthorName = (emailOrName?: string) => {
    if (!emailOrName) return null;
    if (emailOrName.includes('@')) {
      const namePart = emailOrName.split('@')[0];
      return namePart.charAt(0).toUpperCase() + namePart.slice(1);
    }
    return emailOrName;
  };

  const fetchTutorial = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      // Gọi qua tutorialService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
      const data = await tutorialService.getByIdOrSlug(id);
      if (data && data.id) {
        setTutorial(data);
      } else {
        setTutorial(null);
      }
    } catch (err) {
      console.error('Failed to fetch tutorial details:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchTutorial();
  }, [fetchTutorial]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <LoadingSpinner text="Loading tutorial details..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
        <ErrorState 
          title="Cannot Load Tutorial" 
          error={error} 
          onRetry={fetchTutorial} 
        />
      </div>
    );
  }

  if (!tutorial) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
        <EmptyState
          title="Tutorial Not Found"
          description="The tutorial you are trying to view does not exist or may have been removed."
          actionLabel="Back to Tutorials"
          onAction={() => window.location.assign('/tutorials')}
        />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-12">
      {/* Sticky Breadcrumb */}
      <div className="sticky top-16 z-40 bg-white/90 dark:bg-gray-950/90 backdrop-blur-md border-b border-gray-200 dark:border-gray-800 px-4 py-3 mb-8">
        <div className="max-w-4xl mx-auto">
          <nav aria-label="Breadcrumb" className="flex text-sm font-medium text-gray-500 dark:text-gray-400">
            <ol className="flex items-center space-x-2 whitespace-nowrap overflow-x-auto hide-scrollbar">
              <li>
                <Link to="/" className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors">Trang chủ</Link>
              </li>
              <li>
                <ChevronRight className="w-4 h-4 text-gray-400" />
              </li>
              <li>
                <Link to="/tutorials" className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors">Blog</Link>
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
          {(tutorial.coverImage || tutorial.thumbnail) && (
            <img 
              src={tutorial.coverImage || tutorial.thumbnail} 
              alt={tutorial.title} 
              className="w-full h-64 md:h-96 object-cover"
            />
          )}
          
          <div className="p-8 md:p-12">
            <div className="flex items-center gap-4 mb-6">
              <Badge variant="primary">
                {typeof tutorial.category === 'object' && tutorial.category && 'name' in tutorial.category
                  ? String((tutorial.category as Record<string, unknown>).name || 'DevOps')
                  : typeof tutorial.category === 'string' ? tutorial.category : 'DevOps'}
              </Badge>
              <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                <div className="flex items-center gap-1">
                  <Clock className="w-4 h-4" />
                  {formatReadTime(tutorial.readTime, tutorial.content, tutorial.description)} min read
                </div>
                <div className="flex items-center gap-1">
                  <Eye className="w-4 h-4" />
                  {formatViews(tutorial.views, tutorial.viewCount)} views
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

            <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 py-6 border-y border-gray-200 dark:border-gray-800 mb-8">
              <div className="flex items-center gap-4">
                <Avatar src={tutorial.author?.avatar} alt={tutorial.author?.name} size="md" />
                <div>
                  <p className="font-medium text-gray-900 dark:text-gray-100">
                    {tutorial.authorName || formatAuthorName(tutorial.createBy) || tutorial.author?.name || 'Unknown Author'}
                  </p>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    {tutorial.author?.role || 'Contributor'}
                  </p>
                </div>
              </div>
              {(() => {
                const tutorialProgressKey = tutorial.slug?.trim() || '';
                if (!tutorialProgressKey) return null;
                
                return (
                  <div className="w-full md:w-auto md:min-w-[320px]">
                    <LearningProgressControls
                      contentType="TUTORIAL"
                      contentKey={tutorialProgressKey}
                    />
                  </div>
                );
              })()}
            </div>

            <div className="prose dark:prose-invert max-w-none">
              <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-400">
                {tutorial.description}
              </p>
              <h2 className="text-2xl font-bold mt-8 mb-4">Content</h2>
              {tutorial.content ? (
                <div className="w-full mt-8 rounded-xl overflow-hidden bg-white border border-gray-200 dark:border-gray-800 shadow-sm">
                  <iframe
                    srcDoc={
                      (() => {
                        const trimmed = tutorial.content.trim();
                        const isFullHtml = trimmed.toLowerCase().includes('<!doctype html>') || 
                                           trimmed.toLowerCase().includes('<html') ||
                                           (trimmed.startsWith('<') && trimmed.includes('<style>'));
                                           
                        if (isFullHtml) {
                          return tutorial.content;
                        }

                        return `
                          <!DOCTYPE html>
                          <html>
                            <head>
                              <style>
                                body { 
                                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                                  line-height: 1.6;
                                  color: #374151;
                                  margin: 0;
                                  padding: 24px;
                                }
                                html { overflow-y: hidden !important; height: auto !important; }
                                pre { background: #f3f4f6; padding: 1rem; border-radius: 0.5rem; overflow-x: auto; }
                                code { background: #f3f4f6; padding: 0.2rem 0.4rem; border-radius: 0.25rem; font-family: ui-monospace, monospace; }
                                a { color: #4f46e5; text-decoration: none; }
                                a:hover { text-decoration: underline; }
                                img { max-width: 100%; height: auto; border-radius: 0.5rem; }
                                blockquote { border-left: 4px solid #e5e7eb; padding-left: 1rem; color: #6b7280; font-style: italic; }
                              </style>
                            </head>
                            <body>
                              ${marked.parse(tutorial.content)}
                            </body>
                          </html>
                        `;
                      })()
                    }
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
