import { api } from './api';
import type { Project, PagedResponse } from '../types';

export const projectService = {
  getAll: async (page = 0, size = 100): Promise<Project[]> => {
    const response = await api.get<unknown, PagedResponse<Project> | Project[]>(`/projects?page=${page}&size=${size}`);
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getPaged: async (page = 0, size = 10, difficulty?: string): Promise<PagedResponse<Project> | Project[]> => {
    const url = `/projects?page=${page}&size=${size}${difficulty ? `&difficulty=${difficulty}` : ''}`;
    return api.get<unknown, PagedResponse<Project> | Project[]>(url);
  },
  getMyProjects: async (): Promise<Project[]> => {
    const response = await api.get<unknown, PagedResponse<Project> | Project[]>('/projects/my-projects');
    return Array.isArray(response) ? response : (response?.content || []);
  },
  getByIdOrSlug: async (idOrSlug: string | number): Promise<Project> => {
    const endpoint = isNaN(Number(idOrSlug)) ? `/projects/slug/${idOrSlug}` : `/projects/${idOrSlug}`;
    return api.get<unknown, Project>(endpoint);
  },
  create: async (data: Partial<Project>): Promise<Project> => {
    return api.post<unknown, Project>('/projects', data);
  },
  update: async (id: string | number, data: Partial<Project>): Promise<Project> => {
    return api.put<unknown, Project>(`/projects/${id}`, data);
  },
  delete: async (id: string | number): Promise<void> => {
    return api.delete<unknown, void>(`/projects/${id}`);
  }
};
