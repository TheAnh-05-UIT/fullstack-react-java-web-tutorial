import { api } from './api';
import type { Roadmap } from '../types';

export const roadmapService = {
  getAll: async (page = 0, size = 100) => {
    const response = await api.get<any, any>(`/roadmaps?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getByIdOrSlug: async (idOrSlug: string | number) => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/roadmaps/slug/${idOrSlug}` : `/roadmaps/${idOrSlug}`;
    return api.get<any, Roadmap>(endpoint);
  },
  create: async (data: Partial<Roadmap>) => {
    return api.post<any, Roadmap>('/roadmaps', data);
  },
  update: async (id: string | number, data: Partial<Roadmap>) => {
    return api.put<any, Roadmap>(`/roadmaps/${id}`, data);
  },
  delete: async (id: string | number) => {
    return api.delete(`/roadmaps/${id}`);
  }
};
