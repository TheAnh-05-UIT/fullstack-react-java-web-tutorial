import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

// Compact prev/next phase navigation bar at the bottom of each phase page
export function PhaseNavFooter({ data }: { data: PhaseData }) {
  const navigate = useNavigate();
  const { theme: t } = data;

  function handleNav(slug: string) {
    navigate(slug === 'plan' && !data.prevNav ? '/' : `/devops/${slug}`);
  }

  return (
    <div className="border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid sm:grid-cols-2 divide-y sm:divide-y-0 sm:divide-x divide-slate-200 dark:divide-slate-800">
          {/* Prev */}
          {data.prevNav ? (
            <button
              onClick={() => handleNav(data.prevNav!.slug)}
              className="group py-3.5 sm:pr-6 flex items-center gap-3 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors text-left"
            >
              <div className="w-8 h-8 rounded-lg border border-slate-200 dark:border-slate-700 flex items-center justify-center shrink-0 group-hover:border-slate-400 dark:group-hover:border-slate-500 transition-all">
                <ArrowLeft className="w-4 h-4 text-slate-600 dark:text-slate-400" />
              </div>
              <div className="min-w-0">
                <span className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none mb-0.5">{data.prevNav.sublabel}</span>
                <span className="font-bold text-slate-900 dark:text-white text-sm truncate block">{data.prevNav.label}</span>
              </div>
            </button>
          ) : (
            <div className="py-3.5 sm:pr-6 flex items-center gap-3 opacity-30">
              <div className="w-8 h-8 rounded-lg border border-slate-300 dark:border-slate-700 flex items-center justify-center shrink-0">
                <ArrowLeft className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <span className="block text-[10px] font-bold uppercase tracking-wider leading-none mb-0.5">Start of Cycle</span>
                <span className="font-bold text-sm truncate block">Plan Stage</span>
              </div>
            </div>
          )}

          {/* Next */}
          {data.nextNav && (
            <button
              onClick={() => handleNav(data.nextNav!.slug)}
              className="group py-3.5 sm:pl-6 flex items-center justify-end gap-3 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors text-right"
            >
              <div className="min-w-0">
                <span className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none mb-0.5">{data.nextNav.sublabel}</span>
                <span className={`font-bold text-slate-900 dark:text-white text-sm truncate block group-hover:${t.accentColor} transition-colors`}>{data.nextNav.label}</span>
              </div>
              <div className="w-8 h-8 rounded-lg border border-slate-200 dark:border-slate-700 flex items-center justify-center shrink-0 group-hover:border-slate-400 dark:group-hover:border-slate-500 transition-all">
                <ArrowRight className="w-4 h-4 text-slate-600 dark:text-slate-400" />
              </div>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
