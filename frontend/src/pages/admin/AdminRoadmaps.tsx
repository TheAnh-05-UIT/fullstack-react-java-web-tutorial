import { useState } from 'react';
import { Plus } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, Button, SearchInput } from '../../components/ui';
import { roadmapService } from '../../services';
import type { Roadmap } from '../../types';
import { RoadmapTable } from './components/RoadmapTable';
import { RoadmapFormModal } from './components/RoadmapFormModal';
import toast from 'react-hot-toast';

export function AdminRoadmaps() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoadmap, setEditingRoadmap] = useState<Roadmap | null>(null);

  const { data: roadmaps = [], isLoading } = useQuery({
    queryKey: ['roadmaps'],
    // Gọi qua roadmapService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
    queryFn: () => roadmapService.getAllForAdmin()
  });

  const saveMutation = useMutation({
    // Gọi qua roadmapService thay vì api trực tiếp
    mutationFn: async (data: Partial<Roadmap>) => {
      if (editingRoadmap?.id) {
        return roadmapService.update(editingRoadmap.id, data);
      }
      return roadmapService.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roadmaps'] });
      setIsModalOpen(false);
      toast.success(editingRoadmap ? 'Roadmap updated successfully!' : 'Roadmap created successfully!');
    },
    onError: (error) => {
      console.error('Failed to save roadmap:', error);
      toast.error(error instanceof Error ? error.message : 'Cannot save Roadmap');
    }
  });

  const deleteMutation = useMutation({
    // Gọi qua roadmapService thay vì api trực tiếp
    mutationFn: async (id: string | number) => {
      return roadmapService.delete(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roadmaps'] });
      toast.success('Roadmap deleted successfully!');
    },
    onError: (error) => {
      console.error('Failed to delete roadmap:', error);
      toast.error('Cannot delete Roadmap');
    }
  });

  const handleOpenModal = (roadmap?: Roadmap) => {
    setEditingRoadmap(roadmap || null);
    setIsModalOpen(true);
  };

  const handleDelete = (id: string | number) => {
    if (!confirm('Are you sure you want to delete this roadmap?')) return;
    deleteMutation.mutate(id);
  };

  const filteredRoadmaps = roadmaps.filter((roadmap: Roadmap) => 
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

      {isLoading ? (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500"></div>
        </div>
      ) : (
        <RoadmapTable 
          roadmaps={filteredRoadmaps} 
          onEdit={handleOpenModal} 
          onDelete={handleDelete} 
        />
      )}

      <RoadmapFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        roadmap={editingRoadmap}
        onSubmit={async (data: Partial<Roadmap>) => { saveMutation.mutate(data); }}
        isLoading={saveMutation.isPending}
      />
    </div>
  );
}
