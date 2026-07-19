export interface ApiResponse<T> {
  status: 'success' | 'error';
  message: string;
  data: T;
}

export interface PagedResponse<T> {
  content: T[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export type ViewMode = 'public' | 'user' | 'admin';

export type Category = string;

export type Difficulty = 'Beginner' | 'Intermediate' | 'Advanced';

export type ProjectStatus = 'Planned' | 'In Progress' | 'Review' | 'Completed';

export interface Tutorial {
  id: string | number;
  title: string;
  slug?: string;
  description: string;
  // category có thể là string hoặc object tùy API endpoint
  category: Category | { id?: number; name?: string; slug?: string; description?: string };
  coverImage?: string;
  thumbnail?: string;
  author?: Author;
  authorName?: string;
  viewCount?: number;
  views?: number;
  createdAt?: string;
  publishDate?: string;
  readTime?: number;
  content?: string;
  createBy?: string;
  status?: string;
}

export interface Author {
  id: string | number;
  name: string;
  avatar: string;
  role?: string;
}

export interface Project {
  id: string | number;
  title: string;
  slug?: string;
  description: string;
  coverImage?: string;
  thumbnail?: string;
  techStack?: string[];
  difficulty?: Difficulty | string;
  githubUrl?: string;
  demoUrl?: string;
  status?: ProjectStatus | string;
  content?: string;
  createBy?: string;
  authorName?: string;
  createdAt?: string;
  viewCount?: number;
  views?: number;
}

export interface Roadmap {
  id: string | number;
  title: string;
  slug?: string;
  description: string;
  difficulty?: Difficulty | string;
  icon?: string;
  color?: string;
  steps?: RoadmapStep[];
  content?: string;
  coverImage?: string;
  thumbnail?: string;
  createBy?: string;
  authorName?: string;
  createdAt?: string;
  isActive?: boolean;
}

export interface RoadmapStep {
  id: string;
  title: string;
  description: string;
  resources: string[];
  completed?: boolean;
}

export interface DevOpsPhase {
  id: string;
  name: string;
  description: string;
  icon: string;
  tools: string[];
}

export interface AuthUser {
  id: number | string;
  username?: string;
  name?: string;
  email: string;
  role?: string | { id?: number; name?: string };
  avatar?: string;
}

export interface DisplayUser {
  id: string | number;
  name?: string;
  username?: string;
  email: string;
  avatar?: string;
  role?: string | { id?: number; name?: string };
  joinDate?: string;
  createdAt?: string;
  updatedAt?: string;
  coursesCompleted?: number;
  articlesRead?: number;
  projectsFinished?: number;
  learningStreak?: number;
}

export type User = DisplayUser & { status?: string };

export function getRoleName(role?: User['role']): string {
  if (typeof role === 'string') return role;
  if (role && typeof role === 'object' && 'name' in role && typeof role.name === 'string') {
    return role.name;
  }
  return 'USER';
}

export interface Stats {
  tutorials: number;
  projects: number;
  roadmaps: number;
  learners: number;
}

export interface ActivityData {
  date: string;
  value: number;
}

export interface ErrorResponse {
  status?: string | number;
  message?: string;
  error?: string;
  path?: string;
  timestamp?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userLogin: AuthUser;
}

export interface SettingItem {
  key: string;
  value: string;
}

export interface UploadResponse {
  url: string;
  fileName?: string;
  size?: number;
}
