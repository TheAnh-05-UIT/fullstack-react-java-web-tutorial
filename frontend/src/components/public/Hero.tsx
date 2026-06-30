import { Link } from 'react-router-dom';
import { ArrowRight, Map, Terminal, CheckCircle2, Sparkles, GitBranch, Play } from 'lucide-react';
import { Button } from '../ui';

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-gradient-to-b from-slate-50 via-white to-blue-50/40 dark:from-gray-950 dark:via-gray-900 dark:to-blue-950/20 pt-8 pb-20 lg:pt-12 lg:pb-32">
      {/* Dynamic Background Glows */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 right-10 w-96 h-96 bg-blue-500/20 dark:bg-blue-600/15 rounded-full blur-3xl animate-pulse-slow" />
        <div className="absolute top-1/3 -left-20 w-80 h-80 bg-indigo-500/15 dark:bg-indigo-600/10 rounded-full blur-3xl" />
        <div className="absolute bottom-10 right-1/3 w-72 h-72 bg-purple-500/15 dark:bg-purple-600/10 rounded-full blur-3xl" />
      </div>

      <div className="container-app relative z-10">
        <div className="grid lg:grid-cols-12 gap-12 lg:gap-8 items-center">
          {/* Left Column: Typography & CTAs */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-50 dark:bg-blue-900/40 border border-blue-200/60 dark:border-blue-800/60 text-blue-700 dark:text-blue-300 text-xs font-semibold shadow-sm animate-fade-in">
              <Sparkles className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
              <span>Interactive DevOps Roadmaps & Production Labs</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-gray-900 dark:text-gray-100 leading-[1.15]">
              Learn <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 dark:from-blue-400 dark:via-indigo-400 dark:to-purple-400">DevOps</span>
              <br />
              From Zero To Production
            </h1>

            <p className="text-lg text-gray-600 dark:text-gray-300 max-w-xl leading-relaxed">
              Master CI/CD, Docker, Kubernetes, Terraform, AWS, Monitoring, and Cloud Native technologies through practical real-world tutorials and hands-on production labs.
            </p>

            <div className="pt-2 flex flex-wrap items-center gap-4">
              <Link to="/tutorials">
                <Button size="lg" className="shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40 hover:-translate-y-0.5 transition-all duration-200 font-semibold px-7 py-3.5">
                  Start Learning
                  <ArrowRight className="w-4 h-4 ml-1.5" />
                </Button>
              </Link>
              <Link to="/roadmaps">
                <Button variant="secondary" size="lg" className="border border-gray-300 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800 hover:-translate-y-0.5 transition-all duration-200 font-semibold px-6 py-3.5">
                  <Map className="w-4 h-4 mr-2 text-indigo-600 dark:text-indigo-400" />
                  View Roadmaps
                </Button>
              </Link>
            </div>

          </div>

          {/* Right Column: Stunning Interactive Terminal & Pipeline Simulation */}
          <div className="lg:col-span-6 relative flex items-center justify-center">
            <div className="relative w-full max-w-xl">
              {/* Decorative background glow for card */}
              <div className="absolute -inset-1 bg-gradient-to-r from-blue-600 to-purple-600 rounded-3xl blur-2xl opacity-20 dark:opacity-30 transform -rotate-1" />

              {/* Main Code Terminal Card */}
              <div className="relative rounded-2xl bg-slate-900/95 dark:bg-gray-950/95 border border-slate-800 shadow-2xl overflow-hidden backdrop-blur-xl">
                {/* Terminal Header */}
                <div className="flex items-center justify-between px-4 py-3 bg-slate-800/60 border-b border-slate-800">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-red-500/80" />
                    <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
                    <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
                    <span className="ml-2 text-xs font-mono text-slate-400 flex items-center gap-1.5">
                      <Terminal className="w-3.5 h-3.5 text-blue-400" /> deploy-production.yml
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-[11px] font-mono font-medium">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping" />
                    Pipeline Running
                  </div>
                </div>

                {/* Terminal Body Code */}
                <div className="p-5 text-xs sm:text-sm font-mono leading-relaxed overflow-x-auto">
                  <div className="text-slate-500"># Production Kubernetes Deployment Pipeline</div>
                  <div className="mt-2 text-purple-400">name<span className="text-slate-400">:</span> <span className="text-emerald-300">'Cloud-Native Infrastructure Deploy'</span></div>
                  <div className="text-purple-400">on<span className="text-slate-400">:</span></div>
                  <div className="pl-4 text-blue-300">push<span className="text-slate-400">:</span></div>
                  <div className="pl-8 text-slate-300">branches<span className="text-slate-400">:</span> [<span className="text-yellow-300">'main'</span>]</div>
                  <div className="mt-2 text-purple-400">jobs<span className="text-slate-400">:</span></div>
                  <div className="pl-4 text-blue-400">kubernetes-cluster<span className="text-slate-400">:</span></div>
                  <div className="pl-8 text-slate-300">runs-on<span className="text-slate-400">:</span> <span className="text-emerald-300">ubuntu-latest</span></div>
                  <div className="pl-8 text-slate-300">steps<span className="text-slate-400">:</span></div>
                  <div className="pl-12 text-slate-300">- <span className="text-blue-300">name</span><span className="text-slate-400">:</span> Build & Push Docker Container 🐳</div>
                  <div className="pl-14 text-slate-400">run<span className="text-slate-400">:</span> <span className="text-yellow-300">docker build -t devops/api:v2.4 .</span></div>
                  <div className="pl-12 text-slate-300 mt-1">- <span className="text-blue-300">name</span><span className="text-slate-400">:</span> Apply Kubernetes Manifests ☸️</div>
                  <div className="pl-14 text-slate-400">run<span className="text-slate-400">:</span> <span className="text-yellow-300">kubectl apply -f k8s/production/</span></div>
                </div>

                {/* Terminal Footer Progress */}
                <div className="px-5 py-3 bg-slate-900 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono text-slate-300">
                  <div className="flex items-center gap-2">
                    <Play className="w-3.5 h-3.5 text-emerald-400 fill-emerald-400" />
                    <span>Step 2/2: Rolling out pod replicas (24/24)</span>
                  </div>
                  <span className="text-emerald-400 font-bold">SUCCESS</span>
                </div>
              </div>

              {/* Top-Right Floating Status Badge */}
              <div className="absolute -top-6 -right-4 sm:-right-6 bg-white/95 dark:bg-gray-900/95 backdrop-blur-md border border-gray-200/80 dark:border-gray-800 p-3.5 rounded-2xl shadow-xl flex items-center gap-3 animate-bounce-subtle z-20">
                <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 dark:border-emerald-800/60 flex items-center justify-center text-emerald-600 dark:text-emerald-400 shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-gray-900 dark:text-gray-100 flex items-center gap-1.5">
                    <span>EKS Cluster Status</span>
                    <span className="w-2 h-2 rounded-full bg-emerald-500" />
                  </div>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400">100% Healthy • 99.99% Uptime</p>
                </div>
              </div>

              {/* Bottom-Left Floating Pipeline Card */}
              <div className="absolute -bottom-6 -left-4 sm:-left-6 bg-white/95 dark:bg-gray-900/95 backdrop-blur-md border border-gray-200/80 dark:border-gray-800 p-3.5 rounded-2xl shadow-xl flex items-center gap-3.5 z-20 max-w-xs">
                <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/60 border border-blue-200 dark:border-blue-800/60 flex items-center justify-center text-blue-600 dark:text-blue-400 shrink-0">
                  <GitBranch className="w-5 h-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between text-xs font-bold text-gray-900 dark:text-gray-100 mb-1">
                    <span>ArgoCD GitOps Sync</span>
                    <span className="text-blue-600 dark:text-blue-400 text-[10px]">v2.4.0</span>
                  </div>
                  <div className="w-full h-1.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full w-full animate-pulse" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

