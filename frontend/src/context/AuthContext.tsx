import { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { jwtDecode } from 'jwt-decode';
import { api } from '../services/api';

interface DecodedToken {
  sub: string;
  scope?: string;
  role?: string;
  exp: number;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role?: string;
  avatar?: string;
}

interface AuthContextType {
  isAuthenticated: boolean;
  role: string | null;
  user: UserProfile | null;
  login: (accessToken: string, refreshToken: string, userData: UserProfile) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// OPT-07: Helper extract role từ JWT token – tránh duplicate code
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

  // FIX FE-03: Dùng useCallback để tránh stale closure khi dùng logout trong useEffect
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
      const userStr = localStorage.getItem('user');

      if (token && userStr) {
        try {
          const parsedUser = JSON.parse(userStr);
          // Cho dù access_token hết hạn, interceptor vẫn sẽ tự động refresh
          // Nên cứ tạm coi là có quyền nếu còn thẻ (refresh token sẽ gánh)
          setIsAuthenticated(true);
          // OPT-07: Dùng helper để extract role
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
  }, [logout]); // FIX FE-03: Thêm logout vào deps để tránh stale closure

  const login = (accessToken: string, refreshToken: string, userData: UserProfile) => {
    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));

    try {
      // OPT-07: Dùng helper để extract role
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

