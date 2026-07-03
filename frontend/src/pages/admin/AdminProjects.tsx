import { useState } from 'react';
import { Plus } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, Button, SearchInput } from '../../components/ui';
import { api } from '../../services/api';
import type { Project } from '../../types';
import { ProjectTable } from './components/ProjectTable';
import { ProjectFormModal } from './components/ProjectFormModal';
import toast from 'react-hot-toast';

export function AdminProjects() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);

  const { data: projects = [], isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      const response = await api.get<any, any>('/projects?page=0&size=100');
      return Array.isArray(response) ? response : (response?.content || []);
    }
  });

  const saveMutation = useMutation({
    mutationFn: async (data: any) => {
      if (editingProject?.id) {
        return api.put(`/projects/${editingProject.id}`, data);
      }
      return api.post('/projects', data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      setIsModalOpen(false);
      toast.success(editingProject ? 'Project updated successfully!' : 'Project created successfully!');
    },
    onError: (error) => {
      console.error('Failed to save project:', error);
      toast.error(error instanceof Error ? error.message : 'Cannot save Project');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string | number) => {
      return api.delete(`/projects/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project deleted successfully!');
    },
    onError: (error) => {
      console.error('Failed to delete project:', error);
      toast.error('Cannot delete Project');
    }
  });

  const handleOpenModal = (project?: Project) => {
    setEditingProject(project || null);
    setIsModalOpen(true);
  };

  const handleDelete = (id: string | number) => {
    if (!confirm('Are you sure you want to delete this project?')) return;
    deleteMutation.mutate(id);
  };

  const filteredProjects = projects.filter((project: Project) => 
    project.title?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Projects</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage learning projects</p>
        </div>
        <Button onClick={() => handleOpenModal()}>
          <Plus className="w-4 h-4" />
          Add Project
        </Button>
      </div>

      <Card className="p-4">
        <SearchInput
          placeholder="Search projects..."
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
        <ProjectTable 
          projects={filteredProjects} 
          onEdit={handleOpenModal} 
          onDelete={handleDelete} 
        />
      )}

      <ProjectFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        project={editingProject}
        onSubmit={async (data: any) => { saveMutation.mutate(data); }}
        isLoading={saveMutation.isPending}
      />
    </div>
  );
}
