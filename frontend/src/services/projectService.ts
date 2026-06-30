import { api } from './api';
import type { Project } from '../types';

export const projectService = {
  getAll: async (page = 0, size = 100) => {
    const response = await api.get<any, any>(`/projects?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getByIdOrSlug: async (idOrSlug: string) => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/projects/slug/${idOrSlug}` : `/projects/${idOrSlug}`;
    return api.get<any, Project>(endpoint);
  },
  create: async (data: Partial<Project>) => {
    return api.post<any, Project>('/projects', data);
  },
  update: async (id: string, data: Partial<Project>) => {
    return api.put<any, Project>(`/projects/${id}`, data);
  },
  delete: async (id: string) => {
    return api.delete(`/projects/${id}`);
  }
};
