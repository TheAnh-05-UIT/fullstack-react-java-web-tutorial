import { api } from './api';
import type { UploadResponse } from '../types';

export const uploadService = {
  uploadFile: (formData: FormData, folder?: string): Promise<UploadResponse> => {
    const endpoint = folder ? `/upload?folder=${folder}` : '/upload';
    return api.post<unknown, UploadResponse>(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  uploadImage: (formData: FormData, folder?: string): Promise<UploadResponse> => {
    const endpoint = folder ? `/upload?folder=${folder}` : '/upload';
    return api.post<unknown, UploadResponse>(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};
