import { useEffect, useState } from 'react';
import type { PhaseData } from '../types/devops.types';
import { StickyNav }          from '../components/StickyNav';
import { HeroSection, LIFECYCLE_STAGES } from '../components/HeroSection';
import { CurriculumSection }  from '../components/CurriculumSection';
import { ToolsSection }       from '../components/ToolsSection';
import { LearningPathSection } from '../components/LearningPathSection';
import { LabsSection }        from '../components/LabsSection';
import { PhaseNavFooter }     from '../components/PhaseNavFooter';
import { devopsApi }          from '../services/devopsApi';

const OBSERVED_SECTIONS = ['curriculum', 'tools', 'learning-path', 'labs'];

// Main orchestration component — composes all phase sub-sections and loads dynamic content from Java Backend
export function DevOpsPhaseDetailPage({ phaseKey }: { phaseKey: string }) {
  const [activeSection, setActiveSection] = useState('curriculum');
  const [dynamicData, setDynamicData] = useState<PhaseData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  // Fetch dynamic content from Java Spring Boot Backend when phase changes
  useEffect(() => {
    let isMounted = true;
    setLoading(true);
    setError(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    devopsApi.getPhaseDetailByKey(phaseKey)
      .then(res => {
        if (!isMounted) return;
        if (!res) {
          setError(true);
          return;
        }
        
        const currentStageIndex = LIFECYCLE_STAGES.findIndex(s => s.id === (res.phaseKey || phaseKey));
        const prevStage = currentStageIndex > 0 ? LIFECYCLE_STAGES[currentStageIndex - 1] : null;
        
        let nextStage = null;
        let nextSublabel = 'Next Phase';
        if (currentStageIndex >= 0) {
          if (currentStageIndex === LIFECYCLE_STAGES.length - 1) {
            nextStage = LIFECYCLE_STAGES[0];
            nextSublabel = 'Restart Cycle';
          } else {
            nextStage = LIFECYCLE_STAGES[currentStageIndex + 1];
          }
        }

        // Map PhaseDetailResponse back to PhaseData
        const data: PhaseData = {
          id: res.phaseKey || phaseKey, // MUST be the slug (e.g. 'code', 'plan') for switch statements and active styling
          name: res.name || res.title || '',
          title: res.title || '',
          slug: res.phaseKey || phaseKey,
          stageNumber: res.displayOrder || 1,
          tagline: res.tagline || '',
          summary: res.summary || '',
          heroSnippetTitle: res.heroSnippetTitle || '',
          heroSnippet: res.heroSnippet || '',
          theme: res.theme || {
             gradient: res.colorGradient || '',
             iconBg: '',
             badgeBg: '',
             badgeText: '',
             borderColor: '',
             accentColor: '',
             ctaBg: '',
             ctaText: ''
          },
          curriculum: Array.isArray(res.curriculum) ? res.curriculum : [],
          tools: Array.isArray(res.tools) ? res.tools : [],
          learningPath: Array.isArray(res.learningPath) ? res.learningPath : [],
          quiz: Array.isArray(res.quiz) ? res.quiz : [],
          handsOnLabs: Array.isArray(res.handsOnLabs) ? res.handsOnLabs : [],
          prevNav: prevStage ? { slug: prevStage.id, label: `${prevStage.stageNumber < 10 ? '0' : ''}${prevStage.stageNumber} ${prevStage.label}`, sublabel: 'Previous Phase' } : undefined,
          nextNav: nextStage ? { slug: nextStage.id, label: `${nextStage.stageNumber < 10 ? '0' : ''}${nextStage.stageNumber} ${nextStage.label}`, sublabel: nextSublabel } : undefined,
        };
        
        setDynamicData(data);
      })
      .catch(err => {
        if (!isMounted) return;
        console.error('Failed to load phase data from API:', err);
        setError(true);
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => { isMounted = false; };
  }, [phaseKey]);

  // Intersection Observer to highlight the active sticky-nav tab while scrolling
  useEffect(() => {
    if (!dynamicData) return;
    
    const observers: IntersectionObserver[] = [];

    OBSERVED_SECTIONS.forEach(id => {
      const el = document.getElementById(id);
      if (!el) return;
      const obs = new IntersectionObserver(
        ([entry]) => { if (entry.isIntersecting) setActiveSection(id); },
        { threshold: 0.3 }
      );
      obs.observe(el);
      observers.push(obs);
    });

    return () => observers.forEach(o => o.disconnect());
  }, [dynamicData]);

  if (loading && !dynamicData) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error || !dynamicData) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100">
        <h2 className="text-2xl font-bold mb-4">Lỗi tải dữ liệu</h2>
        <p className="text-slate-600 dark:text-slate-400 mb-6">Không thể tải dữ liệu DevOps Phase từ Server.</p>
        <button onClick={() => window.location.reload()} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
          Thử lại
        </button>
      </div>
    );
  }

  return (
    <div className={`min-h-screen bg-slate-50 dark:bg-slate-950 font-sans text-slate-800 dark:text-slate-100 transition-opacity duration-300 ${loading ? 'opacity-50 pointer-events-none' : 'opacity-100'}`}>
      <StickyNav data={dynamicData} activeSection={activeSection} />
      <HeroSection data={dynamicData} />
      <CurriculumSection data={dynamicData} />
      <ToolsSection data={dynamicData} />
      <LearningPathSection data={dynamicData} />
      <LabsSection data={dynamicData} />
      <PhaseNavFooter data={dynamicData} />
    </div>
  );
}
