import { api } from './api';
import type { User } from '../types';

export const userService = {
  getAll: async (page = 0, size = 100) => {
    const response = await api.get<any, any>(`/users?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getById: async (id: string) => {
    return api.get<any, User>(`/users/${id}`);
  },
  create: async (data: Partial<User>) => {
    return api.post<any, User>('/users', data);
  },
  update: async (id: string, data: Partial<User>) => {
    return api.put<any, User>(`/users/${id}`, data);
  },
  delete: async (id: string) => {
    return api.delete(`/users/${id}`);
  }
};
