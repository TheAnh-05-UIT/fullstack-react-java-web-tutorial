import { api } from './api';
import type { User, PagedResponse } from '../types';

export const userService = {
  getAll: async (page = 0, size = 100): Promise<User[]> => {
    const response = await api.get<unknown, PagedResponse<User> | User[]>(`/users?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getProfile: async (): Promise<User> => {
    return api.get<unknown, User>('/users/profile');
  },
  updateProfile: async (data: Partial<User>): Promise<User> => {
    return api.put<unknown, User>('/users/profile', data);
  },
  getById: async (id: string | number): Promise<User> => {
    return api.get<unknown, User>(`/users/${id}`);
  },
  create: async (data: Partial<User>): Promise<User> => {
    return api.post<unknown, User>('/users', data);
  },
  update: async (id: string | number, data: Partial<User>): Promise<User> => {
    return api.put<unknown, User>(`/users/${id}`, data);
  },
  delete: async (id: string | number): Promise<void> => {
    return api.delete<unknown, void>(`/users/${id}`);
  }
};
