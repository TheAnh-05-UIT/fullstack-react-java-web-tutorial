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
  category: Category | { id?: number; name?: string; slug?: string; description?: string };
  coverImage: string;
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
  thumbnail?: string;
  coverImage?: string;
  techStack?: string[];
  difficulty?: Difficulty | string;
  githubUrl?: string;
  githubLink?: string;
  demoUrl?: string;
  liveLink?: string;
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
  id: number;
  username: string;
  email: string;
  role?: string;
  avatar?: string;
}

export interface DisplayUser {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: 'USER' | 'ADMIN';
  joinDate: string;
  coursesCompleted: number;
  articlesRead: number;
  projectsFinished: number;
  learningStreak: number;
}

export type User = DisplayUser & { status?: string };

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
