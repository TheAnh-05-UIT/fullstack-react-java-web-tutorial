import { api } from './api';
import type { SettingItem } from '../types';

export const settingsService = {
  getByKey: (key: string): Promise<SettingItem> => {
    return api.get<unknown, SettingItem>(`/settings/${key}`);
  },
  updateByKey: (key: string, value: string): Promise<SettingItem> => {
    return api.put<unknown, SettingItem>(`/settings/${key}`, { value });
  },
};
