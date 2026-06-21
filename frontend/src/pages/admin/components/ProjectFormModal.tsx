import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal, Input, Button } from '../../../components/ui';
import type { Project } from '../../../types';

const projectSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters'),
  slug: z.string().min(3, 'Slug is required'),
  description: z.string().min(10, 'Description must be at least 10 characters'),
  difficulty: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']),
  status: z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']),
  thumbnail: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  techStack: z.string().optional(),
  content: z.string().optional(),
});

type ProjectFormData = z.infer<typeof projectSchema>;

interface ProjectFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  project: Project | null;
  onSubmit: (data: any) => Promise<void>;
  isLoading: boolean;
}

export function ProjectFormModal({ isOpen, onClose, project, onSubmit, isLoading }: ProjectFormModalProps) {
  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<ProjectFormData>({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      title: '',
      slug: '',
      description: '',
      difficulty: 'BEGINNER',
      status: 'DRAFT',
      thumbnail: 'https://images.pexels.com/photos/270348/pexels-photo-270348.jpeg?auto=compress&cs=tinysrgb&w=600',
      techStack: '',
      content: '',
    }
  });

  useEffect(() => {
    if (isOpen) {
      if (project) {
        reset({
          title: project.title || '',
          slug: project.slug || '',
          description: project.description || '',
          difficulty: project.difficulty as any || 'BEGINNER',
          status: project.status as any || 'DRAFT',
          thumbnail: project.thumbnail || '',
          techStack: Array.isArray(project.techStack) ? project.techStack.join(', ') : project.techStack || '',
          content: project.content || '',
        });
      } else {
        reset({
          title: '',
          slug: '',
          description: '',
          difficulty: 'BEGINNER',
          status: 'DRAFT',
          thumbnail: 'https://images.pexels.com/photos/270348/pexels-photo-270348.jpeg?auto=compress&cs=tinysrgb&w=600',
          techStack: '',
          content: '',
        });
      }
    }
  }, [isOpen, project, reset]);

  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const title = e.target.value;
    setValue('title', title, { shouldValidate: true });
    if (!project) {
      const slug = title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '');
      setValue('slug', slug, { shouldValidate: true });
    }
  };

  const handleFormSubmit = async (data: ProjectFormData) => {
    const submitData = {
      ...data,
      techStack: typeof data.techStack === 'string' 
        ? data.techStack.split(',').map((s: string) => s.trim()).filter(Boolean) 
        : data.techStack
    };
    await onSubmit(submitData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={project ? 'Edit Project' : 'Add New Project'}
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Title</label>
            <Input {...register('title')} onChange={handleTitleChange} />
            {errors.title && <p className="text-red-500 text-xs mt-1">{errors.title.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Slug</label>
            <Input {...register('slug')} />
            {errors.slug && <p className="text-red-500 text-xs mt-1">{errors.slug.message}</p>}
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
          <textarea
            {...register('description')}
            className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
            rows={3}
          />
          {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Difficulty</label>
          <select
            {...register('difficulty')}
            className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
          >
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
          </select>
          {errors.difficulty && <p className="text-red-500 text-xs mt-1">{errors.difficulty.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Status</label>
          <select
            {...register('status')}
            className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
          >
            <option value="DRAFT">Draft</option>
            <option value="PUBLISHED">Published</option>
            <option value="ARCHIVED">Archived</option>
          </select>
          {errors.status && <p className="text-red-500 text-xs mt-1">{errors.status.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Tech Stack (comma separated)</label>
          <Input {...register('techStack')} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">HTML Content</label>
          <textarea
            {...register('content')}
            className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2 font-mono text-sm"
            rows={6}
            placeholder="<h1>Title</h1><p>Content here...</p>"
          />
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <Button variant="secondary" onClick={onClose} type="button" disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? 'Saving...' : (project ? 'Save Changes' : 'Create Project')}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
