import { describe, expect, it, vi } from 'vitest';

import { createSingleFlight } from './singleFlight';

describe('createSingleFlight', () => {
  it('shares one refresh operation across concurrent callers', async () => {
    const operation = vi.fn(async () => 'new-access-token');
    const refresh = createSingleFlight(operation);

    const results = await Promise.all(Array.from({ length: 10 }, () => refresh()));

    expect(operation).toHaveBeenCalledTimes(1);
    expect(results).toEqual(Array(10).fill('new-access-token'));
  });

  it('clears the active promise after failure', async () => {
    const operation = vi.fn()
      .mockRejectedValueOnce(new Error('refresh failed'))
      .mockResolvedValueOnce('recovered');
    const refresh = createSingleFlight(operation);

    await expect(Promise.all([refresh(), refresh()])).rejects.toThrow('refresh failed');
    await expect(refresh()).resolves.toBe('recovered');
    expect(operation).toHaveBeenCalledTimes(2);
  });
});
