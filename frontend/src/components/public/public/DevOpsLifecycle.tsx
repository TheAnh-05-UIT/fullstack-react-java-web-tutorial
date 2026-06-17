import { ClipboardList, Code, Hammer, FlaskConical, Package, Rocket, Settings, Activity } from 'lucide-react';
import { Card } from '../ui';

interface DevOpsPhase {
  id: string;
  name: string;
  description: string;
  icon: string;
  tools: string[];
}

const devOpsPhases: DevOpsPhase[] = [
  { id: '1', name: 'Plan', description: 'Define requirements, estimate resources, and plan project milestones.', icon: 'clipboard-list', tools: ['Jira', 'Confluence', 'Trello'] },
  { id: '2', name: 'Code', description: 'Write, review, and manage source code with version control.', icon: 'code', tools: ['Git', 'GitHub', 'GitLab'] },
  { id: '3', name: 'Build', description: 'Compile source code and create deployable artifacts.', icon: 'hammer', tools: ['Maven', 'Gradle', 'npm'] },
  { id: '4', name: 'Test', description: 'Run automated tests to ensure code quality and functionality.', icon: 'flask-conical', tools: ['Jest', 'Selenium', 'JUnit'] },
  { id: '5', name: 'Release', description: 'Package and version applications for deployment.', icon: 'package', tools: ['Semantic Release', 'Docker', 'Artifactory'] },
  { id: '6', name: 'Deploy', description: 'Release applications to production environments.', icon: 'rocket', tools: ['Docker', 'Kubernetes', 'Terraform', 'ArgoCD'] },
  { id: '7', name: 'Operate', description: 'Manage and maintain production systems and infrastructure.', icon: 'settings', tools: ['Ansible', 'Kubernetes', 'AWS'] },
  { id: '8', name: 'Monitor', description: 'Track system performance and detect issues proactively.', icon: 'activity', tools: ['Prometheus', 'Grafana', 'Datadog'] },
];
const iconMap: Record<string, React.ReactNode> = {
  'clipboard-list': <ClipboardList className="w-6 h-6" />,
  'code': <Code className="w-6 h-6" />,
  'hammer': <Hammer className="w-6 h-6" />,
  'flask-conical': <FlaskConical className="w-6 h-6" />,
  'package': <Package className="w-6 h-6" />,
  'rocket': <Rocket className="w-6 h-6" />,
  'settings': <Settings className="w-6 h-6" />,
  'activity': <Activity className="w-6 h-6" />,
};

const colorMap: Record<string, string> = {
  'Plan': 'from-blue-500 to-blue-600',
  'Code': 'from-indigo-500 to-indigo-600',
  'Build': 'from-violet-500 to-violet-600',
  'Test': 'from-purple-500 to-purple-600',
  'Release': 'from-pink-500 to-pink-600',
  'Deploy': 'from-orange-500 to-orange-600',
  'Operate': 'from-yellow-500 to-yellow-600',
  'Monitor': 'from-green-500 to-green-600',
};

export function DevOpsLifecycle() {
  return (
    <section className="py-20 bg-white dark:bg-gray-950">
      <div className="container-app">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
            The DevOps Lifecycle
          </h2>
          <p className="mt-4 text-gray-600 dark:text-gray-400">
            Master each phase of the DevOps infinity loop. From planning to monitoring,
            learn the tools and practices that modern teams use.
          </p>
        </div>

        <div className="relative">
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {devOpsPhases.map((phase) => (
              <Card
                key={phase.id}
                hover
                className="p-6 group cursor-pointer transition-all duration-300 hover:-translate-y-1"
              >
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${colorMap[phase.name]} flex items-center justify-center text-white mb-4`}>
                  {iconMap[phase.icon]}
                </div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                  {phase.name}
                </h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  {phase.description}
                </p>
                <div className="flex flex-wrap gap-2">
                  {phase.tools.slice(0, 3).map(tool => (
                    <span
                      key={tool}
                      className="text-xs px-2 py-1 rounded-md bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400"
                    >
                      {tool}
                    </span>
                  ))}
                </div>
                <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-800 opacity-0 group-hover:opacity-100 transition-opacity">
                  <span className="text-sm font-medium text-primary-600 dark:text-primary-400 flex items-center gap-1">
                    Learn {phase.name}
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </span>
                </div>
              </Card>
            ))}
          </div>

          <div className="hidden lg:block absolute top-1/2 left-0 right-0 -translate-y-1/2 pointer-events-none">
            <svg className="w-full h-32 opacity-10" viewBox="0 0 1200 100">
              <path
                d="M100,50 C300,0 400,100 600,50 C800,0 900,100 1100,50"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="text-primary-600"
              />
            </svg>
          </div>
        </div>
      </div>
    </section>
  );
}
