import { TrendingUp, Clock, Sparkles, BookOpen, Zap, ArrowRight } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

interface Props {
  data: PhaseData;
}

const CATEGORY_CONFIG = {
  'Core Fundamentals': {
    label: 'Core Fundamentals',
    icon: BookOpen,
    accent: 'from-sky-500 to-blue-600',
    badge: 'bg-sky-50 dark:bg-sky-950/50 text-sky-700 dark:text-sky-300 border-sky-200 dark:border-sky-800',
    glow: 'shadow-sky-500/20',
  },
  'Advanced Practices': {
    label: 'Advanced Practices',
    icon: Zap,
    accent: 'from-violet-500 to-purple-600',
    badge: 'bg-violet-50 dark:bg-violet-950/50 text-violet-700 dark:text-violet-300 border-violet-200 dark:border-violet-800',
    glow: 'shadow-violet-500/20',
  },
} as const;

// Step-by-step learning roadmap — clean informational display, no tracking
export function LearningPathSection({ data }: Props) {
  const { theme: t } = data;
  const totalSteps = data.learningPath.length;

  // Split into two columns for visual balance
  const half = Math.ceil(totalSteps / 2);
  const leftSteps = data.learningPath.slice(0, half);
  const rightSteps = data.learningPath.slice(half);

  return (
    <section id="learning-path" className="py-20 bg-white dark:bg-slate-950">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Header */}
        <div className="text-center mb-14">
          <div className={`inline-flex items-center gap-2 ${t.badgeBg} ${t.badgeText} text-xs font-bold px-3 py-1 rounded-full mb-4 border`}>
            <TrendingUp className="w-3.5 h-3.5" /> Learning Path
          </div>
          <h2 className="text-4xl sm:text-5xl font-black text-slate-900 dark:text-white mb-4">
            Step-by-Step Roadmap
          </h2>
          <p className="text-base text-slate-500 dark:text-slate-400 max-w-2xl mx-auto">
            A verified <strong className="text-slate-700 dark:text-slate-300">{totalSteps}-step</strong> curriculum to master the{' '}
            <strong className="text-slate-700 dark:text-slate-300">{data.name}</strong> phase — structured from fundamentals to advanced production practices.
          </p>
        </div>

        {/* Two-column step grid */}
        <div className="grid lg:grid-cols-2 gap-6">
          {[leftSteps, rightSteps].map((column, colIdx) => (
            <div key={colIdx} className="space-y-5">
              {column.map((step, localIdx) => {
                const globalIdx = colIdx === 0 ? localIdx : half + localIdx;
                const cat = CATEGORY_CONFIG[step.category];
                const CatIcon = cat.icon;
                const isLast = globalIdx === totalSteps - 1;

                return (
                  <div
                    key={globalIdx}
                    className="group relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl overflow-hidden hover:shadow-xl hover:-translate-y-0.5 transition-all duration-300"
                  >
                    {/* Top color bar */}
                    <div className={`h-1 w-full bg-gradient-to-r ${cat.accent}`} />

                    <div className="p-6">
                      {/* Step number + category */}
                      <div className="flex items-start justify-between gap-3 mb-4">
                        <div className="flex items-center gap-3">
                          {/* Big step number bubble */}
                          <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${cat.accent} flex flex-col items-center justify-center shrink-0 shadow-lg ${cat.glow}`}>
                            <span className="text-white/70 text-[9px] font-bold leading-none">STEP</span>
                            <span className="text-white text-lg font-black leading-none">{globalIdx + 1}</span>
                          </div>
                          {/* Category badge */}
                          <span className={`inline-flex items-center gap-1.5 text-[10px] px-2.5 py-1 rounded-full font-bold border ${cat.badge}`}>
                            <CatIcon className="w-3 h-3" />
                            {cat.label}
                          </span>
                        </div>

                        {/* Duration */}
                        <span className="flex items-center gap-1 text-xs text-slate-400 font-mono shrink-0 mt-1">
                          <Clock className="w-3.5 h-3.5" />
                          {step.duration}
                        </span>
                      </div>

                      {/* Title */}
                      <h3 className="font-black text-lg text-slate-900 dark:text-white leading-snug mb-2 group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-slate-900 group-hover:to-slate-600 dark:group-hover:from-white dark:group-hover:to-slate-300 transition-all">
                        {step.title}
                      </h3>

                      {/* Description */}
                      <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed mb-4">
                        {step.description}
                      </p>

                      {/* Key Takeaway */}
                      <div className="flex items-start gap-2.5 bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800/50 rounded-xl px-3.5 py-3">
                        <Sparkles className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
                        <div>
                          <span className="block text-[9px] font-black uppercase tracking-widest text-amber-600 dark:text-amber-400 mb-0.5">
                            Key Takeaway
                          </span>
                          <span className="text-xs text-amber-800 dark:text-amber-200 leading-relaxed">
                            {step.keyTakeaway}
                          </span>
                        </div>
                      </div>

                      {/* Last step CTA */}
                      {isLast && (
                        <div className={`mt-4 flex items-center gap-2 text-xs font-bold ${t.accentColor}`}>
                          <ArrowRight className="w-4 h-4" />
                          Continue to Practice Labs →
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          ))}
        </div>

      </div>
    </section>
  );
}
