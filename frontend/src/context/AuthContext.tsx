import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { jwtDecode } from 'jwt-decode';

import { authService } from '../services/authService';
import { setAccessToken, setAuthFailureHandler } from '../services/api';
import type { AuthUser } from '../types';

interface DecodedToken {
  scope?: string;
  role?: string;
}

export type UserProfile = AuthUser;

interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  role: string | null;
  user: UserProfile | null;
  login: (accessToken: string, userData: UserProfile) => void;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function getRoleString(roleProp?: string | { id?: number; name?: string }): string | undefined {
  if (typeof roleProp === 'string') {
    return roleProp;
  }
  return roleProp?.name;
}

function extractRoleFromToken(
  token: string,
  fallback?: string | { id?: number; name?: string },
): string {
  const fallbackRole = getRoleString(fallback);
  try {
    const decoded = jwtDecode<DecodedToken>(token);
    const role = decoded.scope || decoded.role || fallbackRole || 'USER';
    return role.startsWith('ROLE_') ? role.substring(5) : role;
  } catch {
    return fallbackRole || 'USER';
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [role, setRole] = useState<string | null>(null);
  const [user, setUser] = useState<UserProfile | null>(null);
  const queryClient = useQueryClient();

  const clearAuthState = useCallback(() => {
    setAccessToken(null);
    setIsAuthenticated(false);
    setRole(null);
    setUser(null);
    queryClient.clear();
  }, [queryClient]);

  const login = useCallback((token: string, userData: UserProfile) => {
    setAccessToken(token);
    setIsAuthenticated(true);
    setRole(extractRoleFromToken(token, userData.role));
    setUser(userData);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Local state is cleared even when the network is unavailable.
    } finally {
      clearAuthState();
    }
  }, [clearAuthState]);

  useEffect(() => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
    setAuthFailureHandler(clearAuthState);

    let active = true;
    authService.bootstrap()
      .then((session) => {
        if (active) {
          login(session.accessToken, session.userLogin);
        }
      })
      .catch(() => {
        if (active) {
          clearAuthState();
        }
      })
      .finally(() => {
        if (active) {
          setIsInitialized(true);
        }
      });

    return () => {
      active = false;
      setAuthFailureHandler(null);
    };
  }, [clearAuthState, login]);

  return (
    <AuthContext.Provider value={{ isAuthenticated, isInitialized, role, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// This hook intentionally shares the provider module so callers use the same context instance.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
