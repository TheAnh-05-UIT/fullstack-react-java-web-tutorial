import { useState } from 'react';
import { Plus } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, Button, SearchInput, LoadingSpinner, EmptyState, ErrorState } from '../../components/ui';
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

  const { data: tutorials = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['tutorials'],
    // Gọi qua tutorialService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
    queryFn: () => tutorialService.getAllForAdmin()
  });

  const saveMutation = useMutation({
    // Gọi qua tutorialService thay vì api trực tiếp
    mutationFn: async (data: Partial<Tutorial>) => {
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
    onError: (error: unknown) => {
      console.error('Failed to save tutorial:', error);
      const err = error as Record<string, unknown>;
      const msg = Array.isArray(err?.message) ? err.message.join(', ') : (err?.message || err?.error || 'Cannot save Tutorial');
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
    onError: (error: unknown) => {
      console.error('Failed to delete tutorial:', error);
      const err = error as Record<string, unknown>;
      const msg = Array.isArray(err?.message) ? err.message.join(', ') : (err?.message || err?.error || 'Cannot delete Tutorial');
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
        <LoadingSpinner text="Loading tutorials..." />
      ) : isError ? (
        <ErrorState 
          title="Failed to Load Tutorials" 
          error={error} 
          onRetry={() => refetch()} 
        />
      ) : filteredTutorials.length === 0 ? (
        <EmptyState
          title="No tutorials found"
          description="There are no tutorials matching your search criteria."
          actionLabel={searchQuery ? "Clear Search" : "Add New Tutorial"}
          onAction={() => searchQuery ? setSearchQuery('') : handleOpenModal()}
        />
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
        onSubmit={async (data: Partial<Tutorial>) => { saveMutation.mutate(data); }}
        isLoading={saveMutation.isPending}
      />
    </div>
  );
}
