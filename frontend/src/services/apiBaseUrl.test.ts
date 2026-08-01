import { describe, expect, it } from 'vitest';

import { resolveApiBaseUrl } from './api';

describe('resolveApiBaseUrl', () => {
  it('uses the same-origin API path by default', () => {
    expect(resolveApiBaseUrl()).toBe('/api/v1');
    expect(resolveApiBaseUrl('   ')).toBe('/api/v1');
  });

  it('allows an explicit local development override', () => {
    expect(resolveApiBaseUrl(' http://localhost:8080/api/v1 '))
      .toBe('http://localhost:8080/api/v1');
  });
});
