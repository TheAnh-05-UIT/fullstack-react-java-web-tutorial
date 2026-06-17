import { NavLink } from 'react-router-dom';
import { LayoutDashboard, BookOpen, FolderKanban, Map, Bookmark, StickyNote, Award, User, Settings, ChevronLeft, ChevronRight } from 'lucide-react';
import { Avatar } from '../ui';
import { useApp } from '../../context/AppContext';
import { useAuth } from '../../context/AuthContext';

const userLinks = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { path: '/dashboard/learning', label: 'My Learning', icon: BookOpen },
  { path: '/dashboard/tutorials', label: 'Tutorials', icon: BookOpen },
  { path: '/dashboard/projects', label: 'Projects', icon: FolderKanban },
  { path: '/dashboard/roadmaps', label: 'Roadmaps', icon: Map },
  { path: '/dashboard/saved', label: 'Saved Articles', icon: Bookmark },
  { path: '/dashboard/notes', label: 'Notes', icon: StickyNote },
  { path: '/dashboard/certificates', label: 'Certificates', icon: Award },
  { path: '/dashboard/profile', label: 'Profile', icon: User },
  { path: '/dashboard/settings', label: 'Settings', icon: Settings },
];

const adminLinks = [
  { path: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { path: '/admin/users', label: 'Users', icon: User },
  { path: '/admin/tutorials', label: 'Tutorials', icon: BookOpen },
  { path: '/admin/projects', label: 'Projects', icon: FolderKanban },
  { path: '/admin/roadmaps', label: 'Roadmaps', icon: Map },
  { path: '/admin/settings', label: 'Settings', icon: Settings },
];

export function Sidebar({ variant = 'user' }: { variant?: 'user' | 'admin' }) {
  const { sidebarOpen, setSidebarOpen, setViewMode } = useApp();
  const { user, logout } = useAuth();

  const links = variant === 'admin' ? adminLinks : userLinks;

  return (
    <aside
      className={`fixed left-0 top-0 h-full bg-white dark:bg-gray-950 border-r border-gray-200 dark:border-gray-800 transition-all duration-300 z-40 ${sidebarOpen ? 'w-64' : 'w-20'}`}
    >
      <div className="flex flex-col h-full">
        <div className="flex items-center justify-between h-16 px-4 border-b border-gray-200 dark:border-gray-800">
          <div className={`flex items-center gap-2 ${!sidebarOpen && 'justify-center w-full'}`}>
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-600 to-secondary-600 flex items-center justify-center shrink-0">
              <span className="text-white font-bold text-sm">D</span>
            </div>
            {sidebarOpen && <span className="text-lg font-semibold text-gray-900 dark:text-gray-100">DevOpsBuilder</span>}
          </div>
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className={`p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors ${!sidebarOpen && 'hidden'}`}
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
        </div>

        {!sidebarOpen && (
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors mx-auto my-2"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        )}

        <nav className="flex-1 overflow-y-auto p-3 scrollbar-hide">
          <ul className="space-y-1">
            <li>
              <button
                onClick={() => setViewMode('public')}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200 transition-all"
              >
                <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                {sidebarOpen && <span>Back to Site</span>}
              </button>
            </li>
            <li className="h-px bg-gray-200 dark:bg-gray-800 my-3" />
            {links.map(link => (
              <li key={link.path}>
                <NavLink
                  to={link.path}
                  end={link.end}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${isActive
                      ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200'
                    } ${!sidebarOpen && 'justify-center'}`
                  }
                >
                  <link.icon className="w-5 h-5 shrink-0" />
                  {sidebarOpen && <span>{link.label}</span>}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        {sidebarOpen && user && (
          <div className="p-4 border-t border-gray-200 dark:border-gray-800">
            <div className="flex items-center gap-3">
              <Avatar src={user.avatar} alt={user.name} size="md" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">{user.name}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user.email}</p>
              </div>
              <button
                onClick={() => {
                  logout();
                  setViewMode('public');
                }}
                className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                title="Log out"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
              </button>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}
