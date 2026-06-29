import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor Request: Tự động đính kèm Access Token vào header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor Response: Tự động mồi Refresh Token khi bị lỗi 401
api.interceptors.response.use(
  (response) => {
    // Thêm null guard: trả về response.data.data (dữ liệu bên trong FormatRestResponse).
    // Nếu endpoint nào đó không wrap (file download, health check...), fallback về
    // response.data để tránh trả về undefined silently.
    return response.data?.data ?? response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Nếu lỗi 401 và chưa từng thử refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refresh_token');
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Gọi API cấp lại thẻ mới
        const res = await axios.post(`${API_BASE_URL}/refresh`, { refreshToken });
        
        // Lấy Token mới từ chuẩn FormatRestResponse ({ data: { accessToken, refreshToken } })
        const newAccessToken = res.data.data.accessToken;
        const newRefreshToken = res.data.data.refreshToken;

        // Lưu lại vào LocalStorage
        localStorage.setItem('access_token', newAccessToken);
        localStorage.setItem('refresh_token', newRefreshToken);

        // Gắn thẻ mới vào yêu cầu cũ và gọi lại
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        // Bóc thêm tầng .data bên trong FormatRestResponse (nhất quán với interceptor success)
        return axios(originalRequest).then(res => res.data?.data ?? res.data);
      } catch (refreshError) {
        // Nếu Refresh Token cũng hết hạn -> Xóa hết thẻ, bắt đăng nhập lại
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login'; // Force redirect
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error.response?.data || error);
  }
);
