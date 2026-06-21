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
  id: string;
  title: string;
  slug?: string;
  description: string;
  category: Category;
  coverImage: string;
  thumbnail?: string;
  author: Author;
  readTime: number;
  views: number;
  publishDate: string;
  content?: string;
  createBy?: string;
  createdAt?: string;
  viewCount?: number;
  authorName?: string;
}

export interface Author {
  id: string;
  name: string;
  avatar: string;
  role?: string;
}

export interface Project {
  id: string;
  title: string;
  slug?: string;
  description: string;
  thumbnail: string;
  techStack: string[];
  difficulty: Difficulty;
  githubUrl: string;
  demoUrl?: string;
  status?: ProjectStatus;
  content?: string;
  createBy?: string;
  createdAt?: string;
  viewCount?: number;
  authorName?: string;
}

export interface Roadmap {
  id: string;
  title: string;
  slug?: string;
  description: string;
  difficulty?: Difficulty | string;
  icon: string;
  color: string;
  steps: RoadmapStep[];
  content?: string;
  coverImage?: string;
  createBy?: string;
  createdAt?: string;
  authorName?: string;
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

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: 'user' | 'admin';
  joinDate: string;
  coursesCompleted: number;
  articlesRead: number;
  projectsFinished: number;
  learningStreak: number;
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
