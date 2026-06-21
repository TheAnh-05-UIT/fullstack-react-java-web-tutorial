import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';

import { DashboardLayout } from './pages/DashboardLayout';
import { 
  DashboardHome, 
  DashboardLearning, 
  DashboardTutorials, 
  DashboardProjects, 
  DashboardSettings 
} from './pages/user';

import { 
  AdminOverview, 
  AdminUsers, 
  AdminTutorials, 
  AdminProjects, 
  AdminRoadmaps, 
  AdminSettings 
} from './pages/admin';

import { AppProvider, useApp } from './context/AppContext';
import { ScrollToTop } from './components/ScrollToTop';
import { BackToTop } from './components/ui';

// Import Public Layout và Pages
import { Navbar, Footer } from './components/layout';
import { 
  HomePage, 
  TutorialsPage, 
  TutorialDetailPage, 
  ProjectsPage, 
  ProjectDetailPage, 
  RoadmapsPage, 
  RoadmapDetailPage, 
  AboutPage 
} from './pages/public';

function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-white dark:bg-gray-950 flex flex-col">
      <Navbar />
      <main className="flex-1">
        {children}
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
  const { viewMode } = useApp();

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
      
      {/* Dashboard Dành Cho Người Dùng Đã Đăng Nhập */}
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <DashboardLayout variant="user" />
        </ProtectedRoute>
      }>
        <Route index element={<DashboardHome />} />
        <Route path="learning" element={<DashboardLearning />} />
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

function App() {
  return (
    <BrowserRouter>
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
