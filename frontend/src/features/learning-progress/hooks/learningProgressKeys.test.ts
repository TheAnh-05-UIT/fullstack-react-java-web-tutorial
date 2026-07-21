import { describe, expect, it } from 'vitest';
import { learningProgressKeys } from './learningProgressKeys';

describe('learningProgressKeys', () => {
  it('lists() should be stable', () => {
    expect(learningProgressKeys.lists()).toEqual(['learning-progress', 'list']);
  });

  it('list() should generate correct key based on filters', () => {
    expect(learningProgressKeys.list({ page: 0, size: 10, status: 'IN_PROGRESS', contentType: 'TUTORIAL' }))
      .toEqual(['learning-progress', 'list', 0, 10, 'IN_PROGRESS', 'TUTORIAL']);
  });

  it('list() should normalize undefined status and contentType to ALL', () => {
    expect(learningProgressKeys.list({ page: 1, size: 20 }))
      .toEqual(['learning-progress', 'list', 1, 20, 'ALL', 'ALL']);
  });

  it('detail() should use provided contentKey as is', () => {
    expect(learningProgressKeys.detail('PROJECT', ' my-project '))
      .toEqual(['learning-progress', 'detail', 'PROJECT', ' my-project ']);
  });
});
