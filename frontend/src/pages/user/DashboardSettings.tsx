import { User, Bell, Shield, Moon, Sun, Globe } from 'lucide-react';
import { Card, Button, Input, Avatar } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../hooks/useTheme';

export function DashboardSettings() {
  const { user } = useAuth();
  const { isDark, toggle } = useTheme();

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Settings</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage your account and preferences</p>
      </div>

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-6">
          <User className="w-5 h-5 text-gray-500" />
          <h2 className="font-semibold text-gray-900 dark:text-gray-100">Profile Information</h2>
        </div>
        <div className="flex items-start gap-6 mb-6">
          <div className="relative inline-block">
            <Avatar src={(user as any)?.avatar || ''} alt={user?.username || ''} size="xl" />
            <button className="absolute bottom-0 right-0 p-2 bg-white dark:bg-gray-800 rounded-full border border-gray-200 dark:border-gray-700 shadow-sm text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-500 transition-colors">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
          <div className="flex-1 space-y-4">
            <div className="grid sm:grid-cols-2 gap-4">
              <Input label="First Name" defaultValue={user?.username?.split(' ')[0]} />
              <Input label="Last Name" defaultValue={user?.username?.split(' ')[1] || ''} />
            </div>
            <Input label="Email" type="email" defaultValue={user?.email} />
            <Input label="Bio" placeholder="Tell us about yourself..." />
          </div>
        </div>
        <div className="flex justify-end">
          <Button>Save Changes</Button>
        </div>
      </Card>

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-6">
          <Bell className="w-5 h-5 text-gray-500" />
          <h2 className="font-semibold text-gray-900 dark:text-gray-100">Notifications</h2>
        </div>
        <div className="space-y-4">
          {[
            { label: 'Email notifications', description: 'Receive emails about your learning progress' },
            { label: 'New tutorial alerts', description: 'Get notified when new tutorials are published' },
            { label: 'Weekly digest', description: 'Receive a weekly summary of your activity' },
            { label: 'Marketing emails', description: 'Receive updates about new features and promotions' },
          ].map((item, index) => (
            <div key={index} className="flex items-center justify-between py-2">
              <div>
                <p className="font-medium text-gray-900 dark:text-gray-100">{item.label}</p>
                <p className="text-sm text-gray-500 dark:text-gray-400">{item.description}</p>
              </div>
              <button
                className={`w-11 h-6 rounded-full transition-colors ${index < 3 ? 'bg-primary-600' : 'bg-gray-300 dark:bg-gray-700'}`}
              >
                <div className={`w-4 h-4 rounded-full bg-white transform transition-transform ${index < 3 ? 'translate-x-6' : 'translate-x-1'}`} />
              </button>
            </div>
          ))}
        </div>
      </Card>

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-6">
          <Globe className="w-5 h-5 text-gray-500" />
          <h2 className="font-semibold text-gray-900 dark:text-gray-100">Appearance</h2>
        </div>
        <div className="flex items-center justify-between">
          <div>
            <p className="font-medium text-gray-900 dark:text-gray-100">Theme</p>
            <p className="text-sm text-gray-500 dark:text-gray-400">Choose your preferred theme</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => { if (isDark) toggle(); }}
              className={`px-4 py-2 rounded-lg flex items-center gap-2 transition-colors ${!isDark ? 'bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'
                }`}
            >
              <Sun className="w-4 h-4" />
              Light
            </button>
            <button
              onClick={() => { if (!isDark) toggle(); }}
              className={`px-4 py-2 rounded-lg flex items-center gap-2 transition-colors ${isDark ? 'bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'
                }`}
            >
              <Moon className="w-4 h-4" />
              Dark
            </button>
          </div>
        </div>
      </Card>

      <Card className="p-6 border-error-200 dark:border-error-900/50">
        <div className="flex items-center gap-2 mb-4">
          <Shield className="w-5 h-5 text-error-500" />
          <h2 className="font-semibold text-error-600 dark:text-error-400">Danger Zone</h2>
        </div>
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
          Once you delete your account, there is no going back. Please be certain.
        </p>
        <Button variant="accent">Delete Account</Button>
      </Card>
    </div>
  );
}
