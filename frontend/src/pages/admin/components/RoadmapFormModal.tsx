import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal, Input, Button } from '../../../components/ui';
import type { Roadmap } from '../../../types';

const roadmapSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters'),
  slug: z.string().min(3, 'Slug is required'),
  description: z.string().min(10, 'Description must be at least 10 characters'),
  difficulty: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']),
  color: z.string().min(2, 'Color is required'),
  icon: z.string().min(2, 'Icon name is required'),
  content: z.string().optional(),
});

type RoadmapFormData = z.infer<typeof roadmapSchema>;

interface RoadmapFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  roadmap: Roadmap | null;
  onSubmit: (data: any) => Promise<void>;
  isLoading: boolean;
}

export function RoadmapFormModal({ isOpen, onClose, roadmap, onSubmit, isLoading }: RoadmapFormModalProps) {
  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<RoadmapFormData>({
    resolver: zodResolver(roadmapSchema),
    defaultValues: {
      title: '',
      slug: '',
      description: '',
      difficulty: 'BEGINNER',
      color: 'primary',
      icon: 'Map',
      content: '',
    }
  });

  useEffect(() => {
    if (isOpen) {
      if (roadmap) {
        reset({
          title: roadmap.title || '',
          slug: roadmap.slug || '',
          description: roadmap.description || '',
          difficulty: roadmap.difficulty as any || 'BEGINNER',
          color: roadmap.color || 'primary',
          icon: roadmap.icon || 'Map',
          content: roadmap.content || '',
        });
      } else {
        reset({
          title: '',
          slug: '',
          description: '',
          difficulty: 'BEGINNER',
          color: 'primary',
          icon: 'Map',
          content: '',
        });
      }
    }
  }, [isOpen, roadmap, reset]);

  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const title = e.target.value;
    setValue('title', title, { shouldValidate: true });
    if (!roadmap) {
      const slug = title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '');
      setValue('slug', slug, { shouldValidate: true });
    }
  };

  const handleFormSubmit = async (data: RoadmapFormData) => {
    // Keep the existing steps if editing, or set to empty array if new
    const submitData = {
      ...data,
      steps: roadmap?.steps || []
    };
    await onSubmit(submitData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={roadmap ? 'Edit Roadmap' : 'Add New Roadmap'}
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
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Color Theme</label>
            <select
              {...register('color')}
              className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
            >
              <option value="primary">Primary (Blue)</option>
              <option value="secondary">Secondary (Purple)</option>
              <option value="accent">Accent (Indigo)</option>
              <option value="success">Success (Green)</option>
              <option value="warning">Warning (Orange)</option>
            </select>
            {errors.color && <p className="text-red-500 text-xs mt-1">{errors.color.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Icon Name</label>
            <Input {...register('icon')} placeholder="e.g. cloud, infinity, shield" />
            {errors.icon && <p className="text-red-500 text-xs mt-1">{errors.icon.message}</p>}
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">HTML Content</label>
          <textarea
            {...register('content')}
            className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2 font-mono text-sm"
            rows={6}
            placeholder="<h1>Roadmap Details</h1><p>Content here...</p>"
          />
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <Button variant="secondary" onClick={onClose} type="button" disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? 'Saving...' : (roadmap ? 'Save Changes' : 'Create Roadmap')}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
