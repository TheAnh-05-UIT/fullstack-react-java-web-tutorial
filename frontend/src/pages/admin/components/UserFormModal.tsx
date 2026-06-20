import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal, Input, Button, Avatar } from '../../../components/ui';
import type { User } from '../../../types';

const userSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  username: z.string().optional(),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters').optional().or(z.literal('')),
  role: z.enum(['user', 'admin']),
  avatar: z.string().url('Must be a valid URL').optional().or(z.literal('')),
});

type UserFormData = z.infer<typeof userSchema>;

interface UserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: User | null;
  onSubmit: (data: any) => Promise<void>;
  isLoading: boolean;
}

export function UserFormModal({ isOpen, onClose, user, onSubmit, isLoading }: UserFormModalProps) {
  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      name: '',
      username: '',
      email: '',
      password: '',
      role: 'user',
      avatar: 'https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg?auto=compress&cs=tinysrgb&w=100',
    }
  });

  const avatarUrl = watch('avatar');

  useEffect(() => {
    if (isOpen) {
      if (user) {
        reset({
          name: (user as any).username || user.name || '',
          username: (user as any).username || user.name || '',
          email: user.email || '',
          role: typeof user.role === 'object' ? ((user.role as any).name || 'USER').toLowerCase() as any : (user.role || 'user'),
          avatar: user.avatar || '',
        });
      } else {
        reset({
          name: '',
          username: '',
          email: '',
          password: '',
          role: 'user',
          avatar: 'https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg?auto=compress&cs=tinysrgb&w=100',
        });
      }
    }
  }, [isOpen, user, reset]);

  const handleFormSubmit = async (data: UserFormData) => {
    const submitData = { ...data };
    if (user && !submitData.password) {
      delete submitData.password;
    }
    // Also sync username with name for compatibility
    submitData.username = submitData.name;
    await onSubmit(submitData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={user ? 'Edit User' : 'Add New User'}
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name</label>
            <Input {...register('name')} />
            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
            <Input type="email" {...register('email')} />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {!user && (
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Password</label>
              <Input type="password" {...register('password')} />
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
            </div>
          )}
          
          <div className={user ? "md:col-span-2" : ""}>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
            <select
              {...register('role')}
              className="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-xl px-4 py-2"
            >
              <option value="user">User</option>
              <option value="admin">Admin</option>
            </select>
            {errors.role && <p className="text-red-500 text-xs mt-1">{errors.role.message}</p>}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Avatar URL</label>
          <div className="flex gap-4 items-center">
            <Avatar src={avatarUrl} alt="Preview" size="md" />
            <div className="flex-1 space-y-2">
              <div className="flex gap-2">
                <Input
                  {...register('avatar')}
                  placeholder="https://example.com/avatar.jpg"
                />
                <div className="relative">
                  <input 
                    type="file" 
                    id="avatar-upload" 
                    className="hidden" 
                    accept="image/*"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) {
                        const previewUrl = URL.createObjectURL(file);
                        setValue('avatar', previewUrl, { shouldValidate: true });
                      }
                    }}
                  />
                  <label 
                    htmlFor="avatar-upload" 
                    className="flex items-center justify-center px-4 py-2 bg-gray-100 hover:bg-gray-200 dark:bg-gray-800 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-xl cursor-pointer transition-colors whitespace-nowrap"
                  >
                    Upload
                  </label>
                </div>
              </div>
              {errors.avatar && <p className="text-red-500 text-xs">{errors.avatar.message}</p>}
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <Button variant="secondary" onClick={onClose} type="button" disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? 'Saving...' : (user ? 'Save Changes' : 'Create User')}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
