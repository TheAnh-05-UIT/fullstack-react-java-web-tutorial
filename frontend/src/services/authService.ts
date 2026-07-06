import { api } from './api';

// Service xác thực – gọi các API auth của backend
export const authService = {
  /**
   * Gọi API logout để revoke Refresh Token trong DB.
   * Access Token vẫn còn hiệu lực cho đến khi hết hạn 15 phút (JWT stateless),
   * nhưng sau khi revoke refresh token, user không thể gia hạn thêm.
   * Frontend cần xóa token khỏi localStorage sau khi gọi hàm này.
   */
  logout: (): Promise<void> => {
    return api.post('/logout');
  },
};
