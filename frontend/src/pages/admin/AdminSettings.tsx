import { useState, useEffect } from 'react';
import { Card, Button } from '../../components/ui';
import { Save, Loader2, Info } from 'lucide-react';
import { settingsService } from '../../services/settingsService';

interface ExperienceInfo {
  title: string;
  company: string;
  period: string;
  description: string;
}

interface ConnectLinks {
  github: string;
  linkedin: string;
  twitter: string;
}

interface AboutContent {
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

export function AdminSettings() {
  const [content, setContent] = useState<AboutContent>({
    title: '',
    subtitle: '',
    location: '',
    email: '',
    joined: '',
    description: '',
    skills: [],
    experience: [],
    certifications: [],
    connect: { github: '', linkedin: '', twitter: '' }
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      const response = await settingsService.getByKey('ABOUT_US_CONTENT');
      if (response?.value) {
        const data = JSON.parse(response.value);
        setContent(prev => ({
          ...prev,
          ...data,
          subtitle: data.subtitle || data.mission || prev.subtitle,
        }));
      } else {
        // Default content if none exists
        setContent({
          title: 'John Doe',
          subtitle: 'Senior DevOps Engineer & Content Creator',
          location: 'San Francisco, CA',
          email: 'john@devopsbuilder.io',
          joined: 'Joined Jan 2020',
          description: 'Passionate DevOps engineer with 6+ years of experience in building scalable infrastructure, automating deployment pipelines, and teaching cloud-native technologies. I created DevOpsBuilder to help aspiring engineers learn DevOps through practical tutorials and real-world projects.',
          skills: [
            'Docker', 'Kubernetes', 'Terraform', 'AWS', 'Jenkins', 'GitLab CI/CD',
            'Linux', 'Prometheus', 'Grafana', 'Python', 'Bash', 'Go',
          ],
          experience: [
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
          ],
          certifications: [
            'AWS Certified Solutions Architect',
            'Certified Kubernetes Administrator (CKA)',
            'HashiCorp Certified: Terraform Associate',
          ],
          connect: {
            github: 'https://github.com',
            linkedin: 'https://linkedin.com',
            twitter: 'https://twitter.com'
          }
        });
      }
    } catch (error) {
      console.error('Failed to fetch settings', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    setMessage('');
    try {
      await settingsService.updateByKey('ABOUT_US_CONTENT', JSON.stringify(content));
      setMessage('About page content saved successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      setMessage('Failed to save settings.');
      console.error(error);
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Site Settings</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Manage global configuration for your platform</p>
        </div>
        <Button onClick={handleSave} disabled={isSaving} className="flex items-center gap-2">
          {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
          {isSaving ? 'Saving...' : 'Save Changes'}
        </Button>
      </div>

      {message && (
        <div className={`p-4 rounded-xl ${message.includes('success') ? 'bg-green-50 text-green-700 border-green-200' : 'bg-red-50 text-red-700 border-red-200'} border`}>
          {message}
        </div>
      )}

      <div className="grid gap-6">
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-6">
            <Info className="w-5 h-5 text-primary-600" />
            <h2 className="text-lg font-semibold">About Page Content</h2>
          </div>
          
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Main Title
                </label>
                <input
                  type="text"
                  value={content.title}
                  onChange={e => setContent(prev => ({ ...prev, title: e.target.value }))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="e.g. John Doe"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Subtitle
                </label>
                <input
                  type="text"
                  value={content.subtitle}
                  onChange={e => setContent(prev => ({ ...prev, subtitle: e.target.value }))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="e.g. Senior DevOps Engineer"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Location
                </label>
                <input
                  type="text"
                  value={content.location}
                  onChange={e => setContent(prev => ({ ...prev, location: e.target.value }))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="e.g. San Francisco, CA"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  value={content.email}
                  onChange={e => setContent(prev => ({ ...prev, email: e.target.value }))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="e.g. john@example.com"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Joined Date
                </label>
                <input
                  type="text"
                  value={content.joined}
                  onChange={e => setContent(prev => ({ ...prev, joined: e.target.value }))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                  placeholder="e.g. Joined Jan 2020"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Description
              </label>
              <textarea
                value={content.description}
                onChange={e => setContent(prev => ({ ...prev, description: e.target.value }))}
                rows={4}
                className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="Brief description of your platform..."
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Skills (comma separated)
              </label>
              <input
                type="text"
                value={content.skills.join(', ')}
                onChange={e => setContent(prev => ({ ...prev, skills: e.target.value.split(',').map(s => s.trim()).filter(Boolean) }))}
                className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="e.g. Docker, Kubernetes, AWS"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Certifications (one per line)
              </label>
              <textarea
                value={content.certifications.join('\n')}
                onChange={e => setContent(prev => ({ ...prev, certifications: e.target.value.split('\n').map(c => c.trim()).filter(Boolean) }))}
                rows={4}
                className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                placeholder="AWS Certified Solutions Architect&#10;Certified Kubernetes Administrator"
              />
            </div>

            <div className="pt-4 border-t border-gray-200 dark:border-gray-800">
              <h3 className="text-md font-medium text-gray-900 dark:text-gray-100 mb-3">Connect Links</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">GitHub</label>
                  <input
                    type="url"
                    value={content.connect.github}
                    onChange={e => setContent(prev => ({ ...prev, connect: { ...prev.connect, github: e.target.value } }))}
                    className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    placeholder="https://github.com/..."
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">LinkedIn</label>
                  <input
                    type="url"
                    value={content.connect.linkedin}
                    onChange={e => setContent(prev => ({ ...prev, connect: { ...prev.connect, linkedin: e.target.value } }))}
                    className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    placeholder="https://linkedin.com/in/..."
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Twitter/X</label>
                  <input
                    type="url"
                    value={content.connect.twitter}
                    onChange={e => setContent(prev => ({ ...prev, connect: { ...prev.connect, twitter: e.target.value } }))}
                    className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                    placeholder="https://twitter.com/..."
                  />
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-gray-200 dark:border-gray-800">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-md font-medium text-gray-900 dark:text-gray-100">Experience</h3>
                <Button 
                  onClick={() => setContent(prev => ({ ...prev, experience: [...prev.experience, { title: '', company: '', period: '', description: '' }] }))}
                  variant="secondary" 
                  size="sm"
                >
                  + Add Experience
                </Button>
              </div>
              <div className="space-y-4">
                {content.experience.map((exp, index) => (
                  <div key={index} className="p-4 border border-gray-200 dark:border-gray-800 rounded-lg space-y-4">
                    <div className="flex justify-between items-start">
                      <h4 className="text-sm font-medium">Experience Item {index + 1}</h4>
                      <button 
                        onClick={() => setContent(prev => ({ ...prev, experience: prev.experience.filter((_, i) => i !== index) }))}
                        className="text-red-500 hover:text-red-700 text-sm"
                      >
                        Remove
                      </button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div>
                        <label className="block text-xs text-gray-500 mb-1">Title</label>
                        <input
                          type="text"
                          value={exp.title}
                          onChange={e => {
                            const newExp = [...content.experience];
                            newExp[index].title = e.target.value;
                            setContent(prev => ({ ...prev, experience: newExp }));
                          }}
                          className="w-full px-3 py-1.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-gray-500 mb-1">Company</label>
                        <input
                          type="text"
                          value={exp.company}
                          onChange={e => {
                            const newExp = [...content.experience];
                            newExp[index].company = e.target.value;
                            setContent(prev => ({ ...prev, experience: newExp }));
                          }}
                          className="w-full px-3 py-1.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-gray-500 mb-1">Period</label>
                        <input
                          type="text"
                          value={exp.period}
                          onChange={e => {
                            const newExp = [...content.experience];
                            newExp[index].period = e.target.value;
                            setContent(prev => ({ ...prev, experience: newExp }));
                          }}
                          className="w-full px-3 py-1.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Description</label>
                      <textarea
                        value={exp.description}
                        onChange={e => {
                          const newExp = [...content.experience];
                          newExp[index].description = e.target.value;
                          setContent(prev => ({ ...prev, experience: newExp }));
                        }}
                        rows={2}
                        className="w-full px-3 py-1.5 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

          </div>
        </Card>
      </div>
    </div>
  );
}
