import { api } from './api';
import type { Tutorial } from '../types';

export const tutorialService = {
  getAll: async (page = 0, size = 100) => {
    const response = await api.get<any, any>(`/tutorials?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getByIdOrSlug: async (idOrSlug: string) => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/tutorials/slug/${idOrSlug}` : `/tutorials/${idOrSlug}`;
    return api.get<any, Tutorial>(endpoint);
  },
  create: async (data: Partial<Tutorial>) => {
    return api.post<any, Tutorial>('/tutorials', data);
  },
  update: async (id: string, data: Partial<Tutorial>) => {
    return api.put<any, Tutorial>(`/tutorials/${id}`, data);
  },
  delete: async (id: string) => {
    return api.delete(`/tutorials/${id}`);
  }
};
