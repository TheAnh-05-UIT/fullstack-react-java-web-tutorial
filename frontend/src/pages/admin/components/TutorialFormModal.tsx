import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal, Input, Button } from '../../../components/ui';
import type { Tutorial } from '../../../types';

const tutorialSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters'),
  slug: z.string().min(3, 'Slug is required'),
  description: z.string().min(10, 'Description must be at least 10 characters'),
  category: z.string().min(2, 'Category is required'),
  coverImage: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  content: z.string().optional(),
});

type TutorialFormData = z.infer<typeof tutorialSchema>;

interface TutorialFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  tutorial: Tutorial | null;
  onSubmit: (data: any) => Promise<void>;
  isLoading: boolean;
}

export function TutorialFormModal({ isOpen, onClose, tutorial, onSubmit, isLoading }: TutorialFormModalProps) {
  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<TutorialFormData>({
    resolver: zodResolver(tutorialSchema),
    defaultValues: {
      title: '',
      slug: '',
      description: '',
      category: '',
      coverImage: 'https://images.pexels.com/photos/270348/pexels-photo-270348.jpeg?auto=compress&cs=tinysrgb&w=600',
      content: '',
    }
  });

  useEffect(() => {
    if (isOpen) {
      if (tutorial) {
        reset({
          title: tutorial.title || '',
          slug: tutorial.slug || '',
          description: tutorial.description || '',
          category: typeof tutorial.category === 'object' && tutorial.category ? (tutorial.category as any).name : tutorial.category || '',
          coverImage: tutorial.coverImage || '',
          content: tutorial.content || '',
        });
      } else {
        reset({
          title: '',
          slug: '',
          description: '',
          category: 'DevOps',
          coverImage: 'https://images.pexels.com/photos/270348/pexels-photo-270348.jpeg?auto=compress&cs=tinysrgb&w=600',
          content: '',
        });
      }
    }
  }, [isOpen, tutorial, reset]);

  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const title = e.target.value;
    setValue('title', title, { shouldValidate: true });
    if (!tutorial) {
      const slug = title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)+/g, '');
      setValue('slug', slug, { shouldValidate: true });
    }
  };

  const handleFormSubmit = async (data: TutorialFormData) => {
    const submitData = {
      ...data,
      category: { name: data.category }
    };
    await onSubmit(submitData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={tutorial ? 'Edit Tutorial' : 'Add New Tutorial'}
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
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Category</label>
          <Input {...register('category')} />
          {errors.category && <p className="text-red-500 text-xs mt-1">{errors.category.message}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Cover Image URL</label>
          <Input {...register('coverImage')} />
          {errors.coverImage && <p className="text-red-500 text-xs mt-1">{errors.coverImage.message}</p>}
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
            {isLoading ? 'Saving...' : (tutorial ? 'Save Changes' : 'Create Tutorial')}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
