import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/layout';
import { LoadingSpinner } from '../components/ui';
import { useApp } from '../context/AppContext';
import type { ViewMode } from '../types';

interface DashboardLayoutProps {
  variant: ViewMode;
}

export function DashboardLayout({ variant }: DashboardLayoutProps) {
  const { sidebarOpen } = useApp();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Sidebar variant={variant === 'admin' ? 'admin' : 'user'} />
      <main
        className="transition-all duration-300 p-6 lg:p-8"
        style={{ marginLeft: sidebarOpen ? '16rem' : '5rem' }}
      >
        <Suspense fallback={<LoadingSpinner className="min-h-[60vh]" />}>
          <Outlet />
        </Suspense>
      </main>
    </div>
  );
}
