import React, { ReactElement, StrictMode } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { createTestQueryClient } from './createTestQueryClient';

export interface RenderWithProvidersOptions extends Omit<RenderOptions, 'wrapper'> {
  queryClient?: QueryClient;
  initialEntries?: string[];
  withToaster?: boolean;
  strictMode?: boolean;
}

export const renderWithProviders = (
  ui: ReactElement,
  options: RenderWithProvidersOptions = {}
) => {
  const {
    queryClient = createTestQueryClient(),
    initialEntries = ['/'],
    withToaster = false,
    strictMode = false,
    ...renderOptions
  } = options;

  const content = ui;
  const elementWithStrictMode = strictMode ? <StrictMode>{content}</StrictMode> : content;

  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <MemoryRouter initialEntries={initialEntries}>
      <QueryClientProvider client={queryClient}>
        {children}
        {withToaster ? <Toaster /> : null}
      </QueryClientProvider>
    </MemoryRouter>
  );

  return {
    queryClient,
    ...render(elementWithStrictMode, { wrapper: Wrapper, ...renderOptions }),
  };
};
