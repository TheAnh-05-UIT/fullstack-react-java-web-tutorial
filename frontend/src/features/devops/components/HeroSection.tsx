import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ClipboardList, Code, Hammer, FlaskConical, Package, Rocket,
  Settings, Activity, Sparkles,
} from 'lucide-react';
import type { PhaseData } from '../types/devops.types';
import { PhaseVisualContextCard } from './PhaseVisualContextCard';

export const LIFECYCLE_STAGES = [
  { stageNumber: 1, id: 'plan',    label: 'Plan',    icon: ClipboardList, color: 'from-blue-500 to-sky-400' },
  { stageNumber: 2, id: 'code',    label: 'Code',    icon: Code,          color: 'from-purple-500 to-indigo-400' },
  { stageNumber: 3, id: 'build',   label: 'Build',   icon: Hammer,        color: 'from-violet-500 to-fuchsia-400' },
  { stageNumber: 4, id: 'test',    label: 'Test',    icon: FlaskConical,  color: 'from-pink-500 to-purple-400' },
  { stageNumber: 5, id: 'release', label: 'Release', icon: Package,       color: 'from-rose-500 to-pink-400' },
  { stageNumber: 6, id: 'deploy',  label: 'Deploy',  icon: Rocket,        color: 'from-orange-500 to-amber-400' },
  { stageNumber: 7, id: 'operate', label: 'Operate', icon: Settings,      color: 'from-amber-500 to-yellow-400' },
  { stageNumber: 8, id: 'monitor', label: 'Monitor', icon: Activity,      color: 'from-emerald-500 to-teal-400' },
];

const PHASE_ICONS = [ClipboardList, Code, Hammer, FlaskConical, Package, Rocket, Settings, Activity];

// Banner section at the top of each phase page: title, description, 8-stage stepper, and tool GUI card
export function HeroSection({ data }: { data: PhaseData }) {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const { theme: t } = data;
  const Icon = PHASE_ICONS[(data.stageNumber - 1) % 8];

  function copySnippet() {
    navigator.clipboard.writeText(data.heroSnippet);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  console.log('HeroSection rendering with gradient:', t.gradient);

  return (
    <section className={`bg-gradient-to-br ${t.gradient} text-white`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 sm:py-20">
        <div className="grid lg:grid-cols-[1.15fr_1fr] gap-10 lg:gap-12 items-center">
          {/* Left: Text & 8-Stage Stepper */}
          <div className="space-y-6">
            {/* Stage badge */}
            <div className="inline-flex items-center gap-1.5 bg-white/20 backdrop-blur-sm border border-white/30 rounded-full px-3 py-1 text-xs font-bold">
              <span className="bg-white/30 rounded-full w-5 h-5 flex items-center justify-center text-[10px] font-black">
                {String(data.stageNumber).padStart(2, '0')}
              </span>
              Stage {data.stageNumber} of 8 · DevOps Lifecycle
            </div>

            {/* Heading */}
            <div>
              <div className="flex items-start gap-4 mb-4">
                <div className={`w-12 h-12 mt-1 shrink-0 bg-gradient-to-br ${LIFECYCLE_STAGES[(data.stageNumber - 1) % 8].color} border border-white/20 rounded-xl flex items-center justify-center shadow-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
                <h1 className="text-2xl sm:text-3xl lg:text-4xl leading-[1.2] font-black tracking-tight">{data.name}</h1>
              </div>
              <p className="text-base sm:text-lg text-white/85 leading-relaxed max-w-xl">{data.tagline}</p>
            </div>

            {/* 8-Stage Lifecycle Stepper - Left Side */}
            <div className="pt-2">
              <div className="text-xs font-bold uppercase tracking-wider text-white/90 mb-4 flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-amber-300 shrink-0" />
                <span>DevOps Lifecycle Roadmap</span>
              </div>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {LIFECYCLE_STAGES.map((s) => {
                  const isActive = data.id === s.id;
                  const IconComp = s.icon;
                  return (
                    <button
                      key={s.id}
                      onClick={() => navigate(`/devops/${s.id}`)}
                      className={`group relative flex items-center gap-2 px-2 py-2.5 rounded-xl border font-mono transition-all duration-300 text-left cursor-pointer ${
                        isActive
                          ? 'bg-white text-slate-900 border-white shadow-xl shadow-black/25 ring-4 ring-white/40 font-black scale-[1.04] z-10'
                          : 'bg-white/10 hover:bg-white/20 border-white/15 text-white/80 hover:text-white'
                      }`}
                      title={`Navigate to Stage ${s.stageNumber}: ${s.label}`}
                    >
                      <div
                        className={`w-7 h-7 rounded-lg flex items-center justify-center shrink-0 transition-transform group-hover:scale-110 ${
                          isActive
                            ? `bg-gradient-to-br ${s.color} text-white shadow-sm font-black`
                            : 'bg-white/15 text-white/75 group-hover:bg-white/25 group-hover:text-white'
                        }`}
                      >
                        <IconComp className="w-4 h-4" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className={`text-[10px] leading-none ${isActive ? 'text-slate-400 font-bold' : 'text-white/60'}`}>
                          0{s.stageNumber}
                        </div>
                        <div className={`text-[11px] sm:text-xs truncate leading-tight mt-0.5 ${isActive ? 'font-black text-slate-900' : 'font-semibold text-white/90'}`}>
                          {s.label}
                        </div>
                      </div>
                      {isActive && (
                        <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse shrink-0 shadow-[0_0_8px_#10b981]" />
                      )}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Right: Interactive Tool GUI / Code card */}
          <div className="relative min-w-0">
            <PhaseVisualContextCard data={data} copySnippet={copySnippet} copied={copied} />
            <div className="absolute -bottom-6 -right-6 w-48 h-48 bg-white/10 rounded-full blur-3xl pointer-events-none" />
          </div>
        </div>

      </div>
    </section>
  );
}
