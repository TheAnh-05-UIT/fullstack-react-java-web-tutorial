export interface PhaseTheme {
  gradient: string;
  iconBg: string;
  badgeBg: string;
  badgeText: string;
  borderColor: string;
  accentColor: string;
  ctaBg: string;
  ctaText: string;
}

export interface CurriculumItem {
  id: string;
  title: string;
  category: 'Core Fundamentals' | 'Advanced Practices';
  duration: string;
  level: 'Beginner' | 'Intermediate' | 'Advanced' | 'Enterprise';
  description: string;
  tags: string[];
  objectives?: string[];
}

export interface ToolItem {
  name: string;
  category: 'Version Control' | 'CI/CD' | 'Testing & QA' | 'Artifacts & Containers' | 'Orchestration & IaC' | 'Observability & SRE' | 'Agile Planning' | 'Security';
  description: string;
  industryStandard: boolean;
  documentationUrl?: string;
  internalLink?: string;
}

export interface LearningStep {
  stepNumber: number;
  title: string;
  duration: string;
  category: 'Core Fundamentals' | 'Advanced Practices';
  description: string;
  keyTakeaway: string;
}

export interface QuizQuestion {
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
  difficulty: 'Beginner' | 'Intermediate' | 'Advanced';
}

export interface PracticeLab {
  id: string;
  title: string;
  tabTitle: string;
  level: 'Beginner' | 'Intermediate' | 'Advanced' | 'Enterprise';
  duration: string;
  difficulty: string;
  prerequisites: string;
  desc: string;
  objectives: string[];
  codeSnippet?: string;
  snippetLabel?: string;
}

export interface PhaseNavInfo {
  slug: string;
  label: string;
  sublabel: string;
}

export interface PhaseData {
  id: string;
  name: string;
  slug: string;
  stageNumber: number;
  tagline: string;
  summary: string;
  heroSnippetTitle: string;
  heroSnippet: string;
  theme: PhaseTheme;
  curriculum: CurriculumItem[];
  tools: ToolItem[];
  learningPath: LearningStep[];
  quiz: QuizQuestion[];
  handsOnLabs: PracticeLab[];
  prevNav?: PhaseNavInfo;
  nextNav?: PhaseNavInfo;
}
