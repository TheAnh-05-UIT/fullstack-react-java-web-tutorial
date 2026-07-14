import { useNavigate } from 'react-router-dom';
import { Layers, ArrowRight, ExternalLink } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';
import { getSpecificToolAppearance } from './getSpecificToolAppearance';

// Grid of Industry-Standard tool cards with authentic brand icons and colors
export function ToolsSection({ data }: { data: PhaseData }) {
  const { theme: t } = data;
  const navigate = useNavigate();

  return (
    <section id="tools" className="py-16 bg-slate-50 dark:bg-slate-900/50 border-y border-slate-200 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-10">
          <div className={`inline-flex items-center gap-2 ${t.badgeBg} ${t.badgeText} text-xs font-bold px-3 py-1 rounded-full mb-3 border`}>
            <Layers className="w-3.5 h-3.5" /> Tools & Ecosystem
          </div>
          <h2 className="text-3xl font-black text-slate-900 dark:text-white">Industry-Standard Tools</h2>
          <p className="text-base text-slate-600 dark:text-slate-400 mt-2">
            The exact tools used by top-tier engineering teams at elite DevOps organizations.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {data.tools.map((tool, idx) => {
            const { icon: ToolIcon, bg: toolBg } = getSpecificToolAppearance(tool.name, tool.category, t.iconBg);
            return (
              <div key={idx} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 flex flex-col hover:border-slate-300 dark:hover:border-slate-700 transition-all hover:-translate-y-0.5 shadow-sm hover:shadow-md">
                {/* Icon + Name */}
                <div className="flex items-start gap-3 mb-4">
                  <div className={`w-11 h-11 rounded-xl ${toolBg} text-white flex items-center justify-center shrink-0`}>
                    {ToolIcon}
                  </div>
                  <div className="min-w-0 flex-1">
                    <h3 className="font-bold text-slate-900 dark:text-white text-base leading-tight truncate">{tool.name}</h3>
                    <span className="text-[10px] text-slate-400 font-mono uppercase truncate block mt-0.5">{tool.category}</span>
                  </div>
                </div>
                <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed flex-1 mb-4">{tool.description}</p>

                {/* Footer */}
                <div className="pt-4 border-t border-slate-100 dark:border-slate-800">
                  {tool.internalLink ? (
                    <button
                      onClick={() => navigate(tool.internalLink!)}
                      className={`flex items-center justify-between w-full text-xs font-bold ${t.accentColor} hover:underline`}
                    >
                      <span>Explore Tutorials</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  ) : (
                    <a
                      href={tool.documentationUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center justify-between w-full text-xs font-bold text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
                    >
                      <span>Official Docs</span>
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
