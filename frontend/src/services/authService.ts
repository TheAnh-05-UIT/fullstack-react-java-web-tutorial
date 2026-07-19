import { api } from './api';
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
   * Gọi API logout để revoke Refresh Token trong DB.
   * Access Token vẫn còn hiệu lực cho đến khi hết hạn 15 phút (JWT stateless),
   * nhưng sau khi revoke refresh token, user không thể gia hạn thêm.
   * Frontend cần xóa token khỏi localStorage sau khi gọi hàm này.
   */
  logout: (): Promise<void> => {
    return api.post<unknown, void>('/logout');
  },
};
