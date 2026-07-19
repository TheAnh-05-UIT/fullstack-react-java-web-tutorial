import { Clock, BookOpen } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

const LEVEL_STYLE: Record<string, string> = {
  Beginner:     'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300',
  Intermediate: 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300',
  Advanced:     'bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-300',
  Enterprise:   'bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300',
};

// Grid of curriculum module cards for the current phase
export function CurriculumSection({ data }: { data: PhaseData }) {
  const { theme: t } = data;

  return (
    <section id="curriculum" className="py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-end justify-between gap-4 mb-10">
          <div>
            <div className={`inline-flex items-center gap-2 ${t.badgeBg} ${t.badgeText} text-xs font-bold px-3 py-1 rounded-full mb-3 border`}>
              <BookOpen className="w-3.5 h-3.5" /> Curriculum
            </div>
            <h2 className="text-3xl font-black text-slate-900 dark:text-white">What You Will Learn</h2>
            <p className="text-base text-slate-600 dark:text-slate-400 mt-2">
              {data.curriculum.length} modules covering foundational concepts and advanced practices.
            </p>
          </div>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data.curriculum.map((item) => (
            <div
              key={item.id}
              className="group bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 rounded-2xl p-6 transition-all duration-200 flex flex-col justify-between shadow-sm hover:shadow-md"
            >
              <div>
                <div className="flex flex-wrap items-center gap-2 mb-3">
                  <span className={`text-[10px] px-2.5 py-0.5 rounded-full font-bold uppercase tracking-wider ${LEVEL_STYLE[item.level]}`}>
                    {item.level}
                  </span>
                </div>
                <h4 className="font-bold text-lg leading-snug text-slate-900 dark:text-white mb-3 group-hover:text-emerald-500 dark:group-hover:text-emerald-400 transition-colors">
                  {item.title}
                </h4>
                <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed mb-6">{item.description}</p>
              </div>

              <div className="pt-4 border-t border-slate-100 dark:border-slate-800/80 flex flex-wrap items-center justify-between gap-3 mt-auto">
                <span className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 dark:text-slate-400">
                  <Clock className="w-3.5 h-3.5" /> {item.duration}
                </span>
                <div className="flex flex-wrap gap-1.5">
                  {(item.tags || []).slice(0, 3).map(tag => (
                    <span key={tag} className="text-[11px] px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 font-medium">
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
