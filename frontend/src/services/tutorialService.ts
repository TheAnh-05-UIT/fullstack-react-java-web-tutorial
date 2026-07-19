import { api } from './api';
import type { Tutorial, PagedResponse } from '../types';

export const tutorialService = {
  getAll: async (page = 0, size = 100): Promise<Tutorial[]> => {
    const response = await api.get<unknown, PagedResponse<Tutorial> | Tutorial[]>(`/tutorials?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getMyTutorials: async (): Promise<Tutorial[]> => {
    const response = await api.get<unknown, PagedResponse<Tutorial> | Tutorial[]>('/tutorials/my-tutorials');
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getByIdOrSlug: async (idOrSlug: string | number): Promise<Tutorial> => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/tutorials/slug/${idOrSlug}` : `/tutorials/${idOrSlug}`;
    return api.get<unknown, Tutorial>(endpoint);
  },
  create: async (data: Partial<Tutorial>): Promise<Tutorial> => {
    return api.post<unknown, Tutorial>('/tutorials', data);
  },
  update: async (id: string | number, data: Partial<Tutorial>): Promise<Tutorial> => {
    return api.put<unknown, Tutorial>(`/tutorials/${id}`, data);
  },
  delete: async (id: string | number): Promise<void> => {
    return api.delete<unknown, void>(`/tutorials/${id}`);
  }
};
