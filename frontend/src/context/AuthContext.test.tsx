import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthProvider, useAuth } from './AuthContext';

const { bootstrap, logoutRequest, setAccessToken } = vi.hoisted(() => ({
  bootstrap: vi.fn(),
  logoutRequest: vi.fn(),
  setAccessToken: vi.fn(),
}));

vi.mock('../services/authService', () => ({
  authService: {
    bootstrap,
    logout: logoutRequest,
  },
}));

vi.mock('../services/api', () => ({
  setAccessToken,
  setAuthFailureHandler: vi.fn(),
}));

vi.mock('jwt-decode', () => ({
  jwtDecode: () => ({ scope: 'ROLE_USER' }),
}));

function Harness() {
  const auth = useAuth();
  return (
    <div>
      <span>{auth.isInitialized ? 'initialized' : 'loading'}</span>
      <span>{auth.isAuthenticated ? 'authenticated' : 'anonymous'}</span>
      <button type="button" onClick={() => auth.login('memory-token', {
        id: 1,
        email: 'user@example.test',
        role: 'USER',
      })}>
        Login
      </button>
    </div>
  );
}

function renderProvider() {
  const queryClient = new QueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Harness />
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('AuthProvider browser session storage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    bootstrap.mockRejectedValue(new Error('no cookie session'));
  });

  it('cleans only legacy auth keys and bootstraps from the refresh cookie', async () => {
    localStorage.setItem('access_token', 'legacy-access');
    localStorage.setItem('refresh_token', 'legacy-refresh');
    localStorage.setItem('user', '{}');
    localStorage.setItem('app-theme', 'dark');

    renderProvider();

    await waitFor(() => expect(screen.getByText('initialized')).toBeInTheDocument());
    expect(bootstrap).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(localStorage.getItem('app-theme')).toBe('dark');
  });

  it('keeps a successful login access token only in memory', async () => {
    renderProvider();
    await waitFor(() => expect(screen.getByText('initialized')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(setAccessToken).toHaveBeenCalledWith('memory-token');
    expect(screen.getByText('authenticated')).toBeInTheDocument();
    expect(localStorage.length).toBe(0);
  });
});
