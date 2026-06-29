import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import type { ViewMode } from '../types';

interface AppContextType {
  viewMode: ViewMode;
  setViewMode: (mode: ViewMode) => void;
  sidebarOpen: boolean;
  setSidebarOpen: (open: boolean) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  // Dùng useLocation() từ react-router-dom thay vì window.location.pathname.
  // window.location chỉ đọc giá trị 1 lần khi mount → viewMode không cập nhật khi
  // điều hướng bằng React Router (client-side navigation, không reload trang).
  // useLocation() là reactive: mỗi khi route thay đổi, useEffect chạy lại và
  // cập nhật viewMode cho đúng.
  const location = useLocation();

  const getViewModeFromPath = (pathname: string): ViewMode => {
    if (pathname.startsWith('/admin')) return 'admin';
    if (pathname.startsWith('/dashboard')) return 'user';
    return 'public';
  };

  const [viewMode, setViewMode] = useState<ViewMode>(() => getViewModeFromPath(location.pathname));
  const [sidebarOpen, setSidebarOpen] = useState(true);

  // Cập nhật viewMode mỗi khi route thay đổi
  useEffect(() => {
    setViewMode(getViewModeFromPath(location.pathname));
  }, [location.pathname]);

  return (
    <AppContext.Provider value={{
      viewMode,
      setViewMode,
      sidebarOpen,
      setSidebarOpen,
    }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
}

