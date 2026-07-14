import { ClipboardList, Code, Hammer, FlaskConical, Package, Rocket, Settings, Activity, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const devOpsPhases = [
  {
    id: '1', slug: 'plan', name: 'Plan', icon: 'clipboard-list',
    goal: 'Define project requirements, set roadmaps, and establish milestones based on business values.',
    keyActivities: 'Managing product backlogs, planning sprints, and creating user stories.',
    tools: ['Jira', 'Trello', 'Azure Boards', 'Confluence']
  },
  {
    id: '2', slug: 'code', name: 'Code', icon: 'code',
    goal: 'Design and write the application logic according to the planning blueprints.',
    keyActivities: 'Writing source code, managing code repositories, and conducting peer reviews.',
    tools: ['Git', 'GitHub', 'GitLab', 'Bitbucket']
  },
  {
    id: '3', slug: 'build', name: 'Build', icon: 'hammer',
    goal: 'Compile the raw code into deployable, functional artifacts.',
    keyActivities: 'Fetching source code from repositories, managing dependencies, and packing code into executables or Docker images.',
    tools: ['Maven', 'Gradle', 'Jenkins']
  },
  {
    id: '4', slug: 'test', name: 'Test', icon: 'flask-conical',
    goal: 'Ensure code stability, functionality, and security before releasing it.',
    keyActivities: 'Executing automated unit, functional, security, and performance tests.',
    tools: ['Selenium', 'JUnit', 'Cucumber', 'SonarQube']
  },
  {
    id: '5', slug: 'release', name: 'Release', icon: 'package',
    goal: 'Finalize and package verified builds for application staging or environments.',
    keyActivities: 'Creating release notes, validating build approvals, and readying artifacts.',
    tools: ['Jenkins', 'Spinnaker', 'AWS CodePipeline']
  },
  {
    id: '6', slug: 'deploy', name: 'Deploy', icon: 'rocket',
    goal: 'Distribute software builds to target production servers or hosting platforms.',
    keyActivities: 'Executing automated scripts to push updates to public servers or staging platforms without interrupting runtime.',
    tools: ['Kubernetes', 'Docker', 'Ansible', 'Terraform']
  },
  {
    id: '7', slug: 'operate', name: 'Operate', icon: 'settings',
    goal: 'Maintain system stability and manage live production infrastructure.',
    keyActivities: 'Configuring runtime environments, scaling server nodes, and applying system patches.',
    tools: ['Ansible', 'Chef', 'Puppet', 'OpenShift']
  },
  {
    id: '8', slug: 'monitor', name: 'Monitor', icon: 'activity',
    goal: 'Analyze performance data and user trends to optimize the active application.',
    keyActivities: 'Reviewing logs, tracking real-time error logs, measuring uptime, and monitoring server metrics.',
    tools: ['Prometheus', 'Grafana', 'Datadog', 'Splunk', 'New Relic']
  },
];

const iconMap: Record<string, React.ReactNode> = {
  'clipboard-list': <ClipboardList className="w-6 h-6" />,
  'code':           <Code className="w-6 h-6" />,
  'hammer':         <Hammer className="w-6 h-6" />,
  'flask-conical':  <FlaskConical className="w-6 h-6" />,
  'package':        <Package className="w-6 h-6" />,
  'rocket':         <Rocket className="w-6 h-6" />,
  'settings':       <Settings className="w-6 h-6" />,
  'activity':       <Activity className="w-6 h-6" />,
};

const colorMap: Record<string, string> = {
  Plan:    'from-blue-500 to-blue-600',
  Code:    'from-indigo-500 to-purple-600',
  Build:   'from-violet-500 to-violet-600',
  Test:    'from-purple-500 to-fuchsia-600',
  Release: 'from-pink-500 to-rose-600',
  Deploy:  'from-orange-500 to-orange-600',
  Operate: 'from-amber-500 to-yellow-500',
  Monitor: 'from-emerald-500 to-teal-600',
};

export function DevOpsLifecycle() {
  const navigate = useNavigate();

  return (
    <section className="py-20 bg-white dark:bg-gray-950">
      <div className="container-app">
        <div className="text-center max-w-3xl mx-auto mb-14">
          <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 dark:text-gray-100 tracking-tight">
            The DevOps Lifecycle & Specialized Tools
          </h2>
          <p className="mt-4 text-base text-gray-600 dark:text-gray-400 leading-relaxed">
            Master each phase of the DevOps infinity loop. Explore exact goals, key activities, and specialized automation tools used across industry-leading teams.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {devOpsPhases.map((phase) => (
            <button
              key={phase.id}
              onClick={() => navigate(`/devops/${phase.slug}`)}
              className="group text-left bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6 flex flex-col justify-between hover:border-gray-300 dark:hover:border-gray-700 hover:-translate-y-1.5 transition-all duration-300 shadow-sm hover:shadow-xl"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${colorMap[phase.name]} flex items-center justify-center text-white shadow-md`}>
                    {iconMap[phase.icon]}
                  </div>
                  <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 font-mono uppercase tracking-wider">
                    Stage {phase.id.padStart(2, '0')}
                  </span>
                </div>

                <h3 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-3 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                  {phase.name}
                </h3>

                <div className="space-y-3 mb-5">
                  <div>
                    <span className="block text-[11px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">Goal</span>
                    <p className="text-xs text-gray-700 dark:text-gray-300 leading-relaxed font-medium">
                      {phase.goal}
                    </p>
                  </div>

                  <div>
                    <span className="block text-[11px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">Key Activities</span>
                    <p className="text-xs text-gray-600 dark:text-gray-400 leading-relaxed">
                      {phase.keyActivities}
                    </p>
                  </div>
                </div>
              </div>

              <div>
                <div className="pt-4 border-t border-gray-100 dark:border-gray-800/80 mb-4">
                  <span className="block text-[10px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">Specialized Tools</span>
                  <div className="flex flex-wrap gap-1.5">
                    {phase.tools.map(tool => (
                      <span key={tool} className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-gray-100 dark:bg-gray-800/90 text-gray-700 dark:text-gray-300">
                        {tool}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs font-bold text-primary-600 dark:text-primary-400 group-hover:translate-x-1 transition-transform pt-2">
                  <span>Explore {phase.name} Phase</span>
                  <ArrowRight className="w-4 h-4" />
                </div>
              </div>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
