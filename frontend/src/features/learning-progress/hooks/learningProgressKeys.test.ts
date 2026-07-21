import { describe, expect, it } from 'vitest';
import { learningProgressKeys } from './learningProgressKeys';

describe('learningProgressKeys', () => {
  it('all should be stable', () => {
    expect(learningProgressKeys.all).toEqual(['learning-progress']);
  });

  it('summary should be stable and not contain userId', () => {
    expect(learningProgressKeys.summary()).toEqual([...learningProgressKeys.all, 'summary']);
  });

  it('continue should be stable and not contain userId', () => {
    expect(learningProgressKeys.continue()).toEqual([...learningProgressKeys.all, 'continue']);
  });

  it('lists() should start with all and not contain userId', () => {
    expect(learningProgressKeys.lists()).toEqual([...learningProgressKeys.all, 'list']);
  });

  it('list(filters) should start with lists()', () => {
    const listKey = learningProgressKeys.list({ page: 0, size: 10 });
    expect(listKey.slice(0, 2)).toEqual(learningProgressKeys.lists());
  });

  it('different pages should create different keys', () => {
    const key0 = learningProgressKeys.list({ page: 0, size: 10 });
    const key1 = learningProgressKeys.list({ page: 1, size: 10 });
    expect(key0).not.toEqual(key1);
  });

  it('different sizes should create different keys', () => {
    const key10 = learningProgressKeys.list({ page: 0, size: 10 });
    const key20 = learningProgressKeys.list({ page: 0, size: 20 });
    expect(key10).not.toEqual(key20);
  });

  it('different status should create different keys', () => {
    const key1 = learningProgressKeys.list({ page: 0, size: 10, status: 'IN_PROGRESS' });
    const key2 = learningProgressKeys.list({ page: 0, size: 10, status: 'COMPLETED' });
    expect(key1).not.toEqual(key2);
  });

  it('different contentType should create different keys', () => {
    const key1 = learningProgressKeys.list({ page: 0, size: 10, contentType: 'TUTORIAL' });
    const key2 = learningProgressKeys.list({ page: 0, size: 10, contentType: 'PROJECT' });
    expect(key1).not.toEqual(key2);
  });

  it('list() should normalize undefined status and contentType to ALL', () => {
    expect(learningProgressKeys.list({ page: 1, size: 20 }))
      .toEqual(['learning-progress', 'list', 1, 20, 'ALL', 'ALL']);
  });

  it('detail() should use provided contentKey as is', () => {
    expect(learningProgressKeys.detail('PROJECT', ' my-project '))
      .toEqual(['learning-progress', 'detail', 'PROJECT', ' my-project ']);
  });

  it('different contentTypes in detail should create different keys', () => {
    const key1 = learningProgressKeys.detail('PROJECT', 'key1');
    const key2 = learningProgressKeys.detail('TUTORIAL', 'key1');
    expect(key1).not.toEqual(key2);
  });

  it('different contentKeys in detail should create different keys', () => {
    const key1 = learningProgressKeys.detail('PROJECT', 'key1');
    const key2 = learningProgressKeys.detail('PROJECT', 'key2');
    expect(key1).not.toEqual(key2);
  });
});
