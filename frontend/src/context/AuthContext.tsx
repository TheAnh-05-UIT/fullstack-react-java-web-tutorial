import { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { jwtDecode } from 'jwt-decode';
import { api } from '../services/api';
import type { AuthUser } from '../types';

interface DecodedToken {
  sub: string;
  scope?: string;
  role?: string;
  exp: number;
}

// Dùng AuthUser từ types/index.ts thay vì định nghĩa UserProfile riêng ở đây.
// Re-export UserProfile = AuthUser để không phải sửa các file đang import UserProfile.
export type UserProfile = AuthUser;


interface AuthContextType {
  isAuthenticated: boolean;
  role: string | null;
  user: UserProfile | null;
  login: (accessToken: string, refreshToken: string, userData: UserProfile) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Helper extract role từ JWT token – tránh duplicate code
function extractRoleFromToken(token: string, fallback?: string): string {
  try {
    const decoded = jwtDecode<DecodedToken>(token);
    let role = decoded.scope || decoded.role || fallback || 'USER';
    // Spring Boot Security đặt role dạng "ROLE_ADMIN", strip prefix để lấy "ADMIN"
    if (role.startsWith('ROLE_')) {
      role = role.substring(5);
    }
    return role;
  } catch {
    return fallback || 'USER';
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [role, setRole] = useState<string | null>(null);
  const [user, setUser] = useState<UserProfile | null>(null);

  // Dùng useCallback để tránh stale closure khi dùng logout trong useEffect
  const logout = useCallback(() => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setRole(null);
    setUser(null);
  }, []);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('access_token');
      const refreshToken = localStorage.getItem('refresh_token');
      const userStr = localStorage.getItem('user');

      if (token && refreshToken && userStr) {
        try {
          const parsedUser = JSON.parse(userStr);

          // Kiểm tra refresh token có hết hạn không.
          // Nếu refresh token còn hạn → cứ tạm coi là đã đăng nhập (interceptor sẽ
          // tự refresh access token khi cần). Nếu refresh token CŨNG hết hạn → logout
          // ngay lập tức để tránh UX xấu (user thấy đã đăng nhập nhưng bị redirect
          // đột ngột khi gọi API đầu tiên).
          const decodedRefresh = jwtDecode<DecodedToken>(refreshToken);
          if (decodedRefresh.exp * 1000 < Date.now()) {
            // Refresh token hết hạn → xóa toàn bộ, bắt đăng nhập lại
            logout();
            return;
          }

          setIsAuthenticated(true);
          // Dùng helper để extract role
          const userRole = extractRoleFromToken(token, parsedUser.role);
          setRole(userRole);
          setUser(parsedUser);
        } catch {
          // Token hoặc user data bị corrupt → logout
          logout();
        }
      }
    };

    checkAuth();
  }, [logout]); // Thêm logout vào deps để tránh stale closure

  const login = (accessToken: string, refreshToken: string, userData: UserProfile) => {
    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));

    try {
      // Dùng helper để extract role
      const userRole = extractRoleFromToken(accessToken, userData.role);
      setIsAuthenticated(true);
      setRole(userRole);
      setUser(userData);
    } catch {
      console.error('Invalid token format');
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, role, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

