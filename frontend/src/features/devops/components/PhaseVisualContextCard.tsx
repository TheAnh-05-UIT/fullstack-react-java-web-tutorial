import { useState } from 'react';
import {
  Sparkles, Copy, Activity, Zap,
  ClipboardList, GitPullRequest, Package, FlaskConical, Rocket, Server, Settings
} from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

// Renders the stage-specific GUI preview (Jira board, GitHub PR, Jenkins pipeline, etc.)
function renderGuiPreview(data: PhaseData) {
  switch (data.id) {
    case 'plan':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <ClipboardList className="w-3.5 h-3.5 text-blue-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              Jira Software · Azure Boards
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-500/10 border border-blue-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-blue-400">SPRINT 24</span>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-2.5">
            <div className="bg-white/5 rounded-xl p-2.5 space-y-2 border border-white/10">
              <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 flex justify-between">
                <span>Backlog</span><span className="bg-white/10 px-1.5 py-0.2 rounded">12</span>
              </div>
              <div className="bg-white/10 rounded-lg p-2 space-y-1 border-l-2 border-amber-500 shadow-sm">
                <div className="font-semibold text-slate-200">EPIC-404: Zero-Downtime Blue/Green</div>
                <div className="flex items-center justify-between text-[10px] text-slate-400">
                  <span className="text-amber-400">⚡ Critical</span>
                  <span className="bg-white/10 px-1 rounded font-mono">13 SP</span>
                </div>
              </div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 space-y-2 border border-white/10">
              <div className="text-[10px] font-bold uppercase tracking-wider text-blue-400 flex justify-between">
                <span>Sprint 24</span><span className="bg-blue-500/20 text-blue-300 px-1.5 py-0.2 rounded">4</span>
              </div>
              <div className="bg-blue-500/10 rounded-lg p-2 space-y-1 border-l-2 border-blue-500 shadow-sm">
                <div className="font-semibold text-blue-200">DEV-102: Config ArgoCD GitOps Sync</div>
                <div className="flex items-center justify-between text-[10px] text-blue-300">
                  <span>In Progress</span>
                  <span className="bg-blue-500/20 px-1 rounded font-mono">8 SP</span>
                </div>
              </div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 space-y-2 border border-white/10">
              <div className="text-[10px] font-bold uppercase tracking-wider text-emerald-400 flex justify-between">
                <span>Done</span><span className="bg-emerald-500/20 text-emerald-300 px-1.5 py-0.2 rounded">28</span>
              </div>
              <div className="bg-emerald-500/10 rounded-lg p-2 space-y-1 border-l-2 border-emerald-500 opacity-80">
                <div className="font-semibold text-emerald-200 line-through">ARCH-01: Kubernetes Cluster Setup</div>
                <div className="text-[10px] text-emerald-400">Completed in Sprint 23</div>
              </div>
            </div>
          </div>
        </div>
      );

    case 'code':
      return (
        <div className="p-5 space-y-4 font-mono text-xs">
          <div className="flex items-center justify-between font-sans">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <GitPullRequest className="w-3.5 h-3.5 text-purple-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              GitHub Pull Request #142
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-400">OPEN</span>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3 text-[11px]">
            <div className="bg-rose-500/10 rounded-lg p-2.5 border border-rose-500/20 text-rose-300">
              <div className="font-bold mb-1">- livenessProbe: httpGet /healthz</div>
              <div>- initialDelaySeconds: 5</div>
            </div>
            <div className="bg-emerald-500/10 rounded-lg p-2.5 border border-emerald-500/20 text-emerald-300">
              <div className="font-bold mb-1">+ livenessProbe: httpGet /api/v1/health</div>
              <div>+ initialDelaySeconds: 15</div>
              <div>+ periodSeconds: 10</div>
            </div>
          </div>
          <div className="flex items-center justify-between pt-2 border-t border-white/10 font-sans text-[11px] text-slate-400">
            <span>2 approvals required · SonarQube Quality Gate: <strong className="text-emerald-400">PASSED</strong></span>
            <span className="text-purple-400 font-bold cursor-pointer hover:underline">Merge pull request →</span>
          </div>
        </div>
      );

    case 'build':
      return (
        <div className="p-5 space-y-4 font-mono text-xs">
          <div className="flex items-center justify-between font-sans">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <Package className="w-3.5 h-3.5 text-amber-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              Docker Multi-Stage
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-400">BUILD #482</span>
              </div>
            </div>
          </div>
          <div className="bg-black/60 rounded-xl p-3 space-y-1.5 text-[11px] text-slate-300 border border-white/5 leading-relaxed">
            <div className="text-slate-500">$ docker build -t devops-app:v2.4.0 --target production .</div>
            <div className="text-cyan-400">[1/2] BUILDER STAGE: eclipse-temurin:21-jdk-alpine</div>
            <div className="text-slate-400">  ---&gt; Compiling 42 Java modules with Maven... <span className="text-emerald-400">[SUCCESS]</span></div>
            <div className="text-amber-400">[2/2] PRODUCTION STAGE: eclipse-temurin:21-jre-alpine (distroless)</div>
            <div className="text-slate-400">  ---&gt; Copying target/app.jar ---&gt; Layer size: 18.4 MB</div>
            <div className="text-emerald-400 font-bold">Successfully tagged devops-app:v2.4.0 (SHA256: 8f4e2b1c)</div>
          </div>
        </div>
      );

    case 'test':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <FlaskConical className="w-3.5 h-3.5 text-emerald-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              JUnit 5 · Jest
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-teal-500/10 border border-teal-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-teal-400">COV: 94.2%</span>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-4 gap-2 text-center">
            <div className="bg-white/5 rounded-xl p-2.5 border border-white/10">
              <div className="text-emerald-400 text-base font-bold">284</div>
              <div className="text-[10px] text-slate-400 uppercase">Unit Tests</div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 border border-white/10">
              <div className="text-emerald-400 text-base font-bold">42</div>
              <div className="text-[10px] text-slate-400 uppercase">Integration</div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 border border-white/10">
              <div className="text-emerald-400 text-base font-bold">16</div>
              <div className="text-[10px] text-slate-400 uppercase">E2E / Cypress</div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 border border-white/10">
              <div className="text-emerald-400 text-base font-bold">0</div>
              <div className="text-[10px] text-slate-400 uppercase">Vulnerabilities</div>
            </div>
          </div>
        </div>
      );

    case 'release':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <Rocket className="w-3.5 h-3.5 text-rose-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              ArgoCD GitOps
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-400">SYNCED</span>
              </div>
            </div>
          </div>
          <div className="bg-white/5 rounded-xl p-3 border border-white/10 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-rose-500/20 border border-rose-500/40 flex items-center justify-center font-bold text-rose-400">v2.4</div>
              <div>
                <div className="font-bold text-white">Release Candidate v2.4.0-GA</div>
                <div className="text-[10px] text-slate-400">Signed artifact by Cosign · Image scan clean</div>
              </div>
            </div>
            <span className="px-2.5 py-1 rounded bg-emerald-500 text-slate-950 font-black text-[11px] shadow">Ready to Promote</span>
          </div>
        </div>
      );

    case 'deploy':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <Server className="w-3.5 h-3.5 text-indigo-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              Kubernetes Update
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-400">3/3 ACTIVE</span>
              </div>
            </div>
          </div>
          <div className="grid grid-cols-3 gap-2.5">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-indigo-500/10 rounded-xl p-2.5 border border-indigo-500/30 space-y-1">
                <div className="flex justify-between items-center text-[10px] text-indigo-300 font-bold">
                  <span>Pod #{i}</span><span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                </div>
                <div className="font-mono text-[10px] text-slate-300">web-app-859b-node-{i}</div>
                <div className="text-[9px] text-emerald-400">HTTP 200 OK · CPU: 12%</div>
              </div>
            ))}
          </div>
        </div>
      );

    case 'operate':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <Settings className="w-3.5 h-3.5 text-slate-400 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              Terraform · Ansible
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-400">DRIFT: CLEAN</span>
              </div>
            </div>
          </div>
          <div className="bg-black/60 rounded-xl p-3 font-mono text-[11px] text-slate-300 border border-white/5 space-y-1">
            <div className="text-slate-500">$ terraform plan -detailed-exitcode</div>
            <div className="text-slate-300">No changes. Your infrastructure matches the configuration.</div>
            <div className="text-emerald-400 font-bold">Terraform has compared your real infrastructure against your configuration and found no differences.</div>
          </div>
        </div>
      );

    case 'monitor':
      return (
        <div className="p-5 space-y-4 font-sans">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-rose-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500"></div>
              <Activity className="w-3.5 h-3.5 text-emerald-500 ml-1.5" />
            </div>
            <div className="text-center font-bold text-slate-100 text-[11px] leading-tight">
              Prometheus · Grafana ·<br />OpenTelemetry
            </div>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-950/40 border border-emerald-500/30 text-[9px] font-bold tracking-wider">
              <Zap className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
              <div className="flex flex-col leading-[1.1] text-left">
                <span className="text-emerald-500/80">ALERTING:</span>
                <span className="text-emerald-400">NORMAL</span>
              </div>
            </div>
          </div>

          {/* Line Chart Area */}
          <div className="relative h-28 bg-[#0f172a] rounded-xl border border-white/5 overflow-hidden flex items-center justify-center">
            <div className="absolute inset-0 opacity-20" style={{ backgroundImage: 'linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px)', backgroundSize: '1rem 1rem' }}></div>
            
            <div className="absolute top-2.5 right-2.5 px-2.5 py-1 rounded-full bg-slate-900/80 border border-white/5 flex items-center gap-1.5 z-10">
              <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 shadow-[0_0_4px_rgba(16,185,129,0.8)]"></div>
              <span className="text-[9px] text-emerald-400 font-bold tracking-wide">Live Telemetry Stream</span>
            </div>

            <svg className="w-full h-full text-emerald-400 drop-shadow-[0_0_8px_rgba(52,211,153,0.6)]" viewBox="0 0 100 30" preserveAspectRatio="none">
              <polyline fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" points="15,15 28,15 31,5 34,25 37,15 50,15 53,5 56,25 59,15 85,15" />
            </svg>
          </div>

          {/* 3 Bottom Cards */}
          <div className="grid grid-cols-3 gap-2.5">
            <div className="bg-white/5 rounded-xl p-3 border border-white/5 flex flex-col justify-center">
              <div className="text-[10px] text-slate-400 font-bold mb-1">P99 Latency</div>
              <div className="text-emerald-400 font-mono text-[13px] font-bold tracking-tight">42ms</div>
            </div>
            <div className="bg-white/5 rounded-xl p-3 border border-white/5 flex flex-col justify-center">
              <div className="text-[10px] text-slate-400 font-bold mb-1">Error Rate</div>
              <div className="text-emerald-400 font-mono text-[13px] font-bold tracking-tight">0.001%</div>
            </div>
            <div className="bg-white/5 rounded-xl p-3 border border-white/5 flex flex-col justify-center">
              <div className="text-[10px] text-slate-400 font-bold mb-1">CPU Saturation</div>
              <div className="text-slate-100 font-mono text-[13px] font-bold tracking-tight">38.4%</div>
            </div>
          </div>
        </div>
      );

    default:
      return null;
  }
}

interface Props {
  data: PhaseData;
  copySnippet: () => void;
  copied: boolean;
}

// Interactive card with two tabs: "GUI Preview" and "Raw YAML/Code" (đã loại bỏ tab Terminal giả lập)
export function PhaseVisualContextCard({ data, copySnippet, copied }: Props) {
  const [activeTab, setActiveTab] = useState<'gui' | 'code'>('gui');

  return (
    <div className="bg-slate-950/95 backdrop-blur-xl rounded-2xl border border-white/15 shadow-2xl overflow-hidden flex flex-col">
      {/* Tab Bar & Admin Action */}
      <div className="flex flex-wrap items-center justify-between px-4 py-2.5 border-b border-white/10 bg-white/5 gap-2">
        <div className="flex items-center gap-1.5 flex-wrap">
          {(['gui', 'code'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                activeTab === tab
                  ? 'bg-gradient-to-r from-primary-600 to-indigo-600 text-white shadow-md ring-1 ring-white/20'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'
              }`}
            >
              {tab === 'gui' && <><Sparkles className="w-3.5 h-3.5 text-sky-400" /> Interactive Tool GUI Preview</>}
              {tab === 'code' && <><Copy className="w-3.5 h-3.5 text-emerald-400" /> Raw YAML / Code Snippet</>}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'code' && (
            <button
              onClick={copySnippet}
              className="flex items-center gap-1.5 text-xs font-semibold text-slate-300 hover:text-white bg-white/10 hover:bg-white/20 px-2.5 py-1 rounded-lg transition-all"
            >
              <Copy className="w-3.5 h-3.5" />
              {copied ? 'Copied!' : 'Copy Snippet'}
            </button>
          )}


        </div>
      </div>

      {/* Content Area */}
      {activeTab === 'gui' ? (
        renderGuiPreview(data)
      ) : (
        <div className="relative">
          <div className="px-5 py-2 border-b border-white/5 bg-white/[0.02] text-xs font-mono text-slate-400">
            {data.heroSnippetTitle}
          </div>
          <pre className="p-5 text-xs leading-relaxed font-mono text-emerald-300/95 overflow-x-auto max-h-80 scrollbar-thin scrollbar-track-transparent scrollbar-thumb-white/10">
            {data.heroSnippet}
          </pre>
        </div>
      )}
    </div>
  );
}
