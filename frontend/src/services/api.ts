import axios, {
  type AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';

import type { AuthResponse } from '../types';
import { createSingleFlight } from './singleFlight';

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

let accessToken: string | null = null;
let authFailureHandler: (() => void) | null = null;

const sessionClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json',
  },
});

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function setAuthFailureHandler(handler: (() => void) | null): void {
  authFailureHandler = handler;
}

export async function ensureCsrfToken(): Promise<void> {
  await sessionClient.get('/csrf');
}

const runRefresh = createSingleFlight(async (): Promise<AuthResponse> => {
  try {
    await ensureCsrfToken();
    const response = await sessionClient.post('/refresh');
    const session: AuthResponse = response.data?.data ?? response.data;
    if (!session?.accessToken) {
      throw new Error('Invalid refresh response contract');
    }
    setAccessToken(session.accessToken);
    return session;
  } catch (error) {
    setAccessToken(null);
    authFailureHandler?.();
    throw error;
  }
});

export function refreshBrowserSession(): Promise<AuthResponse> {
  return runRefresh();
}

api.interceptors.request.use((config) => {
  if (accessToken && config.headers) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response: AxiosResponse) => response.data?.data ?? response.data,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined;
    const url = originalRequest?.url ?? '';
    const excluded = ['/login', '/register', '/refresh', '/logout', '/csrf']
      .some((endpoint) => url.includes(endpoint));

    if (!originalRequest || error.response?.status !== 401 || originalRequest._retry || excluded) {
      return Promise.reject(error.response?.data ?? error);
    }

    originalRequest._retry = true;
    try {
      const session = await refreshBrowserSession();
      if (originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${session.accessToken}`;
      }
      return api(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  },
);
