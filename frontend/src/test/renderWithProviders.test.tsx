import { describe, expect, it } from 'vitest';
import { useLocation } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { renderWithProviders } from './renderWithProviders';

const TestComponent = () => {
  const location = useLocation();
  const queryClient = useQueryClient();
  
  return (
    <div>
      <span data-testid="path">{location.pathname}</span>
      <span data-testid="has-client">{queryClient ? 'yes' : 'no'}</span>
    </div>
  );
};

describe('renderWithProviders', () => {
  it('should provide router and queryClient context', () => {
    const { getByTestId, queryClient } = renderWithProviders(<TestComponent />, {
      initialEntries: ['/test-route']
    });

    expect(getByTestId('path')).toHaveTextContent('/test-route');
    expect(getByTestId('has-client')).toHaveTextContent('yes');
    expect(queryClient).toBeDefined();
  });

  it('should handle strictMode and withToaster options without crashing', () => {
    const { getByTestId } = renderWithProviders(<TestComponent />, {
      strictMode: true,
      withToaster: true,
    });
    
    expect(getByTestId('path')).toHaveTextContent('/');
  });
});
