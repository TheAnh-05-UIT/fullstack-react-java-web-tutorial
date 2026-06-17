import { useState, useEffect } from 'react';
import { Mail, MapPin, Calendar, ExternalLink, Award, Briefcase, Code } from 'lucide-react';
import { Card, Badge } from '../../components/ui';
import { api } from '../../services/api';

export interface ExperienceInfo {
  title: string;
  company: string;
  period: string;
  description: string;
}

export interface ConnectLinks {
  github: string;
  linkedin: string;
  twitter: string;
}

export interface AboutContent {
  title: string;
  subtitle: string;
  location: string;
  email: string;
  joined: string;
  description: string;
  skills: string[];
  experience: ExperienceInfo[];
  certifications: string[];
  connect: ConnectLinks;
}

const defaultSkills = [
  'Docker', 'Kubernetes', 'Terraform', 'AWS', 'Jenkins', 'GitLab CI/CD',
  'Linux', 'Prometheus', 'Grafana', 'Python', 'Bash', 'Go',
];

const defaultExperience = [
  {
    title: 'Senior DevOps Engineer',
    company: 'TechCorp Inc.',
    period: '2023 - Present',
    description: 'Leading cloud infrastructure automation and CI/CD pipeline optimization.',
  },
  {
    title: 'DevOps Engineer',
    company: 'CloudStartup',
    period: '2021 - 2023',
    description: 'Implemented Kubernetes cluster management and monitoring solutions.',
  },
  {
    title: 'Software Engineer',
    company: 'DevAgency',
    period: '2019 - 2021',
    description: 'Developed microservices and automated deployment workflows.',
  },
];

const defaultCertifications = [
  'AWS Certified Solutions Architect',
  'Certified Kubernetes Administrator (CKA)',
  'HashiCorp Terraform Associate',
  'Docker Certified Associate',
];

export function AboutPage() {
  const [content, setContent] = useState<AboutContent>({
    title: 'John Doe',
    subtitle: 'Senior DevOps Engineer & Content Creator',
    location: 'San Francisco, CA',
    email: 'john@devopsbuilder.io',
    joined: 'Joined Jan 2020',
    description: 'Passionate DevOps engineer with 6+ years of experience in building scalable infrastructure, automating deployment pipelines, and teaching cloud-native technologies. I created DevOpsBuilder to help aspiring engineers learn DevOps through practical tutorials and real-world projects.',
    skills: defaultSkills,
    experience: defaultExperience,
    certifications: defaultCertifications,
    connect: {
      github: 'https://github.com',
      linkedin: 'https://linkedin.com',
      twitter: 'https://twitter.com'
    }
  });

  useEffect(() => {
    api.get<{ key: string, value: string }>('/settings/ABOUT_US_CONTENT')
      .then(response => {
        if (response.value) {
          const data = JSON.parse(response.value);
          // Map old schema to new if necessary
          setContent(prev => ({
            ...prev,
            ...data,
            // Fallback for old schema keys if they existed
            subtitle: data.subtitle || data.mission || prev.subtitle,
          }));
        }
      })
      .catch(err => console.error('Failed to load about content:', err));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="border-b border-gray-200 dark:border-gray-800 bg-gradient-to-br from-primary-50 via-white to-secondary-50 dark:from-primary-950/30 dark:via-gray-950 dark:to-secondary-950/30">
        <div className="container-app py-16">
          <div className="max-w-3xl">
            <div className="flex items-start gap-6">
              <img
                src="https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=200"
                alt="Profile"
                className="w-32 h-32 rounded-2xl object-cover ring-4 ring-white dark:ring-gray-800 shadow-lg"
              />
              <div>
                <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100">
                  {content.title}
                </h1>
                <p className="mt-1 text-xl text-primary-600 dark:text-primary-400">
                  {content.subtitle}
                </p>
                <div className="mt-4 flex flex-wrap gap-4 text-sm text-gray-600 dark:text-gray-400">
                  <div className="flex items-center gap-1.5">
                    <MapPin className="w-4 h-4" />
                    {content.location}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Mail className="w-4 h-4" />
                    {content.email}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-4 h-4" />
                    {content.joined}
                  </div>
                </div>
              </div>
            </div>
            <p className="mt-8 text-lg text-gray-600 dark:text-gray-400 leading-relaxed whitespace-pre-wrap">
              {content.description}
            </p>
          </div>
        </div>
      </div>

      <div className="container-app py-12">
        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-8">
            <Card className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <Briefcase className="w-5 h-5 text-gray-500" />
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Experience</h2>
              </div>
              <div className="space-y-6">
                {content.experience.map((exp, index) => (
                  <div key={index} className="relative pl-6 before:absolute before:left-0 before:top-2 before:w-2 before:h-2 before:rounded-full before:bg-primary-500">
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="font-medium text-gray-900 dark:text-gray-100">{exp.title}</h3>
                        <p className="text-sm text-primary-600 dark:text-primary-400">{exp.company}</p>
                      </div>
                      <span className="text-sm text-gray-500 dark:text-gray-400">{exp.period}</span>
                    </div>
                    <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">{exp.description}</p>
                  </div>
                ))}
              </div>
            </Card>

            <Card className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <Code className="w-5 h-5 text-gray-500" />
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Skills</h2>
              </div>
              <div className="flex flex-wrap gap-2">
                {content.skills.map(skill => (
                  <Badge key={skill} variant="primary">{skill}</Badge>
                ))}
              </div>
            </Card>

          </div>

          <div className="space-y-6">
            <Card className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <Award className="w-5 h-5 text-gray-500" />
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Certifications</h2>
              </div>
              <ul className="space-y-3">
                {content.certifications.map((cert, index) => (
                  <li key={index} className="flex items-start gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <svg className="w-4 h-4 text-success-500 mt-0.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                    {cert}
                  </li>
                ))}
              </ul>
            </Card>

            <Card className="p-6">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Connect</h2>
              <div className="space-y-3">
                {content.connect.github && (
                  <a
                    href={content.connect.github}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                  >
                    <div className="w-10 h-10 rounded-lg bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
                      <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.545 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
                      </svg>
                    </div>
                    <div>
                      <p className="font-medium text-gray-900 dark:text-gray-100">GitHub</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Follow my projects</p>
                    </div>
                    <ExternalLink className="w-4 h-4 text-gray-400 ml-auto" />
                  </a>
                )}
                {content.connect.linkedin && (
                  <a
                    href={content.connect.linkedin}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                  >
                    <div className="w-10 h-10 rounded-lg bg-[#0A66C2]/10 flex items-center justify-center">
                      <svg className="w-5 h-5 text-[#0A66C2]" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
                      </svg>
                    </div>
                    <div>
                      <p className="font-medium text-gray-900 dark:text-gray-100">LinkedIn</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Connect professionally</p>
                    </div>
                    <ExternalLink className="w-4 h-4 text-gray-400 ml-auto" />
                  </a>
                )}
                {content.connect.twitter && (
                  <a
                    href={content.connect.twitter}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
                  >
                    <div className="w-10 h-10 rounded-lg bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
                      <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
                      </svg>
                    </div>
                    <div>
                      <p className="font-medium text-gray-900 dark:text-gray-100">Twitter/X</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Follow for updates</p>
                    </div>
                    <ExternalLink className="w-4 h-4 text-gray-400 ml-auto" />
                  </a>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
