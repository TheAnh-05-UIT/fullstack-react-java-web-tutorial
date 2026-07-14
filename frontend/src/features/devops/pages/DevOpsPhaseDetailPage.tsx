import { useEffect, useState } from 'react';
import type { PhaseData } from '../types/devops.types';
import { StickyNav }          from '../components/StickyNav';
import { HeroSection }        from '../components/HeroSection';
import { CurriculumSection }  from '../components/CurriculumSection';
import { ToolsSection }       from '../components/ToolsSection';
import { LearningPathSection } from '../components/LearningPathSection';
import { LabsSection }        from '../components/LabsSection';
import { PhaseNavFooter }     from '../components/PhaseNavFooter';

const OBSERVED_SECTIONS = ['curriculum', 'tools', 'learning-path', 'labs'];

// Main orchestration component — composes all phase sub-sections
export function DevOpsPhaseDetailPage({ data }: { data: PhaseData }) {
  const [activeSection, setActiveSection] = useState('curriculum');

  // Intersection Observer to highlight the active sticky-nav tab while scrolling
  useEffect(() => {
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
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 font-sans text-slate-800 dark:text-slate-100">
      <StickyNav data={data} activeSection={activeSection} />
      <HeroSection data={data} />
      <CurriculumSection data={data} />
      <ToolsSection data={data} />
      <LearningPathSection data={data} />
      <LabsSection data={data} />
      <PhaseNavFooter data={data} />
    </div>
  );
}
