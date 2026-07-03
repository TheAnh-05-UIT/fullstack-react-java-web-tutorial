import { Trash2, Edit2, Map, Infinity, Cloud, Shield, Layers, Container } from 'lucide-react';
import { Card } from '../../../components/ui';
import type { Roadmap } from '../../../types';

interface RoadmapTableProps {
  roadmaps: Roadmap[];
  onEdit: (roadmap: Roadmap) => void;
  onDelete: (id: string | number) => void;
}

const iconComponents: Record<string, React.ReactNode> = {
  'infinity': <Infinity className="w-5 h-5" />,
  'cloud': <Cloud className="w-5 h-5" />,
  'shield': <Shield className="w-5 h-5" />,
  'layers': <Layers className="w-5 h-5" />,
  'container': <Container className="w-5 h-5" />,
};

export function RoadmapTable({ roadmaps, onEdit, onDelete }: RoadmapTableProps) {
  return (
    <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
            <tr>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Title</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Color</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Steps</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
            {roadmaps.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                  No roadmaps found
                </td>
              </tr>
            ) : (
              roadmaps.map(roadmap => (
                <tr key={roadmap.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded flex items-center justify-center shrink-0 bg-${roadmap.color || 'primary'}-100 text-${roadmap.color || 'primary'}-700 dark:bg-${roadmap.color || 'primary'}-900/30 dark:text-${roadmap.color || 'primary'}-400`}>
                        {iconComponents[roadmap.icon] || <Map className="w-5 h-5" />}
                      </div>
                      <p className="font-medium text-gray-900 dark:text-gray-100">{roadmap.title}</p>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full bg-${roadmap.color || 'primary'}-100 text-${roadmap.color || 'primary'}-700 dark:bg-${roadmap.color || 'primary'}-900/30 dark:text-${roadmap.color || 'primary'}-400`}>
                      {roadmap.color || 'primary'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                    {roadmap.steps?.length || 0} steps
                  </td>
                  <td className="px-6 py-4 text-right space-x-2">
                    <button onClick={() => onEdit(roadmap)} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-blue-500 transition-colors">
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button onClick={() => onDelete(roadmap.id)} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-red-500 transition-colors">
                      <Trash2 className="w-4 h-4" />
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
