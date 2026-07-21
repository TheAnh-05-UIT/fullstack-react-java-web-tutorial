import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';

import { DashboardLayout } from './pages/DashboardLayout';
import { AppProvider, useApp } from './context/AppContext';
import { ScrollToTop } from './components/ScrollToTop';
import { BackToTop, LoadingSpinner } from './components/ui';

// Import Public Layout và HomePage (giữ eager cho HomePage tải ngay lập tức)
import { Navbar, Footer } from './components/layout';
import { HomePage } from './pages/public/HomePage';

// Lazy load Public Pages (lớn hoặc ít truy cập ban đầu)
const TutorialsPage = lazy(() => import('./pages/public/TutorialsPage').then(m => ({ default: m.TutorialsPage })));
const TutorialDetailPage = lazy(() => import('./pages/public/TutorialDetailPage').then(m => ({ default: m.TutorialDetailPage })));
const ProjectsPage = lazy(() => import('./pages/public/ProjectsPage').then(m => ({ default: m.ProjectsPage })));
const ProjectDetailPage = lazy(() => import('./pages/public/ProjectDetailPage').then(m => ({ default: m.ProjectDetailPage })));
const RoadmapsPage = lazy(() => import('./pages/public/RoadmapsPage').then(m => ({ default: m.RoadmapsPage })));
const RoadmapDetailPage = lazy(() => import('./pages/public/RoadmapDetailPage').then(m => ({ default: m.RoadmapDetailPage })));
const AboutPage = lazy(() => import('./pages/public/AboutPage').then(m => ({ default: m.AboutPage })));

// Lazy load DevOps Pages
const DevOpsPhaseRoute = lazy(() => import('./features/devops/pages/DevOpsPhaseRoute').then(m => ({ default: m.DevOpsPhaseRoute })));
const DevOpsAdminPage = lazy(() => import('./features/devops/admin/DevOpsAdminPage').then(m => ({ default: m.DevOpsAdminPage })));

// Lazy load User Dashboard Pages
const DashboardHome = lazy(() => import('./pages/user/DashboardHome').then(m => ({ default: m.DashboardHome })));
const DashboardLearning = lazy(() => import('./pages/user/DashboardLearning').then(m => ({ default: m.DashboardLearning })));
const DashboardLearningProgress = lazy(() => import('./pages/user/DashboardLearningProgress').then(m => ({ default: m.DashboardLearningProgress })));
const DashboardTutorials = lazy(() => import('./pages/user/DashboardTutorials').then(m => ({ default: m.DashboardTutorials })));
const DashboardProjects = lazy(() => import('./pages/user/DashboardProjects').then(m => ({ default: m.DashboardProjects })));
const DashboardSettings = lazy(() => import('./pages/user/DashboardSettings').then(m => ({ default: m.DashboardSettings })));

// Lazy load Admin Pages (chứa rich text editor, table lớn, quản trị)
const AdminOverview = lazy(() => import('./pages/admin/AdminOverview').then(m => ({ default: m.AdminOverview })));
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers').then(m => ({ default: m.AdminUsers })));
const AdminTutorials = lazy(() => import('./pages/admin/AdminTutorials').then(m => ({ default: m.AdminTutorials })));
const AdminProjects = lazy(() => import('./pages/admin/AdminProjects').then(m => ({ default: m.AdminProjects })));
const AdminRoadmaps = lazy(() => import('./pages/admin/AdminRoadmaps').then(m => ({ default: m.AdminRoadmaps })));
const AdminSettings = lazy(() => import('./pages/admin/AdminSettings').then(m => ({ default: m.AdminSettings })));

function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-white dark:bg-gray-950 flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Suspense fallback={<LoadingSpinner className="min-h-[60vh]" />}>
          {children}
        </Suspense>
      </main>
      <Footer />
    </div>
  );
}

function ProtectedRoute({ children, requiredRole }: { children: React.ReactNode, requiredRole?: string }) {
  const { isAuthenticated, role } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Bỏ điều kiện thừa `role !== 'ROLE_ADMIN'` vì AuthContext đã strip prefix ROLE_
  // Role sau khi decode luôn là 'ADMIN' hoặc 'USER', không bao giờ là 'ROLE_ADMIN'
  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

function AppRoutes() {
  const { isAuthenticated } = useAuth();
  useApp();

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/register" element={isAuthenticated ? <Navigate to="/" replace /> : <RegisterPage />} />
      
      {/* Các Trang Công Khai */}
      <Route path="/" element={<PublicLayout><HomePage /></PublicLayout>} />
      <Route path="/tutorials" element={<PublicLayout><TutorialsPage /></PublicLayout>} />
      <Route path="/tutorials/:id" element={<PublicLayout><TutorialDetailPage /></PublicLayout>} />
      <Route path="/projects" element={<PublicLayout><ProjectsPage /></PublicLayout>} />
      <Route path="/projects/:id" element={<PublicLayout><ProjectDetailPage /></PublicLayout>} />
      <Route path="/roadmaps" element={<PublicLayout><RoadmapsPage /></PublicLayout>} />
      <Route path="/roadmaps/:id" element={<PublicLayout><RoadmapDetailPage /></PublicLayout>} />
      <Route path="/about" element={<PublicLayout><AboutPage /></PublicLayout>} />

      {/* DevOps Lifecycle Phase Pages (wrapped inside PublicLayout for Navbar and Footer) */}
      <Route path="/devops/:phaseKey" element={<PublicLayout><DevOpsPhaseRoute /></PublicLayout>} />
      
      {/* Dashboard Dành Cho Người Dùng Đã Đăng Nhập */}
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <DashboardLayout variant="user" />
        </ProtectedRoute>
      }>
        <Route index element={<DashboardHome />} />
        <Route path="learning" element={<DashboardLearning />} />
        <Route path="learning-progress" element={<DashboardLearningProgress />} />
        <Route path="tutorials" element={<DashboardTutorials />} />
        <Route path="projects" element={<DashboardProjects />} />
        <Route path="settings" element={<DashboardSettings />} />
      </Route>

      {/* Dashboard Dành Cho Admin */}
      <Route path="/admin" element={
        <ProtectedRoute requiredRole="ADMIN">
          <DashboardLayout variant="admin" />
        </ProtectedRoute>
      }>
        <Route index element={<AdminOverview />} />
        <Route path="devops" element={<DevOpsAdminPage />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="tutorials" element={<AdminTutorials />} />
        <Route path="projects" element={<AdminProjects />} />
        <Route path="roadmaps" element={<AdminRoadmaps />} />
        <Route path="settings" element={<AdminSettings />} />
      </Route>
      
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

import { Toaster } from 'react-hot-toast';

function App() {
  return (
    <BrowserRouter>
      <Toaster position="top-right" />
      <ScrollToTop />
      <AuthProvider>
        <AppProvider>
          <AppRoutes />
          <BackToTop />
        </AppProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
