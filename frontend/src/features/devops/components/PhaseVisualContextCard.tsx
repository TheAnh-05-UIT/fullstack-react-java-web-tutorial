import { useState } from 'react';
import {
  Sparkles, Terminal, Check, CheckCircle2, AlertCircle, Copy,
} from 'lucide-react';
import type { PhaseData } from '../types/devops.types';

// Renders the stage-specific GUI preview (Jira board, GitHub PR, Jenkins pipeline, etc.)
function renderGuiPreview(data: PhaseData) {
  switch (data.id) {
    case 'plan':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-blue-500/20 border border-blue-500/40 text-blue-300 font-bold">Jira Software</span>
              <span className="px-2 py-0.5 rounded bg-sky-500/20 border border-sky-500/40 text-sky-300 font-bold">Azure Boards</span>
            </div>
            <span className="text-[11px] font-mono text-slate-400">Sprint 24 Board · Velocity Target: 64 SP</span>
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
              <div className="bg-blue-950/40 rounded-lg p-2 space-y-1 border-l-2 border-blue-500 shadow-sm">
                <div className="font-semibold text-white">JIRA-101: CI/CD Pipeline Gating</div>
                <div className="flex items-center justify-between text-[10px] text-blue-300">
                  <span>In Progress</span>
                  <span className="bg-blue-500/30 px-1 rounded font-mono font-bold">8 SP</span>
                </div>
              </div>
            </div>
            <div className="bg-white/5 rounded-xl p-2.5 space-y-2 border border-white/10">
              <div className="text-[10px] font-bold uppercase tracking-wider text-emerald-400 flex justify-between">
                <span>Confluence PRD</span><span className="bg-emerald-500/20 text-emerald-300 px-1.5 py-0.2 rounded">SLO</span>
              </div>
              <div className="bg-emerald-950/30 rounded-lg p-2 space-y-1 border border-emerald-500/30 text-[10px] text-emerald-300 leading-tight">
                <div className="font-bold mb-1">Acceptance Criteria:</div>
                <div>✔ SonarQube gate must score A</div>
                <div>✔ Rollback under 30s on failure</div>
              </div>
            </div>
          </div>
          <div className="flex items-center justify-between pt-1 text-[11px] text-slate-400">
            <span className="flex items-center gap-1.5"><Check className="w-3.5 h-3.5 text-emerald-400" /> Trello Kanban & Confluence Docs Synced</span>
            <span className="text-blue-400 font-semibold cursor-pointer hover:underline">View Roadmap ➔</span>
          </div>
        </div>
      );

    case 'code':
      return (
        <div className="p-5 space-y-3.5 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-purple-500/20 border border-purple-500/40 text-purple-300 font-bold">GitHub PR #142</span>
              <span className="px-2 py-0.5 rounded bg-indigo-500/20 border border-indigo-500/40 text-indigo-300 font-bold">GitLab MR</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/50 px-2 py-0.5 rounded border border-emerald-500/30">Branch Protected</span>
          </div>
          <div className="bg-white/5 rounded-xl p-3 border border-white/10 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-slate-200">feat(auth): add RSA-256 JWT token generation</span>
              <span className="font-mono text-[11px] text-slate-400">feat/user-auth ➔ main</span>
            </div>
            <div className="font-mono text-[11px] bg-slate-900/90 p-2 rounded-lg border border-white/5 space-y-1">
              <div className="text-rose-400">- const token = jwt.sign(payload, secret);</div>
              <div className="text-emerald-400">+ const token = await JwtProvider.generateRsa256(user, {"{ expiresIn: '15m' }"});</div>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-2.5 pt-0.5">
            <div className="bg-emerald-950/40 border border-emerald-500/40 rounded-xl p-2.5 flex items-center gap-2.5">
              <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
              <div>
                <div className="font-bold text-emerald-300">SonarQube Quality Gate</div>
                <div className="text-[10px] text-emerald-400/80">Passed · 0 Bugs · A Rating</div>
              </div>
            </div>
            <div className="bg-purple-950/40 border border-purple-500/40 rounded-xl p-2.5 flex items-center gap-2.5">
              <Sparkles className="w-5 h-5 text-purple-400 shrink-0" />
              <div>
                <div className="font-bold text-purple-300">Peer Reviews (2/2)</div>
                <div className="text-[10px] text-purple-400/80">Approved by @sre-lead, @arch</div>
              </div>
            </div>
          </div>
        </div>
      );

    case 'build':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-violet-500/20 border border-violet-500/40 text-violet-300 font-bold">Jenkins Pipeline</span>
              <span className="px-2 py-0.5 rounded bg-fuchsia-500/20 border border-fuchsia-500/40 text-fuchsia-300 font-bold">Maven & Gradle</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/40">BUILD #2481 · SUCCESS</span>
          </div>
          <div className="grid grid-cols-4 gap-2 text-center font-mono text-[11px]">
            {['1. Checkout', '2. Maven', '3. Gradle', '4. Docker'].map((s, i) => (
              <div key={i} className="bg-emerald-950/50 border border-emerald-500/50 rounded-lg p-2 space-y-1">
                <div className="font-bold text-emerald-300">{s}</div>
                <div className="text-[10px] text-emerald-400/80">{['Git main (4s)', 'mvn package (32s)', 'gradle build (24s)', 'build image (14s)'][i]}</div>
              </div>
            ))}
          </div>
          <div className="bg-slate-900/90 rounded-xl p-3 font-mono text-[11px] text-slate-300 border border-white/10 space-y-1">
            <div className="text-violet-400">[Jenkins] Running Maven Clean Package -Pproduction</div>
            <div>[INFO] Building Spring Boot Backend v2.4.0... <span className="text-emerald-400">SUCCESS</span></div>
            <div>[INFO] Packaging Docker image: <span className="text-sky-300 font-bold">devopsbuilder/payment-service:v2.4.0</span></div>
          </div>
        </div>
      );

    case 'test':
      return (
        <div className="p-5 space-y-3.5 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-purple-500/20 border border-purple-500/40 text-purple-300 font-bold">SonarQube Quality Gate</span>
              <span className="px-2 py-0.5 rounded bg-pink-500/20 border border-pink-500/40 text-pink-300 font-bold">Cucumber BDD & Selenium</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/40">GATE PASSED ✅</span>
          </div>
          <div className="grid grid-cols-4 gap-2 text-center">
            {[['A', 'Reliability (0 Bugs)', 'text-emerald-400'], ['A', 'Security (0 Hotspots)', 'text-emerald-400'], ['94.8%', 'Code Coverage', 'text-purple-400'], ['0.0%', 'Duplication', 'text-sky-400']].map(([val, label, cls], i) => (
              <div key={i} className="bg-white/5 border border-white/10 rounded-xl p-2">
                <div className={`text-xl font-black ${cls}`}>{val}</div>
                <div className="text-[10px] text-slate-400">{label}</div>
              </div>
            ))}
          </div>
          <div className="bg-slate-900/90 rounded-xl p-3 font-mono text-[11px] border border-white/10 space-y-1.5 text-slate-300">
            <div className="flex items-center justify-between text-emerald-400"><span>✔ Given user is on checkout page with 3 items</span><span>(42ms)</span></div>
            <div className="flex items-center justify-between text-emerald-400"><span>✔ When Selenium driver clicks submit-payment</span><span>(110ms)</span></div>
            <div className="flex items-center justify-between text-emerald-400 font-bold"><span>✔ Then SonarQube confirms 0 security flaws</span><span>(15ms)</span></div>
          </div>
        </div>
      );

    case 'release':
      return (
        <div className="p-5 space-y-4 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-pink-500/20 border border-pink-500/40 text-pink-300 font-bold">Spinnaker CD</span>
              <span className="px-2 py-0.5 rounded bg-rose-500/20 border border-rose-500/40 text-rose-300 font-bold">AWS CodePipeline</span>
            </div>
            <span className="text-[11px] font-mono text-amber-300 bg-amber-950/60 px-2 py-0.5 rounded border border-amber-500/40 animate-pulse">⏳ APPROVAL GATE REQUIRED</span>
          </div>
          <div className="grid grid-cols-3 gap-2 text-center font-mono text-[11px]">
            <div className="bg-emerald-950/50 border border-emerald-500/50 rounded-lg p-2"><div className="font-bold text-emerald-300">1. Tag Artifact</div><div className="text-[10px] text-emerald-400/80">v2.4.0 Immutable</div></div>
            <div className="bg-emerald-950/50 border border-emerald-500/50 rounded-lg p-2"><div className="font-bold text-emerald-300">2. Canary 10%</div><div className="text-[10px] text-emerald-400/80">Health: 98.4% ✅</div></div>
            <div className="bg-amber-950/60 border border-amber-500/60 rounded-lg p-2"><div className="font-bold text-amber-300">3. Promote 100%</div><div className="text-[10px] text-amber-400">Awaiting Approval</div></div>
          </div>
          <div className="bg-gradient-to-r from-pink-950/60 to-rose-950/60 border border-pink-500/40 rounded-xl p-3.5 space-y-2.5">
            <div className="flex items-center gap-2 font-bold text-rose-200">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
              <span>AWS CodePipeline Manual Judgment Gate</span>
            </div>
            <p className="text-[11px] text-slate-300 leading-relaxed">Canary deployment healthy across EKS cluster (Score: 98.4% Datadog APM). Authorize promotion to 100% production traffic?</p>
            <div className="flex gap-2.5 pt-1">
              <span className="flex-1 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold py-1.5 rounded-lg text-center shadow-md border border-emerald-400/30 cursor-pointer hover:brightness-110 transition">✔ Approve Promotion</span>
              <span className="bg-white/10 hover:bg-white/20 text-slate-300 font-semibold px-3 py-1.5 rounded-lg text-center transition cursor-pointer">🛑 Rollback</span>
            </div>
          </div>
        </div>
      );

    case 'deploy':
      return (
        <div className="p-5 space-y-3.5 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-orange-500/20 border border-orange-500/40 text-orange-300 font-bold">Kubernetes & Docker</span>
              <span className="px-2 py-0.5 rounded bg-amber-500/20 border border-amber-500/40 text-amber-300 font-bold">ArgoCD & Terraform</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/40">SYNCED & HEALTHY</span>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-3 space-y-2.5">
            <div className="flex items-center justify-between text-slate-200">
              <span className="font-bold">Deployment: payment-service-prod</span>
              <span className="font-mono text-[11px] text-orange-400">ReplicaSet: 4 / 4 Running</span>
            </div>
            <div className="grid grid-cols-2 gap-2 font-mono text-[10px]">
              {['payment-api-7b89f-2xq1', 'payment-api-7b89f-8lk2', 'payment-api-7b89f-9zz3', 'payment-api-7b89f-p419'].map(pod => (
                <div key={pod} className="bg-emerald-950/40 border border-emerald-500/40 rounded-lg p-2 flex items-center justify-between">
                  <span className="text-emerald-300 truncate">{pod}</span>
                  <span className="text-emerald-400 font-bold">Running</span>
                </div>
              ))}
            </div>
          </div>
          <div className="flex items-center justify-between text-[11px] text-slate-400 px-1">
            <span>🏗️ Provisioned via Terraform on AWS EKS</span>
            <span>⚙️ Configured by Ansible Playbooks</span>
          </div>
        </div>
      );

    case 'operate':
      return (
        <div className="p-5 space-y-3.5 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-amber-500/20 border border-amber-500/40 text-amber-300 font-bold">Ansible & Chef</span>
              <span className="px-2 py-0.5 rounded bg-yellow-500/20 border border-yellow-500/40 text-yellow-300 font-bold">Red Hat OpenShift</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/40">FLEET ONLINE 12/12</span>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-3 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="font-bold text-slate-200">OpenShift Worker Fleet Status</span>
              <span className="font-mono text-[11px] text-amber-400">TASK: Rolling Security Patch</span>
            </div>
            <div className="grid grid-cols-4 gap-2 font-mono text-[10px] text-center">
              <div className="bg-emerald-950/50 border border-emerald-500/40 rounded p-1.5 text-emerald-300">Nodes 01-03 ✅</div>
              <div className="bg-emerald-950/50 border border-emerald-500/40 rounded p-1.5 text-emerald-300">Nodes 04-06 ✅</div>
              <div className="bg-emerald-950/50 border border-emerald-500/40 rounded p-1.5 text-emerald-300">Nodes 07-09 ✅</div>
              <div className="bg-amber-950/60 border border-amber-500/50 rounded p-1.5 text-amber-300 animate-pulse">Nodes 10-12 🔄</div>
            </div>
          </div>
          <div className="bg-slate-900/90 rounded-xl p-2.5 font-mono text-[11px] text-slate-300 border border-white/10 space-y-1">
            <div className="text-amber-400">TASK [ansible.builtin.service : Verify Chef & Puppet compliance agents]</div>
            <div className="text-emerald-400">ok: [worker-node-01..09] =&gt; {'{"changed": false, "status": "active"}'}</div>
            <div className="text-sky-300">changed: [worker-node-10..12] =&gt; {'{"status": "patching_cve_kernel"}'}</div>
          </div>
        </div>
      );

    case 'monitor':
    default:
      return (
        <div className="p-5 space-y-3.5 font-sans text-xs">
          <div className="flex items-center justify-between border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 font-bold">Prometheus & Grafana</span>
              <span className="px-2 py-0.5 rounded bg-teal-500/20 border border-teal-500/40 text-teal-300 font-bold">Datadog & Splunk</span>
            </div>
            <span className="text-[11px] font-mono text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/40">SLO: 99.98% HEALTHY</span>
          </div>
          <div className="grid grid-cols-3 gap-2.5 text-center">
            {[['99.98%', 'Availability SLO', 'text-emerald-400'], ['14ms', 'P95 Latency', 'text-sky-400'], ['0.01%', 'Error Rate', 'text-emerald-400']].map(([v, l, c], i) => (
              <div key={i} className="bg-white/5 border border-white/10 rounded-xl p-2.5">
                <div className={`text-lg font-black ${c}`}>{v}</div>
                <div className="text-[10px] text-slate-400">{l}</div>
              </div>
            ))}
          </div>
          <div className="bg-slate-900/90 rounded-xl p-3 border border-white/10 space-y-2">
            <div className="flex items-center justify-between font-mono text-[11px]">
              <span className="text-slate-300 font-bold">HTTP Request Throughput</span>
              <span className="text-emerald-400">4,280 req/sec</span>
            </div>
            <div className="w-full bg-white/10 rounded-full h-3 overflow-hidden flex">
              <div className="bg-gradient-to-r from-emerald-500 to-teal-400 h-full w-[85%]" />
              <div className="bg-amber-400 h-full w-[12%]" />
              <div className="bg-rose-500 h-full w-[3%]" />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 font-mono">
              <span>🟢 2xx OK (85%)</span><span>🟡 3xx (12%)</span><span>🔴 5xx (&lt; 0.01%)</span>
            </div>
          </div>
        </div>
      );
  }
}

interface Props {
  data: PhaseData;
  copySnippet: () => void;
  copied: boolean;
}

// Interactive card with two tabs: "GUI Preview" and "Raw YAML/Code" for each phase
export function PhaseVisualContextCard({ data, copySnippet, copied }: Props) {
  const [activeTab, setActiveTab] = useState<'gui' | 'code'>('gui');

  return (
    <div className="bg-slate-950/95 backdrop-blur-xl rounded-2xl border border-white/15 shadow-2xl overflow-hidden">
      {/* Tab Bar */}
      <div className="flex items-center justify-between px-4 py-2.5 border-b border-white/10 bg-white/5">
        <div className="flex items-center gap-2">
          {(['gui', 'code'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                activeTab === tab
                  ? 'bg-gradient-to-r from-primary-600 to-indigo-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'
              }`}
            >
              {tab === 'gui' ? <><Sparkles className="w-3.5 h-3.5" /> Interactive Tool GUI</> : <><Terminal className="w-3.5 h-3.5" /> Raw YAML / Code</>}
            </button>
          ))}
        </div>
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

      {/* Content */}
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
