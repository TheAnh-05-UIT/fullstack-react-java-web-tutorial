import { useState } from 'react';
import { Plus } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, Button, SearchInput } from '../../components/ui';
import { tutorialService } from '../../services';
import type { Tutorial } from '../../types';
import { TutorialTable } from './components/TutorialTable';
import { TutorialFormModal } from './components/TutorialFormModal';
import toast from 'react-hot-toast';

export function AdminTutorials() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTutorial, setEditingTutorial] = useState<Tutorial | null>(null);

  const { data: tutorials = [], isLoading } = useQuery({
    queryKey: ['tutorials'],
    // Gọi qua tutorialService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
    queryFn: () => tutorialService.getAll()
  });

  const saveMutation = useMutation({
    // Gọi qua tutorialService thay vì api trực tiếp
    mutationFn: async (data: any) => {
      if (editingTutorial?.id) {
        return tutorialService.update(editingTutorial.id, data);
      }
      return tutorialService.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tutorials'] });
      setIsModalOpen(false);
      toast.success(editingTutorial ? 'Tutorial updated successfully!' : 'Tutorial created successfully!');
    },
    onError: (error: any) => {
      console.error('Failed to save tutorial:', error);
      const msg = Array.isArray(error?.message) ? error.message.join(', ') : (error?.message || error?.error || 'Cannot save Tutorial');
      toast.error(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
  });

  const deleteMutation = useMutation({
    // Gọi qua tutorialService thay vì api trực tiếp
    mutationFn: async (id: string | number) => {
      return tutorialService.delete(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tutorials'] });
      toast.success('Tutorial deleted successfully!');
    },
    onError: (error: any) => {
      console.error('Failed to delete tutorial:', error);
      const msg = Array.isArray(error?.message) ? error.message.join(', ') : (error?.message || error?.error || 'Cannot delete Tutorial');
      toast.error(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
  });

  const handleOpenModal = (tutorial?: Tutorial) => {
    setEditingTutorial(tutorial || null);
    setIsModalOpen(true);
  };

  const handleDelete = (id: string | number) => {
    if (!confirm('Are you sure you want to delete this tutorial?')) return;
    deleteMutation.mutate(id);
  };

  const filteredTutorials = tutorials.filter((tutorial: Tutorial) => 
    tutorial.title?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Tutorials</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage learning tutorials</p>
        </div>
        <Button onClick={() => handleOpenModal()}>
          <Plus className="w-4 h-4" />
          Add Tutorial
        </Button>
      </div>

      <Card className="p-4">
        <SearchInput
          placeholder="Search tutorials..."
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
        <TutorialTable 
          tutorials={filteredTutorials} 
          onEdit={handleOpenModal} 
          onDelete={handleDelete} 
        />
      )}

      <TutorialFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tutorial={editingTutorial}
        onSubmit={async (data: any) => { saveMutation.mutate(data); }}
        isLoading={saveMutation.isPending}
      />
    </div>
  );
}
