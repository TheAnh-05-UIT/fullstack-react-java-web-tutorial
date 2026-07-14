import { useNavigate } from 'react-router-dom';
import { Home, ChevronRight, BookOpen, Layers, TrendingUp, Terminal } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

const SECTIONS = [
  { id: 'curriculum',    label: 'Curriculum',    icon: BookOpen },
  { id: 'tools',         label: 'Tools',         icon: Layers },
  { id: 'learning-path', label: 'Learning Path', icon: TrendingUp },
  { id: 'labs',          label: 'Practice Labs', icon: Terminal },
];

interface Props {
  data: PhaseData;
  activeSection: string;
}

// Sticky top navigation bar with breadcrumb and section links
export function StickyNav({ data, activeSection }: Props) {
  const navigate = useNavigate();
  const { theme: t } = data;

  function scrollTo(id: string) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  return (
    <div className="sticky top-16 z-30 bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl border-b border-slate-200 dark:border-slate-800 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-14">
          {/* Breadcrumb */}
          <nav className="hidden sm:flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium shrink-0">
            <button onClick={() => navigate('/')} className="flex items-center gap-1 hover:text-blue-600 transition-colors">
              <Home className="w-3.5 h-3.5" /> Home
            </button>
            <ChevronRight className="w-3.5 h-3.5" />
            <span className="text-slate-700 dark:text-slate-300 font-bold">Stage {data.stageNumber}: {data.name}</span>
          </nav>

          {/* Section links */}
          <div className="flex items-center gap-1 overflow-x-auto scrollbar-none">
            {SECTIONS.map(s => (
              <button
                key={s.id}
                onClick={() => scrollTo(s.id)}
                className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                  activeSection === s.id
                    ? `${t.badgeBg} ${t.badgeText}`
                    : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <s.icon className="w-3.5 h-3.5" />
                {s.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
