import { describe, expect, it } from 'vitest';
import { createTestQueryClient } from './createTestQueryClient';

describe('createTestQueryClient', () => {
  it('should create a new QueryClient instance on each call', () => {
    const client1 = createTestQueryClient();
    const client2 = createTestQueryClient();
    expect(client1).not.toBe(client2);
  });

  it('should have retry set to false for queries and mutations', () => {
    const client = createTestQueryClient();
    const queryDefaults = client.getDefaultOptions().queries;
    const mutationDefaults = client.getDefaultOptions().mutations;
    
    expect(queryDefaults?.retry).toBe(false);
    expect(mutationDefaults?.retry).toBe(false);
  });
});
