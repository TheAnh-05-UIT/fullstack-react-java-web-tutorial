import { api, refreshBrowserSession } from './api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types';

// Service xác thực – gọi các API auth của backend
export const authService = {
  /**
   * Gọi API đăng nhập tài khoản.
   */
  login: (data: LoginRequest): Promise<AuthResponse> => {
    return api.post<unknown, AuthResponse>('/login', data);
  },

  /**
   * Gọi API đăng ký tài khoản mới.
   */
  register: (data: RegisterRequest): Promise<AuthResponse> => {
    return api.post<unknown, AuthResponse>('/register', data);
  },

  /**
   * Revoke the current refresh-token family and clear its HttpOnly cookie.
   * The in-memory access token is cleared by AuthContext.
   */
  logout: (): Promise<void> => {
    return api.post<unknown, void>('/logout');
  },

  bootstrap: (): Promise<AuthResponse> => refreshBrowserSession(),
};
