import type { LearningContentType } from '../types/learningProgress.types';

export function getLearningContentTypeLabel(contentType: LearningContentType): string {
  switch (contentType) {
    case 'TUTORIAL':
      return 'Tutorial';
    case 'PROJECT':
      return 'Project';
    case 'ROADMAP':
      return 'Roadmap';
    case 'DEVOPS_PHASE':
      return 'DevOps Lifecycle';
    default:
      return contentType;
  }
}

export function getLearningContentFallbackTitle(contentKey: string | null | undefined): string {
  if (!contentKey || contentKey.trim().length === 0) {
    return 'Unknown Content';
  }
  
  try {
    const decoded = decodeURIComponent(contentKey);
    // Thay thế - và _ thành khoảng trắng, loại bỏ khoảng trắng thừa
    const formatted = decoded.replace(/[-_]/g, ' ').replace(/\s+/g, ' ').trim();
    // Viết hoa chữ cái đầu của mỗi từ
    return formatted.replace(/\b\w/g, (char) => char.toUpperCase());
  } catch {
    return contentKey;
  }
}

export function buildLearningContentRoute(
  contentType: LearningContentType,
  contentKey: string,
  backendRoute?: string | null
): string | null {
  if (backendRoute && backendRoute.trim().length > 0) {
    return backendRoute;
  }

  const encodedKey = encodeURIComponent(contentKey.trim());
  if (!encodedKey) return null;

  switch (contentType) {
    case 'TUTORIAL':
      return `/tutorials/${encodedKey}`;
    case 'PROJECT':
      return `/projects/${encodedKey}`;
    case 'ROADMAP':
      return `/roadmaps/${encodedKey}`;
    case 'DEVOPS_PHASE':
      return `/devops/${encodedKey}`;
    default:
      return null;
  }
}

export function formatLearningLastAccessedAt(isoDateString: string | null | undefined): string {
  if (!isoDateString) return 'Chưa truy cập';
  
  try {
    const date = new Date(isoDateString);
    if (isNaN(date.getTime())) return 'Ngày không hợp lệ';
    
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch {
    return 'Ngày không hợp lệ';
  }
}
