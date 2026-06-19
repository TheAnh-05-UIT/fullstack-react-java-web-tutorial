import { useState, useEffect } from 'react';
import { Plus, Trash2, Edit2, Map } from 'lucide-react';
import { Card, Button, SearchInput, Modal, Input } from '../../components/ui';
import { api } from '../../services/api';
import type { Roadmap, PagedResponse } from '../../types';
import { Infinity, Cloud, Shield, Layers, Container } from 'lucide-react';

const iconComponents: Record<string, React.ReactNode> = {
  'infinity': <Infinity className="w-5 h-5" />,
  'cloud': <Cloud className="w-5 h-5" />,
  'shield': <Shield className="w-5 h-5" />,
  'layers': <Layers className="w-5 h-5" />,
  'container': <Container className="w-5 h-5" />,
};

export function AdminRoadmaps() {
  const [roadmaps, setRoadmaps] = useState<Roadmap[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoadmap, setEditingRoadmap] = useState<Roadmap | null>(null);
  const [formData, setFormData] = useState<Partial<Roadmap>>({});

  useEffect(() => {
    fetchRoadmaps();
  }, []);

  const fetchRoadmaps = async () => {
    try {
      const data = await api.get<any, any>('/roadmaps?page=0&size=100');
      setRoadmaps(Array.isArray(data) ? data : (data?.content || []));
    } catch (error) {
      console.error('Failed to fetch roadmaps:', error);
    }
  };

  const handleOpenModal = (roadmap?: Roadmap) => {
    if (roadmap) {
      setEditingRoadmap(roadmap);
      setFormData(roadmap);
    } else {
      setEditingRoadmap(null);
      setFormData({
        title: '',
        slug: '',
        description: '',
        difficulty: 'BEGINNER' as any,
        icon: 'Map',
        color: 'primary',
        steps: []
      });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingRoadmap?.id) {
        await api.put(`/roadmaps/${editingRoadmap.id}`, formData);
      } else {
        await api.post('/roadmaps', formData);
      }
      setIsModalOpen(false);
      fetchRoadmaps();
    } catch (error) {
      console.error('Failed to save roadmap:', error);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this roadmap?')) return;
    try {
      await api.delete(`/roadmaps/${id}`);
      fetchRoadmaps();
    } catch (error) {
      console.error('Failed to delete roadmap:', error);
    }
  };

  const filteredRoadmaps = roadmaps.filter(roadmap => 
    roadmap.title?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Roadmaps</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage learning roadmaps</p>
        </div>
        <Button onClick={() => handleOpenModal()}>
          <Plus className="w-4 h-4" />
          Add Roadmap
        </Button>
      </div>

      <Card className="p-4">
        <SearchInput
          placeholder="Search roadmaps..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="max-w-md"
        />
      </Card>

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
              {filteredRoadmaps.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                    No roadmaps found
                  </td>
                </tr>
              ) : (
                filteredRoadmaps.map(roadmap => (
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
                      <button onClick={() => handleOpenModal(roadmap)} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-blue-500 transition-colors">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button onClick={() => handleDelete(roadmap.id)} className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-red-500 transition-colors">
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

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingRoadmap ? 'Edit Roadmap' : 'Add New Roadmap'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Title</label>
              <Input
                required
                value={formData.title || ''}
                onChange={(e) => {
                  const title = e.target.value;
                  const slug = title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '');
                  setFormData({ ...formData, title, slug });
                }}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Slug</label>
              <Input
                required
                value={formData.slug || ''}
                onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
            <textarea
              className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
              rows={3}
              required
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Difficulty</label>
              <select
                className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
                value={formData.difficulty || 'BEGINNER'}
                onChange={(e) => setFormData({ ...formData, difficulty: e.target.value as any })}
              >
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Color Theme</label>
              <select
                className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
                value={formData.color || 'primary'}
                onChange={(e) => setFormData({ ...formData, color: e.target.value })}
              >
                <option value="primary">Primary (Blue)</option>
                <option value="secondary">Secondary (Purple)</option>
                <option value="accent">Accent (Indigo)</option>
                <option value="success">Success (Green)</option>
                <option value="warning">Warning (Orange)</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Icon Name</label>
              <Input
                value={formData.icon || ''}
                placeholder="e.g. cloud, infinity, shield"
                onChange={(e) => setFormData({ ...formData, icon: e.target.value })}
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">HTML Content</label>
            <textarea
              className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2 font-mono text-sm"
              rows={6}
              placeholder="<h1>Roadmap Details</h1><p>Content here...</p>"
              value={formData.content || ''}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)} type="button">
              Cancel
            </Button>
            <Button type="submit">
              {editingRoadmap ? 'Save Changes' : 'Create Roadmap'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
