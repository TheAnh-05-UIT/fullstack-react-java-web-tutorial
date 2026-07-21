import { describe, it, expect } from 'vitest';
import { BookOpen, FolderKanban, Map, Terminal } from 'lucide-react';
import {
  getLearningContentTypeLabel,
  getLearningContentTypeIcon,
  getLearningProgressStatusLabel,
  getLearningProgressStatusClassName,
  getLearningContentFallbackTitle,
  buildLearningContentRoute,
  formatLearningLastAccessedAt,
} from './learningProgressUi';

describe('learningProgressUi', () => {
  describe('getLearningContentTypeLabel', () => {
    it('returns correct label for all content types', () => {
      expect(getLearningContentTypeLabel('TUTORIAL')).toBe('Tutorial');
      expect(getLearningContentTypeLabel('PROJECT')).toBe('Project');
      expect(getLearningContentTypeLabel('ROADMAP')).toBe('Roadmap');
      expect(getLearningContentTypeLabel('DEVOPS_PHASE')).toBe('DevOps Lifecycle');
    });

    it('returns the input as fallback for unknown types', () => {
      expect(getLearningContentTypeLabel('UNKNOWN_TYPE' as unknown as 'TUTORIAL')).toBe('UNKNOWN_TYPE');
    });
  });

  describe('getLearningContentTypeIcon', () => {
    it('returns correct component for all content types', () => {
      expect(getLearningContentTypeIcon('TUTORIAL')).toBe(BookOpen);
      expect(getLearningContentTypeIcon('PROJECT')).toBe(FolderKanban);
      expect(getLearningContentTypeIcon('ROADMAP')).toBe(Map);
      expect(getLearningContentTypeIcon('DEVOPS_PHASE')).toBe(Terminal);
    });

    it('returns default icon for unknown types', () => {
      expect(getLearningContentTypeIcon('UNKNOWN_TYPE' as unknown as 'TUTORIAL')).toBe(BookOpen);
    });
  });

  describe('getLearningProgressStatusLabel', () => {
    it('returns correct label for IN_PROGRESS and COMPLETED', () => {
      expect(getLearningProgressStatusLabel('IN_PROGRESS')).toBe('Đang học');
      expect(getLearningProgressStatusLabel('COMPLETED')).toBe('Đã hoàn thành');
    });
  });

  describe('getLearningProgressStatusClassName', () => {
    it('returns correct class string for IN_PROGRESS', () => {
      expect(getLearningProgressStatusClassName('IN_PROGRESS')).toContain('bg-primary-100');
    });

    it('returns correct class string for COMPLETED', () => {
      expect(getLearningProgressStatusClassName('COMPLETED')).toContain('bg-success-100');
    });
  });

  describe('getLearningContentFallbackTitle', () => {
    it('formats string by replacing - and _ and capitalizing words', () => {
      expect(getLearningContentFallbackTitle('java-spring_boot')).toBe('Java Spring Boot');
    });

    it('handles empty, null, or undefined', () => {
      expect(getLearningContentFallbackTitle(null)).toBe('Unknown Content');
      expect(getLearningContentFallbackTitle(undefined)).toBe('Unknown Content');
      expect(getLearningContentFallbackTitle('   ')).toBe('Unknown Content');
    });
  });

  describe('buildLearningContentRoute', () => {
    it('returns backendRoute if provided', () => {
      expect(buildLearningContentRoute('TUTORIAL', 'test-key', '/custom-route')).toBe('/custom-route');
    });

    it('builds route from type and encoded key', () => {
      expect(buildLearningContentRoute('TUTORIAL', 'java spring')).toBe('/tutorials/java%20spring');
      expect(buildLearningContentRoute('PROJECT', 'my-project')).toBe('/projects/my-project');
      expect(buildLearningContentRoute('ROADMAP', 'my-roadmap')).toBe('/roadmaps/my-roadmap');
      expect(buildLearningContentRoute('DEVOPS_PHASE', 'plan')).toBe('/devops/plan');
    });

    it('returns null if contentKey is invalid', () => {
      expect(buildLearningContentRoute('TUTORIAL', null)).toBeNull();
      expect(buildLearningContentRoute('TUTORIAL', '   ')).toBeNull();
      expect(buildLearningContentRoute('UNKNOWN' as unknown as 'TUTORIAL', 'key')).toBeNull();
    });
  });

  describe('formatLearningLastAccessedAt', () => {
    it('returns fallback string for null or undefined', () => {
      expect(formatLearningLastAccessedAt(null)).toBe('Chưa truy cập');
      expect(formatLearningLastAccessedAt(undefined)).toBe('Chưa truy cập');
    });

    it('returns "Ngày không hợp lệ" for invalid dates', () => {
      expect(formatLearningLastAccessedAt('invalid-date')).toBe('Ngày không hợp lệ');
      expect(formatLearningLastAccessedAt('')).toBe('Chưa truy cập');
    });

    it('formats a valid ISO date securely without throwing', () => {
      const result = formatLearningLastAccessedAt('2023-10-01T12:00:00Z');
      expect(result).not.toBe('Ngày không hợp lệ');
      expect(result).not.toBe('Chưa truy cập');
      // Just check it contains year or day depending on locale, which should be vi-VN
      expect(result).toContain('2023');
    });
  });
});
