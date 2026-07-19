import axios, { type InternalAxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios';

/**
 * Interface mở rộng cho AxiosRequestConfig, thêm cờ _retry với kiểu dữ liệu rõ ràng (boolean)
 * để ngăn chặn lặp vô tận khi xử lý lỗi 401 Access Token hết hạn.
 */
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

/**
 * Cấu trúc phần tử trong hàng đợi (Queue) chờ kết quả gia hạn Refresh Token.
 */
interface QueueItem {
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: QueueItem[] = [];

/**
 * Xử lý toàn bộ các request đang chờ trong failedQueue sau khi tiến trình Refresh Token hoàn tất.
 * @param error Ngoại lệ nếu gia hạn thất bại (để reject toàn bộ queue).
 * @param token Access Token mới nếu gia hạn thành công (để resolve toàn bộ queue).
 */
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Interceptor Request: Tự động đính kèm Access Token vào header Authorization
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor Response: Xử lý unwrap dữ liệu thành công & mồi Refresh Token khi lỗi 401
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Trả về response.data.data (dữ liệu bên trong FormatRestResponse) nếu có,
    // fallback về response.data nếu endpoint trả về trực tiếp
    return response.data?.data ?? response.data;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as CustomAxiosRequestConfig | undefined;
    if (!originalRequest) {
      return Promise.reject(error.response?.data || error);
    }

    const url = originalRequest.url || '';

    // Requirement 5: Không refresh đối với:
    // - Request login (/login)
    // - Request refresh token (/refresh)
    // - Request đã được retry (_retry === true)
    // - Lỗi không phải HTTP 401 Unauthorized
    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      url.includes('/login') ||
      url.includes('/refresh')
    ) {
      return Promise.reject(error.response?.data || error);
    }

    // Requirement 2: Khi isRefreshing = true (đang có 1 request refresh token chạy ngầm),
    // các request 401 còn lại phải chờ chung kết quả trong queue thay vì gửi thêm request refresh
    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          // Requirement 3 & 6: Gắn cờ _retry = true cho request trong queue để ngăn lặp lại
          // Gắn token mới và retry request đúng một lần
          originalRequest._retry = true;
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${token}`;
          }
          return api(originalRequest);
        })
        .catch((err: unknown) => {
          const axiosErr = err as AxiosError;
          return Promise.reject(axiosErr?.response?.data || err);
        });
    }

    // Requirement 1 & 6: Khóa tiến trình bằng isRefreshing và đánh dấu cờ _retry cho request đầu tiên
    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      // Requirement 8: Giữ nguyên endpoint, request body ({ refreshToken }) và response contract
      // Dùng axios.post trực tiếp (không qua `api`) để không bị intercept lặp vòng
      const res = await axios.post(`${API_BASE_URL}/refresh`, { refreshToken });

      // Lấy Access Token & Refresh Token mới từ chuẩn FormatRestResponse ({ data: { accessToken, refreshToken } })
      const newAccessToken: string | undefined = res.data?.data?.accessToken ?? res.data?.accessToken;
      const newRefreshToken: string | undefined = res.data?.data?.refreshToken ?? res.data?.refreshToken;

      if (!newAccessToken) {
        throw new Error('Invalid refresh token response contract');
      }

      // Requirement 3 & 8: Lưu access token mới theo đúng storage key hiện tại
      localStorage.setItem('access_token', newAccessToken);
      if (newRefreshToken) {
        localStorage.setItem('refresh_token', newRefreshToken);
      }

      // Requirement 3: Resolve toàn bộ queue đang chờ
      processQueue(null, newAccessToken);

      // Gắn token mới vào request đầu tiên và retry đúng một lần
      if (originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      }
      return await api(originalRequest);
    } catch (refreshError: unknown) {
      // Requirement 4: Khi refresh thất bại -> Reject toàn bộ queue, xóa token theo logic hiện tại
      processQueue(refreshError, null);

      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user');

      // Logout / redirect đúng một lần nếu đang ở trên trình duyệt
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }

      const axiosRefreshErr = refreshError as AxiosError;
      return Promise.reject(axiosRefreshErr?.response?.data || refreshError);
    } finally {
      // Requirement 7: Reset isRefreshing trong finally để đảm bảo không bao giờ bị khóa ngầm
      isRefreshing = false;
    }
  }
);
