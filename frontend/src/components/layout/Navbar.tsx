import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Search, Moon, Sun, Menu, X, LogIn, UserPlus, LayoutDashboard } from 'lucide-react';
import { Button, SearchInput, Avatar } from '../ui';
import { useTheme } from '../../hooks/useTheme';
import { useApp } from '../../context/AppContext';
import { useAuth } from '../../context/AuthContext';

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/tutorials', label: 'Tutorials' },
  { path: '/projects', label: 'Projects' },
  { path: '/roadmaps', label: 'Roadmaps' },
  { path: '/about', label: 'About' },
];

export function Navbar() {
  const { isDark, toggle } = useTheme();
  const { setViewMode } = useApp();
  const { isAuthenticated, user, role } = useAuth();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="sticky top-0 z-50 bg-white/80 dark:bg-gray-950/80 backdrop-blur-lg border-b border-gray-200 dark:border-gray-800">
      <div className="container-app">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-600 to-secondary-600 flex items-center justify-center">
                <span className="text-white font-bold text-sm">D</span>
              </div>
              <span className="text-lg font-semibold text-gray-900 dark:text-gray-100">DevOpsBuilder</span>
            </Link>

            <nav className="hidden md:flex items-center gap-1">
              {navLinks.map(link => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive(link.path)
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
                    : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-gray-50 dark:hover:bg-gray-800'
                    }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-3">
            <div className={`hidden sm:block transition-all duration-300 ${searchOpen ? 'w-64' : 'w-auto'}`}>
              {searchOpen ? (
                <SearchInput placeholder="Search tutorials..." className="w-full" autoFocus onBlur={() => setSearchOpen(false)} />
              ) : (
                <button
                  onClick={() => setSearchOpen(true)}
                  className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                  <Search className="w-5 h-5" />
                </button>
              )}
            </div>

            <button
              onClick={toggle}
              className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
            </button>

            <div className="hidden sm:flex items-center gap-2">
              {isAuthenticated && user ? (
                <>
                  <button 
                    onClick={() => setViewMode('user')}
                    className="flex items-center gap-2 px-3 py-1.5 rounded-full border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                  >
                    <Avatar src={user.avatar} alt={user.name} size="sm" />
                    <span className="text-sm font-medium text-gray-700 dark:text-gray-200">{user.name.split(' ')[0]}</span>
                  </button>
                  {role === 'ADMIN' && (
                    <Button variant="secondary" size="md" onClick={() => setViewMode('admin')}>
                      Admin
                    </Button>
                  )}
                </>
              ) : (
                <>
                  <Link to="/login">
                    <Button variant="ghost" size="md">
                      <LogIn className="w-4 h-4" />
                      Log in
                    </Button>
                  </Link>
                  <Link to="/register">
                    <Button variant="primary" size="md">
                      <UserPlus className="w-4 h-4" />
                      Sign up
                    </Button>
                  </Link>
                </>
              )}
            </div>

            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {mobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-gray-200 dark:border-gray-800 animate-slide-down">
            <nav className="flex flex-col gap-1">
              {navLinks.map(link => (
                <Link
                  key={link.path}
                  to={link.path}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`px-3 py-2 rounded-lg text-sm font-medium ${isActive(link.path)
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
                    : 'text-gray-600 dark:text-gray-400'
                    }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            <div className="mt-4 flex flex-col gap-2">
              {isAuthenticated && user ? (
                <>
                  <Button variant="ghost" className="w-full justify-center" onClick={() => setViewMode('user')}>
                    <LayoutDashboard className="w-4 h-4" />
                    Dashboard
                  </Button>
                  {role === 'ADMIN' && (
                    <Button variant="secondary" className="w-full justify-center" onClick={() => setViewMode('admin')}>
                      Admin Dashboard
                    </Button>
                  )}
                </>
              ) : (
                <>
                  <Link to="/login" className="block w-full">
                    <Button variant="ghost" className="w-full justify-center">
                      <LogIn className="w-4 h-4" />
                      Log in
                    </Button>
                  </Link>
                  <Link to="/register" className="block w-full">
                    <Button variant="primary" className="w-full justify-center">
                      <UserPlus className="w-4 h-4" />
                      Sign up
                    </Button>
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
