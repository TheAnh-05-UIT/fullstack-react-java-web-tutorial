import { createContext, useContext, useState, type ReactNode, useEffect } from 'react';
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
}

interface AuthContextType {
  isAuthenticated: boolean;
  role: string | null;
  user: UserProfile | null;
  login: (accessToken: string, refreshToken: string, userData: UserProfile) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [role, setRole] = useState<string | null>(null);
  const [user, setUser] = useState<UserProfile | null>(null);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('access_token');
      const userStr = localStorage.getItem('user');

      if (token && userStr) {
        try {
          const decoded = jwtDecode<DecodedToken>(token);
          // Cho dù access_token hết hạn, interceptor vẫn sẽ tự động refresh
          // Nên cứ tạm coi là có quyền nếu còn thẻ (refresh token sẽ gánh)
          setIsAuthenticated(true);
          const parsedUser = JSON.parse(userStr);
          // Spring Boot Security puts role in "scope" claim like "ROLE_ADMIN"
          let userRole = decoded.scope || decoded.role || parsedUser.role || 'USER';
          if (userRole.startsWith('ROLE_')) {
            userRole = userRole.substring(5); // Convert "ROLE_ADMIN" to "ADMIN"
          }
          setRole(userRole);
          setUser(parsedUser);
        } catch (e) {
          logout();
        }
      }
    };

    checkAuth();
  }, []);

  const login = (accessToken: string, refreshToken: string, userData: UserProfile) => {
    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));

    try {
      const decoded = jwtDecode<DecodedToken>(accessToken);
      setIsAuthenticated(true);
      let userRole = decoded.scope || decoded.role || userData.role || 'USER';
      if (userRole.startsWith('ROLE_')) {
        userRole = userRole.substring(5); // Convert "ROLE_ADMIN" to "ADMIN"
      }
      setRole(userRole);
      setUser(userData);
    } catch (e) {
      console.error('Invalid token format');
    }
  };

  const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setRole(null);
    setUser(null);
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
