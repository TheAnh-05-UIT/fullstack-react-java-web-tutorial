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
  // Chuẩn hóa field tên – dùng viewCount (khớp với BE ResponseDTO).
  // Xóa 'views' trùng lặp; 'publishDate' → dùng 'createdAt' từ BE.
  viewCount?: number;
  createdAt?: string;
  readTime: number;
  content?: string;
  createBy?: string;
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

// Tách thành 2 interface rõ ràng để tránh nhầm lẫn:
// - AuthUser: dùng trong AuthContext (dữ liệu từ JWT / backend login response)
// - DisplayUser: dùng để hiển thị profile người dùng trên UI (dữ liệu phong phú hơn)
//
// UserProfile trong AuthContext đã được đồng nhất về đây.
export interface AuthUser {
  id: number;       // BE trả Long → JSON number
  username: string; // tên hiển thị
  email: string;
  role?: string;    // 'ADMIN' | 'USER' sau khi đã strip prefix 'ROLE_'
  avatar?: string;
}

// DisplayUser: dữ liệu mở rộng dùng cho trang profile / dashboard
export interface DisplayUser {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: 'USER' | 'ADMIN'; // uppercase theo convention BE
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
