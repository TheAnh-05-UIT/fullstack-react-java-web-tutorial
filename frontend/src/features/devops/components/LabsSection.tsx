import { useState } from 'react';
import { Terminal, Target, CheckCircle2, Copy } from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

// Tabbed practice lab section with code snippet viewer and copy functionality
export function LabsSection({ data }: { data: PhaseData }) {
  const [activeLabIdx, setActiveLabIdx] = useState(0);
  const [copied, setCopied] = useState(false);
  const { theme: t } = data;
  const lab = data.handsOnLabs[activeLabIdx];

  function copySnippet() {
    if (lab?.codeSnippet) {
      navigator.clipboard.writeText(lab.codeSnippet);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  }

  return (
    <section id="labs" className="py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <div className={`inline-flex items-center gap-2 ${t.badgeBg} ${t.badgeText} text-xs font-bold px-3 py-1 rounded-full mb-3 border`}>
            <Terminal className="w-3.5 h-3.5" /> Practice Labs
          </div>
          <h2 className="text-3xl font-black text-slate-900 dark:text-white">Hands-On Project Labs</h2>
          <p className="text-base text-slate-600 dark:text-slate-400 mt-2">
            Real-world production simulation exercises with step-by-step instructions.
          </p>
        </div>

        {/* Lab Tabs */}
        {data.handsOnLabs.length > 1 && (
          <div className="flex flex-wrap gap-3 mb-8">
            {data.handsOnLabs.map((l, idx) => (
              <button
                key={l.id}
                onClick={() => { setActiveLabIdx(idx); setCopied(false); }}
                className={`flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm transition-all ${
                  activeLabIdx === idx
                    ? `${t.iconBg} text-white shadow-lg ring-2 ring-offset-2 ring-slate-300 dark:ring-slate-700`
                    : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800/60'
                }`}
              >
                <span>{l.tabTitle}</span>
                <span className={`text-[10px] px-2 py-0.5 rounded font-bold uppercase ${activeLabIdx === idx ? 'bg-white/20 text-white' : 'bg-slate-100 dark:bg-slate-800 text-slate-500'}`}>
                  {l.level}
                </span>
              </button>
            ))}
          </div>
        )}

        {/* Lab Content */}
        <div className={`rounded-3xl bg-gradient-to-br ${t.gradient} text-white p-8 sm:p-10 shadow-2xl`}>
          <div className="grid lg:grid-cols-12 gap-8 items-start">
            {/* Left: Description */}
            <div className="lg:col-span-6 space-y-5">
              <div className="inline-flex items-center gap-2 bg-white/20 border border-white/30 rounded-full px-3.5 py-1 text-xs font-bold uppercase tracking-wider">
                <Target className="w-3.5 h-3.5" /> {lab?.level} Practice Lab
              </div>
              <h2 className="text-3xl font-black text-white leading-tight">{lab?.title}</h2>
              <p className="text-base text-white/85 leading-relaxed">{lab?.desc}</p>

              <div className="space-y-2">
                <p className="text-xs font-bold text-white/70 uppercase tracking-widest">Lab Objectives:</p>
                {(lab?.objectives || []).map((obj, i) => (
                  <div key={i} className="flex items-start gap-2.5 text-sm text-white/90">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span>{obj}</span>
                  </div>
                ))}
              </div>

              <div className="flex flex-wrap gap-4 pt-2">
                <div className="bg-white/15 rounded-xl px-4 py-3">
                  <div className="text-xs text-white/60 font-mono uppercase">Duration</div>
                  <div className="font-black text-white">{lab?.duration}</div>
                </div>
                <div className="bg-white/15 rounded-xl px-4 py-3">
                  <div className="text-xs text-white/60 font-mono uppercase">Difficulty</div>
                  <div className="font-black text-white text-sm">{lab?.difficulty}</div>
                </div>
              </div>
              <p className="text-xs text-white/60">
                <strong className="text-white/80">Prerequisites:</strong> {lab?.prerequisites}
              </p>
            </div>

            {/* Right: Code Snippet */}
            <div className="lg:col-span-6">
              <div className="bg-slate-950/90 backdrop-blur rounded-2xl border border-white/10 overflow-hidden">
                <div className="flex items-center justify-between px-5 py-3 border-b border-white/10 bg-white/5">
                  <span className="text-xs font-mono text-slate-400">{lab?.snippetLabel || 'lab-instructions.sh'}</span>
                  <button onClick={copySnippet} className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-white bg-white/5 hover:bg-white/10 px-2.5 py-1.5 rounded-lg transition-all">
                    <Copy className="w-3.5 h-3.5" />
                    {copied ? 'Copied!' : 'Copy'}
                  </button>
                </div>
                <pre className="p-5 text-xs leading-relaxed font-mono text-emerald-300/95 overflow-x-auto max-h-80 scrollbar-thin scrollbar-track-transparent scrollbar-thumb-white/10">
                  {lab?.codeSnippet || '# No code snippet available for this lab.'}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
