import { createContext, useContext, useState, ReactNode } from 'react';
import type { ViewMode } from '../types';

interface AppContextType {
  viewMode: ViewMode;
  setViewMode: (mode: ViewMode) => void;
  sidebarOpen: boolean;
  setSidebarOpen: (open: boolean) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const getInitialViewMode = (): ViewMode => {
    if (window.location.pathname.startsWith('/admin')) return 'admin';
    if (window.location.pathname.startsWith('/dashboard')) return 'user';
    return 'public';
  };

  const [viewMode, setViewMode] = useState<ViewMode>(getInitialViewMode());
  const [sidebarOpen, setSidebarOpen] = useState(true);

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
