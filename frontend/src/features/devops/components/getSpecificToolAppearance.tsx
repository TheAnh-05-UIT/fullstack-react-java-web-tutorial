import React from 'react';
import {
  Terminal, Boxes, Wrench, Server, BellRing, Flame, CheckSquare, FileText,
  LayoutGrid, Columns, Box, RefreshCw, Navigation, Package, GitBranch,
  GitPullRequest, ShieldCheck, Code, GitCommit, Lock, Hammer, Settings,
  Archive, Zap, Play, FlaskConical, Gauge, Tag, Sliders,
  BarChart3, Network, Eye, Search, Layers, Shield,
} from 'lucide-react';

// Returns the brand-accurate icon and gradient background for a DevOps tool
export function getSpecificToolAppearance(
  toolName: string,
  category: string,
  fallbackBg: string
): { icon: React.ReactNode; bg: string } {
  const n = toolName.toLowerCase();

  if (n.includes('ansible')) return { icon: <Terminal className="w-5 h-5" />, bg: 'bg-gradient-to-br from-red-600 to-rose-600 shadow-md shadow-red-500/25' };
  if (n.includes('terraform') || n.includes('opentofu') || n.includes('pulumi')) return { icon: <Boxes className="w-5 h-5" />, bg: 'bg-gradient-to-br from-purple-600 to-indigo-600 shadow-md shadow-purple-500/25' };
  if (n.includes('chef') || n.includes('puppet')) return { icon: <Wrench className="w-5 h-5" />, bg: 'bg-gradient-to-br from-amber-600 to-orange-500 shadow-md shadow-amber-500/25' };
  if (n.includes('openshift')) return { icon: <Server className="w-5 h-5" />, bg: 'bg-gradient-to-br from-red-700 to-rose-600 shadow-md shadow-red-600/25' };
  if (n.includes('pagerduty') || n.includes('opsgenie')) return { icon: <BellRing className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-green-600 shadow-md shadow-emerald-500/25' };
  if (n.includes('litmus') || n.includes('chaos')) return { icon: <Flame className="w-5 h-5" />, bg: 'bg-gradient-to-br from-orange-600 to-red-600 shadow-md shadow-orange-500/25' };
  if (n.includes('jira')) return { icon: <CheckSquare className="w-5 h-5" />, bg: 'bg-gradient-to-br from-blue-600 to-sky-500 shadow-md shadow-blue-500/25' };
  if (n.includes('confluence')) return { icon: <FileText className="w-5 h-5" />, bg: 'bg-gradient-to-br from-indigo-600 to-blue-500 shadow-md shadow-indigo-500/25' };
  if (n.includes('azure boards')) return { icon: <LayoutGrid className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-blue-600 shadow-md shadow-sky-500/25' };
  if (n.includes('trello')) return { icon: <Columns className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-500 to-indigo-500 shadow-md shadow-sky-500/25' };
  if (n.includes('docker') || n.includes('buildkit') || n.includes('container runtimes')) return { icon: <Box className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-500 to-blue-600 shadow-md shadow-sky-500/25' };
  if (n.includes('argocd')) return { icon: <RefreshCw className="w-5 h-5" />, bg: 'bg-gradient-to-br from-orange-500 to-amber-500 shadow-md shadow-orange-500/25' };
  if (n.includes('kubernetes') || n.includes('kubectl')) return { icon: <Navigation className="w-5 h-5" />, bg: 'bg-gradient-to-br from-blue-600 to-indigo-600 shadow-md shadow-blue-500/25' };
  if (n.includes('helm')) return { icon: <Package className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-blue-700 shadow-md shadow-sky-500/25' };
  if (n.includes('git & github')) return { icon: <GitBranch className="w-5 h-5" />, bg: 'bg-gradient-to-br from-slate-900 to-slate-700 dark:from-slate-800 dark:to-slate-600 shadow-md shadow-slate-900/25' };
  if (n.includes('gitlab') || n.includes('bitbucket')) return { icon: <GitPullRequest className="w-5 h-5" />, bg: 'bg-gradient-to-br from-orange-600 to-red-500 shadow-md shadow-orange-500/25' };
  if (n.includes('sonarqube') || n.includes('eslint')) return { icon: <ShieldCheck className="w-5 h-5" />, bg: 'bg-gradient-to-br from-blue-600 to-indigo-600 shadow-md shadow-blue-500/25' };
  if (n.includes('visual studio code') || n.includes('jetbrains')) return { icon: <Code className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-purple-600 shadow-md shadow-sky-500/25' };
  if (n.includes('pre-commit') || n.includes('husky')) return { icon: <GitCommit className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-teal-500 shadow-md shadow-emerald-500/25' };
  if (n.includes('snyk')) return { icon: <Lock className="w-5 h-5" />, bg: 'bg-gradient-to-br from-purple-600 to-indigo-600 shadow-md shadow-purple-500/25' };
  if (n.includes('maven') || n.includes('gradle')) return { icon: <Hammer className="w-5 h-5" />, bg: 'bg-gradient-to-br from-red-600 to-orange-500 shadow-md shadow-red-500/25' };
  if (n.includes('jenkins') || n.includes('spinnaker')) return { icon: <Settings className="w-5 h-5" />, bg: 'bg-gradient-to-br from-slate-800 to-red-600 shadow-md shadow-slate-800/25' };
  if (n.includes('trivy') || n.includes('cosign')) return { icon: <ShieldCheck className="w-5 h-5" />, bg: 'bg-gradient-to-br from-cyan-600 to-blue-600 shadow-md shadow-cyan-500/25' };
  if (n.includes('harbor') || n.includes('artifactory') || n.includes('nexus')) return { icon: <Archive className="w-5 h-5" />, bg: 'bg-gradient-to-br from-teal-600 to-emerald-600 shadow-md shadow-teal-500/25' };
  if (n.includes('bazel') || n.includes('nx')) return { icon: <Boxes className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-green-500 shadow-md shadow-emerald-500/25' };
  if (n.includes('vitest') || n.includes('jest')) return { icon: <Zap className="w-5 h-5" />, bg: 'bg-gradient-to-br from-amber-500 to-yellow-500 shadow-md shadow-amber-500/25' };
  if (n.includes('playwright') || n.includes('selenium')) return { icon: <Play className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-teal-500 shadow-md shadow-emerald-500/25' };
  if (n.includes('junit') || n.includes('cucumber')) return { icon: <FlaskConical className="w-5 h-5" />, bg: 'bg-gradient-to-br from-green-600 to-emerald-600 shadow-md shadow-green-500/25' };
  if (n.includes('testcontainers')) return { icon: <Boxes className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-indigo-600 shadow-md shadow-sky-500/25' };
  if (n.includes('k6')) return { icon: <Gauge className="w-5 h-5" />, bg: 'bg-gradient-to-br from-purple-600 to-violet-600 shadow-md shadow-purple-500/25' };
  if (n.includes('semantic-release')) return { icon: <Tag className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-green-500 shadow-md shadow-emerald-500/25' };
  if (n.includes('launchdarkly')) return { icon: <Sliders className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-blue-700 shadow-md shadow-sky-500/25' };
  if (n.includes('aws codepipeline')) return { icon: <GitBranch className="w-5 h-5" />, bg: 'bg-gradient-to-br from-amber-600 to-orange-500 shadow-md shadow-amber-500/25' };
  if (n.includes('prometheus') || n.includes('alertmanager')) return { icon: <Flame className="w-5 h-5" />, bg: 'bg-gradient-to-br from-orange-600 to-red-500 shadow-md shadow-orange-500/25' };
  if (n.includes('grafana')) return { icon: <BarChart3 className="w-5 h-5" />, bg: 'bg-gradient-to-br from-amber-500 to-orange-600 shadow-md shadow-amber-500/25' };
  if (n.includes('opentelemetry')) return { icon: <Network className="w-5 h-5" />, bg: 'bg-gradient-to-br from-sky-600 to-blue-600 shadow-md shadow-sky-500/25' };
  if (n.includes('datadog') || n.includes('dynatrace')) return { icon: <Eye className="w-5 h-5" />, bg: 'bg-gradient-to-br from-purple-600 to-violet-600 shadow-md shadow-purple-500/25' };
  if (n.includes('splunk') || n.includes('new relic')) return { icon: <Search className="w-5 h-5" />, bg: 'bg-gradient-to-br from-emerald-600 to-teal-600 shadow-md shadow-emerald-500/25' };
  if (n.includes('elk')) return { icon: <FileText className="w-5 h-5" />, bg: 'bg-gradient-to-br from-yellow-600 to-amber-600 shadow-md shadow-yellow-500/25' };

  // Category-level fallback
  const CATEGORY_ICON: Record<string, React.ReactNode> = {
    'Version Control': <GitBranch className="w-5 h-5" />,
    'CI/CD': <Zap className="w-5 h-5" />,
    'Testing & QA': <FlaskConical className="w-5 h-5" />,
    'Artifacts & Containers': <Package className="w-5 h-5" />,
    'Orchestration & IaC': <Layers className="w-5 h-5" />,
    'Observability & SRE': <BarChart3 className="w-5 h-5" />,
    'Agile Planning': <CheckSquare className="w-5 h-5" />,
    'Security': <Shield className="w-5 h-5" />,
  };

  return { icon: CATEGORY_ICON[category] || <Layers className="w-5 h-5" />, bg: fallbackBg };
}
