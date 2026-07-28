import { Trash2, Edit2, BookOpen } from 'lucide-react';
import { Card, Badge } from '../../../components/ui';
import type { Tutorial } from '../../../types';

interface TutorialTableProps {
  tutorials: Tutorial[];
  onEdit: (tutorial: Tutorial) => void;
  onDelete: (id: string | number) => void;
}

export function TutorialTable({ tutorials, onEdit, onDelete }: TutorialTableProps) {
  return (
    <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
            <tr>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Title</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Category</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Views</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
            {tutorials.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                  No tutorials found
                </td>
              </tr>
            ) : (
              tutorials.map(tutorial => (
                <tr key={tutorial.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded bg-gray-100 dark:bg-gray-800 flex items-center justify-center shrink-0 overflow-hidden">
                        {tutorial.coverImage ? (
                          <img src={tutorial.coverImage} alt={tutorial.title} className="w-full h-full object-cover" />
                        ) : (
                          <BookOpen className="w-5 h-5 text-gray-400" />
                        )}
                      </div>
                      <p className="font-medium text-gray-900 dark:text-gray-100">{tutorial.title}</p>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <Badge variant="primary">
                      {typeof tutorial.category === 'object' && tutorial.category && 'name' in tutorial.category
                        ? String((tutorial.category as Record<string, unknown>).name || 'DevOps')
                        : typeof tutorial.category === 'string' ? tutorial.category : 'DevOps'}
                    </Badge>
                  </td>
                  <td className="px-6 py-4">
                    <Badge variant={tutorial.status === 'PUBLISHED' ? 'success' : 'secondary'}>
                      {tutorial.status || 'DRAFT'}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {Number(tutorial.viewCount ?? tutorial.views ?? 0).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 text-right space-x-2">
                    <button type="button" onClick={() => onEdit(tutorial)} aria-label={`Edit tutorial ${tutorial.title}`} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-blue-500 transition-colors">
                      <Edit2 className="w-4 h-4" aria-hidden="true" />
                    </button>
                    <button type="button" onClick={() => onDelete(tutorial.id)} aria-label={`Delete tutorial ${tutorial.title}`} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-red-500 transition-colors">
                      <Trash2 className="w-4 h-4" aria-hidden="true" />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
