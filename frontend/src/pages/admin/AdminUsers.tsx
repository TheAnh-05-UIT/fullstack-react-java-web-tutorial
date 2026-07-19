import { useState } from 'react';
import { UserPlus } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, Button, SearchInput } from '../../components/ui';
import { userService } from '../../services';
import { getRoleName, type User } from '../../types';
import { UserTable } from './components/UserTable';
import { UserFormModal } from './components/UserFormModal';
import toast from 'react-hot-toast';

export function AdminUsers() {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<'all' | 'user' | 'admin'>('all');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    // Gọi qua userService thay vì api trực tiếp để tuân thủ kiến trúc phân tầng Service
    queryFn: () => userService.getAll()
  });

  const saveMutation = useMutation({
    // Gọi qua userService thay vì api trực tiếp
    mutationFn: async (data: Partial<User> & { password?: string }) => {
      if (editingUser?.id) {
        return userService.update(String(editingUser.id), data);
      }
      return userService.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsModalOpen(false);
      toast.success(editingUser ? 'User updated successfully!' : 'User created successfully!');
    },
    onError: (error) => {
      console.error('Failed to save user:', error);
      toast.error(error instanceof Error ? error.message : 'Cannot save User');
    }
  });

  const deleteMutation = useMutation({
    // Gọi qua userService thay vì api trực tiếp
    mutationFn: async (id: string | number) => {
      return userService.delete(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User deleted successfully!');
    },
    onError: (error) => {
      console.error('Failed to delete user:', error);
      toast.error('Cannot delete User');
    }
  });

  const handleOpenModal = (user?: User) => {
    setEditingUser(user || null);
    setIsModalOpen(true);
  };

  const handleDelete = (id: string | number) => {
    if (!confirm('Are you sure you want to delete this user?')) return;
    deleteMutation.mutate(id);
  };

  const filteredUsers = users.filter((user: User) => {
    const userName = user.username || user.name || '';
    const userRoleStr = getRoleName(user.role).toLowerCase();
    
    const matchesSearch = userName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = roleFilter === 'all' || userRoleStr === roleFilter.toLowerCase();
    return matchesSearch && matchesRole;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Users</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage platform users</p>
        </div>
        <Button onClick={() => handleOpenModal()}>
          <UserPlus className="w-4 h-4" />
          Add User
        </Button>
      </div>

      <Card className="p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <SearchInput
            placeholder="Search users..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1"
          />
          <div className="flex gap-2">
            {(['all', 'user', 'admin'] as const).map(role => (
              <Button
                key={role}
                variant={roleFilter === role ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setRoleFilter(role)}
              >
                {role === 'all' ? 'All' : role.charAt(0).toUpperCase() + role.slice(1)}
              </Button>
            ))}
          </div>
        </div>
      </Card>

      {isLoading ? (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500"></div>
        </div>
      ) : (
        <UserTable 
          users={filteredUsers} 
          onEdit={handleOpenModal} 
          onDelete={handleDelete} 
        />
      )}

      <UserFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        user={editingUser}
        onSubmit={async (data) => { saveMutation.mutate(data); }}
        isLoading={saveMutation.isPending}
      />
    </div>
  );
}
