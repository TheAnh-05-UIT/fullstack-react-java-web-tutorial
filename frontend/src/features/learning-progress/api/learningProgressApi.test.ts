import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '../../../services/api';
import { learningProgressApi } from './learningProgressApi';
import type { LearningProgressListFilters } from '../types/learningProgress.types';

vi.mock('../../../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('learningProgressApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getMyProgressPage', () => {
    it('should call GET with page and size params', async () => {
      const filters: LearningProgressListFilters = { page: 0, size: 10 };
      vi.mocked(api.get).mockResolvedValueOnce({ content: [], totalElements: 0 });

      await learningProgressApi.getMyProgressPage(filters);

      expect(api.get).toHaveBeenCalledWith('/learning-progress/me', {
        params: { page: 0, size: 10 },
      });
    });

    it('should include status and contentType if provided and not ALL', async () => {
      const filters: LearningProgressListFilters = {
        page: 1,
        size: 5,
        status: 'IN_PROGRESS',
        contentType: 'TUTORIAL',
      };
      vi.mocked(api.get).mockResolvedValueOnce({});

      await learningProgressApi.getMyProgressPage(filters);

      expect(api.get).toHaveBeenCalledWith('/learning-progress/me', {
        params: { page: 1, size: 5, status: 'IN_PROGRESS', contentType: 'TUTORIAL' },
      });
    });

    it('should ignore status and contentType if they are ALL', async () => {
      const filters: LearningProgressListFilters = {
        page: 0,
        size: 20,
        status: 'ALL' as unknown as 'IN_PROGRESS',
        contentType: 'ALL' as unknown as 'TUTORIAL',
      };
      vi.mocked(api.get).mockResolvedValueOnce({});

      await learningProgressApi.getMyProgressPage(filters);

      expect(api.get).toHaveBeenCalledWith('/learning-progress/me', {
        params: { page: 0, size: 20 },
      });
    });
  });

  describe('URL encoding and buildContentPath', () => {
    it('should encode contentKey with spaces and slashes', async () => {
      vi.mocked(api.get).mockResolvedValueOnce({});
      
      // key like "java spring/cơ bản"
      await learningProgressApi.getProgress('TUTORIAL', 'java spring/cơ bản');
      
      const expectedEncoded = encodeURIComponent('java spring/cơ bản');
      expect(api.get).toHaveBeenCalledWith(`/learning-progress/me/TUTORIAL/${expectedEncoded}`);
      // Ensure we don't double encode
      expect(expectedEncoded).toBe('java%20spring%2Fc%C6%A1%20b%E1%BA%A3n');
    });
  });

  describe('Mutations', () => {
    it('should call POST for touchProgress', async () => {
      vi.mocked(api.post).mockResolvedValueOnce({});
      await learningProgressApi.touchProgress('PROJECT', 'test-key');
      expect(api.post).toHaveBeenCalledWith('/learning-progress/me/PROJECT/test-key/touch');
    });

    it('should call PUT for updateProgress with correct body', async () => {
      vi.mocked(api.put).mockResolvedValueOnce({});
      await learningProgressApi.updateProgress('TUTORIAL', 'test-key', { progressPercent: 50 });
      expect(api.put).toHaveBeenCalledWith('/learning-progress/me/TUTORIAL/test-key', { progressPercent: 50 });
    });

    it('should call POST for completeProgress', async () => {
      vi.mocked(api.post).mockResolvedValueOnce({});
      await learningProgressApi.completeProgress('ROADMAP', 'test-key');
      expect(api.post).toHaveBeenCalledWith('/learning-progress/me/ROADMAP/test-key/complete');
    });

    it('should call DELETE for resetProgress', async () => {
      vi.mocked(api.delete).mockResolvedValueOnce({});
      const result = await learningProgressApi.resetProgress('DEVOPS_PHASE', 'test-key');
      expect(api.delete).toHaveBeenCalledWith('/learning-progress/me/DEVOPS_PHASE/test-key');
      expect(result).toBeUndefined();
    });
  });
});
