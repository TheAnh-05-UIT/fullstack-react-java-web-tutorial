import { api } from './api';
import type { Roadmap, PagedResponse } from '../types';

export const roadmapService = {
  getAll: async (page = 0, size = 100): Promise<Roadmap[]> => {
    const response = await api.get<unknown, PagedResponse<Roadmap> | Roadmap[]>(`/roadmaps?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getAllForAdmin: async (page = 0, size = 100): Promise<Roadmap[]> => {
    const response = await api.get<unknown, PagedResponse<Roadmap> | Roadmap[]>(`/roadmaps/admin?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getPaged: async (page = 0, size = 10): Promise<PagedResponse<Roadmap> | Roadmap[]> => {
    return api.get<unknown, PagedResponse<Roadmap> | Roadmap[]>(`/roadmaps?page=${page}&size=${size}`);
  },
  getByIdOrSlug: async (idOrSlug: string | number): Promise<Roadmap> => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/roadmaps/slug/${idOrSlug}` : `/roadmaps/${idOrSlug}`;
    return api.get<unknown, Roadmap>(endpoint);
  },
  create: async (data: Partial<Roadmap>): Promise<Roadmap> => {
    return api.post<unknown, Roadmap>('/roadmaps', data);
  },
  update: async (id: string | number, data: Partial<Roadmap>): Promise<Roadmap> => {
    return api.put<unknown, Roadmap>(`/roadmaps/${id}`, data);
  },
  delete: async (id: string | number): Promise<void> => {
    return api.delete<unknown, void>(`/roadmaps/${id}`);
  }
};
