import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Map } from 'lucide-react';
import { api } from '../../services/api';
import type { PagedResponse, Roadmap } from '../../types';

export function RoadmapDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [roadmap, setRoadmap] = useState<Roadmap | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRoadmap = async () => {
      try {
        const data = await api.get<PagedResponse<Roadmap>>('/roadmaps?page=0&size=100');
        const found = data?.content?.find(r => String(r.id) === id);
        setRoadmap(found || null);
      } catch (error) {
        console.error('Failed to fetch roadmap details:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchRoadmap();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center dark:bg-gray-900">
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  if (!roadmap) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center dark:bg-gray-900">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Roadmap Not Found</h2>
        <Link to="/roadmaps" className="mt-4 text-primary-600 hover:underline">
          Back to Roadmaps
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <Link to="/roadmaps" className="inline-flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 mb-8 transition-colors">
          <ArrowLeft className="w-4 h-4" />
          Back to Roadmaps
        </Link>

        <div className="bg-white dark:bg-gray-950 rounded-2xl overflow-hidden shadow-sm border border-gray-200 dark:border-gray-800 p-8 md:p-12">
          
          <div className="flex items-center gap-4 mb-6">
            <div className={`w-16 h-16 rounded-xl bg-gray-100 dark:bg-gray-800 flex items-center justify-center ${roadmap.color}`}>
              <Map className="w-8 h-8" />
            </div>
            <div>
              <h1 className="text-3xl md:text-5xl font-bold text-gray-900 dark:text-gray-100">
                {roadmap.title}
              </h1>
            </div>
          </div>

          <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-400 mb-8">
            {roadmap.description}
          </p>

          {roadmap.content ? (
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
                      ${roadmap.content}
                    </body>
                  </html>
                `}
                title={roadmap.title}
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
                This roadmap does not have any detailed HTML content yet.
              </p>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}
